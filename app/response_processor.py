from app import settings
from app.queue_publisher import QueuePublisher
from app.settings import session
from json import dumps
import os
from requests.packages.urllib3.exceptions import MaxRetryError
import sys


class ResponseProcessor:

    @staticmethod
    def options(cfg, name="default"):
        keys = ("secret",)
        if cfg.has_section(name):
            data = ((k, cfg.get(name, k)) for k in keys)
            rv = dict(pair for pair in data if pair[1] is not None)
        else:
            rv = {}

        variables = {
            k: "{0}_{1}".format(name.replace(".", "_").upper(), k.replace(".", "_").upper())
            for k in keys
        }
        overlay = ((key, os.getenv(var)) or rv[key] for key, var in variables.items())
        rv.update(pair for pair in overlay if pair[1] is not None)
        return rv
        
    def __init__(self, logger):
        self.logger = logger
        self.tx_id = ""

        self.rrm_publisher = QueuePublisher(logger, settings.RABBIT_URLS, settings.RABBIT_RRM_RECEIPT_QUEUE)
        self.ctp_publisher = QueuePublisher(logger, settings.RABBIT_URLS, settings.RABBIT_CTP_RECEIPT_QUEUE)

    def process(self, encrypted_survey, **kwargs):
        # decrypt
        decrypt_ok, decrypted_json = self.decrypt_survey(encrypted_survey)
        if not decrypt_ok:
            return False

        metadata = decrypted_json['metadata']
        self.logger = self.logger.bind(user_id=metadata['user_id'], ru_ref=metadata['ru_ref'])

        if 'tx_id' in decrypted_json:
            self.tx_id = decrypted_json['tx_id']
            self.logger = self.logger.bind(tx_id=self.tx_id)

        # validate
        validate_ok = self.validate_survey(decrypted_json)
        if not validate_ok:
            decrypted_json['invalid'] = True

        # store
        store_ok = self.store_survey(decrypted_json)
        if not store_ok:
            return False

        receipt_json = {
            'tx_id': decrypted_json['tx_id'],
            'collection': {
                'exercise_sid': decrypted_json['collection']['exercise_sid']
            },
            'metadata': {
                'ru_ref': decrypted_json['metadata']['ru_ref'],
                'user_id': decrypted_json['metadata']['user_id']
            }
        }

        if decrypted_json.get("survey_id") and decrypted_json["survey_id"] == "census":
            queue_ok = self.ctp_publisher.publish_message(
                dumps(receipt_json), kwargs.get("secret")
            )
        else:
            queue_ok = self.rrm_publisher.publish_message(
                dumps(receipt_json), kwargs.get("secret")
            )

        if not queue_ok:
            return False
        else:
            return True

    def decrypt_survey(self, encrypted_survey):
        response = self.remote_call(settings.SDX_DECRYPT_URL, data=encrypted_survey)
        decrypt_ok = self.response_ok(response)
        if decrypt_ok:
            return (True, response.json())
        else:
            return (False, None)

    def validate_survey(self, decrypted_json):
        response = self.remote_call(settings.SDX_VALIDATE_URL, json=decrypted_json)
        return self.response_ok(response)

    def store_survey(self, decrypted_json):
        response = self.remote_call(settings.SDX_STORE_URL, json=decrypted_json)
        return self.response_ok(response)

    def remote_call(self, request_url, json=None, data=None, headers=None, verify=True, auth=None):
        try:
            self.logger.info("Calling service", request_url=request_url)
            r = None

            if json:
                r = session.post(request_url, json=json, headers=headers, verify=verify, auth=auth)
            elif data:
                r = session.post(request_url, data=data, headers=headers, verify=verify, auth=auth)
            else:
                r = session.get(request_url, headers=headers, verify=verify, auth=auth)

            return r

        except MaxRetryError:
            self.logger.error("Max retries exceeded (5)", request_url=request_url)

    def response_ok(self, res):
        if res.status_code == 200 or res.status_code == 201:
            self.logger.info("Returned from service", request_url=res.url, status_code=res.status_code)
            return True

        else:
            self.logger.error("Returned from service", request_url=res.url, status_code=res.status_code)
            return False
