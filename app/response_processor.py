from json import dumps
import logging
import os

from requests.packages.urllib3.exceptions import MaxRetryError
from sdc.rabbit import QueuePublisher
from sdc.rabbit.exceptions import PublishMessageError, RetryableError, QuarantinableError
from structlog import wrap_logger

from app import settings
from app.helpers.exceptions import ClientError
from app.private_publisher import PrivatePublisher
from app.settings import session

logger = wrap_logger(logging.getLogger(__name__))


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

    def __init__(self, logger=logger):
        self.logger = logger

        self.tx_id = None

        self.rrm_publisher = PrivatePublisher(
            settings.RABBIT_URLS, settings.RABBIT_RRM_RECEIPT_QUEUE
        )

        self.cs_notifications = QueuePublisher(settings.RABBIT_URLS,
                                               settings.RABBIT_CS_QUEUE)

        self.cs_notifications._durable_queue = False

        self.cora_notifications = QueuePublisher(settings.RABBIT_URLS,
                                                 settings.RABBIT_CORA_QUEUE)

        self.cora_notifications._durable_queue = False

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
            self.logger.error("No valid service name", exception=e)

    def process(self, msg, tx_id=None):
        decrypted_json = self.decrypt_survey(msg)

        metadata = decrypted_json.get('metadata', {})
        self.logger = self.logger.bind(user_id=metadata.get('user_id'),
                                       ru_ref=metadata.get('ru_ref'))

        if not tx_id:
            self.tx_id = decrypted_json.get('tx_id')
        elif tx_id != decrypted_json.get('tx_id'):
            logger.info('tx_ids from decrypted_json and message header do not match.' +
                        ' Rejecting message',
                        decrypted_tx_id=decrypted_json.get('tx_id'),
                        message_tx_id=self.tx_id)
            raise QuarantinableError
        else:
            self.tx_id = tx_id

        self.logger = self.logger.bind(tx_id=self.tx_id)

        response_type = str(decrypted_json.get("type"))

        try:
            self.validate_survey(decrypted_json)
        except ClientError:
            # If the validation fails, the message is to be marked "invalid"
            # and then stored. We don't then want to stop processing at this point.

            decrypted_json['invalid'] = True
            self.logger.info("Invalid survey data, skipping receipting")
        else:
            if response_type.find("feedback") == -1:
                self.logger.info("Receipting survey")
                self.send_receipt(decrypted_json)
            else:
                self.logger.info("Feedback survey, skipping receipting")

        self.store_survey(decrypted_json)

        if response_type.find("feedback") == -1 and not decrypted_json.get('invalid'):
            self.send_notification(decrypted_json.get("survey_id"))
        else:
            self.logger.info("Feedback survey, skipping notification")

        self.logger.unbind("user_id", "ru_ref", "tx_id")

    def send_receipt(self, decrypted_json):
        try:
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
        except KeyError as e:
            self.logger.error("Unsuccesful publish, missing key values", error=e)
            raise QuarantinableError

        if not decrypted_json.get("survey_id"):
            self.logger.error("No survey id")
            raise QuarantinableError

        elif decrypted_json.get("survey_id") == "census":
            self.logger.info("Ignoring received CTP submission")
            return None

        else:

            try:
                self.logger.info("About to publish receipt into rrm queue")
                self.rrm_publisher.publish(dumps(receipt_json),
                                           headers={'tx_id': self.tx_id},
                                           secret=settings.SDX_COLLECT_SECRET)
            except PublishMessageError as e:
                self.logger.error("Unsuccesful publish", error=e)
                raise RetryableError

        self.logger.info("Receipt published")

    def send_notification(self, survey_id):

        try:
            if survey_id == 'census':
                self.logger.info("Ignoring received CTP submission")
            elif survey_id == '144':
                self.logger.info("About to publish notification to cora queue")
                self.cora_notifications.publish_message(self.tx_id, headers={'tx_id': self.tx_id})
            else:
                self.logger.info("About to publish notification to cs queue")
                self.cs_notifications.publish_message(self.tx_id, headers={'tx_id': self.tx_id})
        except PublishMessageError as e:
            self.logger.error("Unable to queue response notification", error=e)
            raise RetryableError

    def decrypt_survey(self, encrypted_survey):
        self.logger.info("Decrypting survey")
        response = self.remote_call(settings.SDX_DECRYPT_URL,
                                    data=encrypted_survey)
        try:
            self.response_ok(response)
        except ClientError:
            self.logger.error("Survey decryption unsuccessful. Quarantining Survey.")
            raise QuarantinableError

        self.logger.info("Survey decryption successful")
        return response.json()

    def validate_survey(self, decrypted_json):
        self.logger.info("Validating survey")
        self.response_ok(self.remote_call(settings.SDX_VALIDATE_URL,
                                          json=decrypted_json))
        self.logger.info("Survey validation successful")

    def store_survey(self, decrypted_json):
        self.logger.info("Storing survey")
        response = self.remote_call(settings.SDX_RESPONSES_URL,
                                    json=decrypted_json)
        try:
            self.response_ok(response)
        except ClientError:
            self.logger.error("Survey storage unsuccessful. Quarantining Survey.")
            raise QuarantinableError

        self.logger.info("Survey storage successful")

    def remote_call(self, request_url, json=None, data=None, headers=None,
                    verify=True, auth=None):
        service = self.service_name(request_url)

        try:
            self.logger.info("Calling service", request_url=request_url,
                             service=service)
            r = None

            if json:
                r = session.post(request_url, json=json, headers=headers,
                                 verify=verify, auth=auth)
            elif data:
                r = session.post(request_url, data=data, headers=headers,
                                 verify=verify, auth=auth)
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

        elif 400 <= res.status_code < 500:
            res_logger.info("Returned from service", response="client error", service=service)
            raise ClientError

        else:
            res_logger.error("Returned from service", response="service error", service=service)
            raise RetryableError
