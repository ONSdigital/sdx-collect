from json import dumps
import os

from requests.packages.urllib3.exceptions import MaxRetryError

from app import settings
from app.private_publisher import PrivatePublisher
from app.settings import session
from app.helpers.exceptions import DecryptError, BadMessageError, RetryableError


class ResponseProcessor:

    @staticmethod
    def options():
        rv = {}
        try:
            rv["secret"] = os.getenv("SDX_COLLECT_SECRET").encode("ascii")
        except AttributeError:
            # No secret in env
            pass
        return rv

    def __init__(self, logger, tx_id=None):
        self.logger = logger

        if tx_id:
            self.tx_id = tx_id
        else:
            self.tx_id = None

        self.rrm_publisher = PrivatePublisher(
            logger, settings.RABBIT_URLS, settings.RABBIT_RRM_RECEIPT_QUEUE
        )
        self.ctp_publisher = PrivatePublisher(
            logger, settings.RABBIT_URLS, settings.RABBIT_CTP_RECEIPT_QUEUE
        )

    def service_name(self, url=None):
        try:
            parts = url.split('/')
            if 'responses' in parts:
                return 'SDX-STORE'
            elif 'decrypt' in parts:
                return 'SDX-DECRYPT'
            elif 'validate' in parts:
                return 'SDX-VALIDATE'
        except AttributeError as e:
            self.logger.error(e)

    def process(self, encrypted_survey):
        # decrypt
        decrypted_json = self.decrypt_survey(encrypted_survey)

        metadata = decrypted_json['metadata']
        self.logger = self.logger.bind(user_id=metadata['user_id'], ru_ref=metadata['ru_ref'])

        if not self.tx_id:
            self.tx_id = decrypted_json.get('tx_id')
        elif self.tx_id != decrypted_json.get('tx_id'):
            raise BadMessageError

        self.logger = self.logger.bind(tx_id=self.tx_id)

        try:
            self.validate_survey(decrypted_json)
        except BadMessageError:
            # If the validation fails, the message is to be marked "invalid"
            # and then stored. We don't then want to stop processing at this point.
            decrypted_json['invalid'] = True

        self.store_survey(decrypted_json)
        if decrypted_json.get("survey_id") != "feedback":
            self.send_receipt(decrypted_json)
        else:
            self.logger.info("Feedback survey, skipping receipting")

        return

    def send_receipt(self, decrypted_json):
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

        if not decrypted_json.get("survey_id"):
            self.logger.error("No survey id",
                              tx_id=decrypted_json['tx_id'],
                              ru_ref=decrypted_json['metadata']['ru_ref'])
            queue_ok = False
        elif decrypted_json.get("survey_id") == "census":
            self.logger.info("About to publish receipt into ctp queue")
            queue_ok = self.ctp_publisher.publish_message(dumps(receipt_json), secret=settings.SDX_COLLECT_SECRET)
        else:
            self.logger.info("About to publish receipt into rrm queue")
            queue_ok = self.rrm_publisher.publish_message(dumps(receipt_json), secret=settings.SDX_COLLECT_SECRET)

        if not queue_ok:
            raise RetryableError()

        self.logger.info("Receipt published")

    def decrypt_survey(self, encrypted_survey):
        self.logger.info("Decrypting survey")
        response = self.remote_call(settings.SDX_DECRYPT_URL, data=encrypted_survey)
        try:
            self.response_ok(response)
        except BadMessageError:
            # Translate the Bad Message into a Decrypt Error to force quarantine
            raise DecryptError

        self.logger.info("Survey decryption successful")
        return response.json()

    def validate_survey(self, decrypted_json):
        self.logger.info("Validating survey")
        self.response_ok(self.remote_call(settings.SDX_VALIDATE_URL, json=decrypted_json))
        self.logger.info("Survey validation successful")

    def store_survey(self, decrypted_json):
        self.logger.info("Storing survey")
        self.response_ok(self.remote_call(settings.SDX_RESPONSES_URL, json=decrypted_json))
        self.logger.info("Survey storage successful")

    def remote_call(self, request_url, json=None, data=None, headers=None, verify=True, auth=None):
        service = self.service_name(request_url)

        try:
            self.logger.info("Calling service", request_url=request_url, service=service)
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
        request_url = res.url

        service = self.service_name(request_url)

        res_logger = self.logger
        res_logger.bind(request_url=res.url, status=res.status_code)

        if res.status_code == 200 or res.status_code == 201:
            res_logger.info("Returned from service", response="ok", service=service)
            return

        elif res.status_code == 400:
            res_logger.info("Returned from service", response="client error", service=service)
            raise BadMessageError

        else:
            res_logger.error("Returned from service", response="service error", service=service)
            raise RetryableError
