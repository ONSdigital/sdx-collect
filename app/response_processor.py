from datetime import datetime
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
from app import session

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
        self.rrm_publisher = PrivatePublisher(settings.RABBIT_URLS, settings.RABBIT_RRM_RECEIPT_QUEUE)
        self.notifications = QueuePublisher(settings.RABBIT_URLS, settings.RABBIT_SURVEY_QUEUE)
        self.dap = QueuePublisher(settings.RABBIT_URLS, settings.RABBIT_DAP_QUEUE)

    def service_name(self, url=None):
        try:
            parts = url.split('/')
            if 'responses' in parts:
                return 'SDX-STORE'
            elif 'decrypt' in parts:
                return 'SDX-DECRYPT'
            elif 'validate' in parts:
                return 'SDX-VALIDATE'
        except AttributeError:
            self.logger.exception("No valid service name")

    def process(self, msg, tx_id=None, decrypt=True):
        # Bind the tx_id from the rabbit message header as we don't have access to the one in the survey yet.
        self.logger = self.logger.bind(tx_id=tx_id)

        if decrypt:
            decrypted_json = self.decrypt_survey(msg)
        else:
            decrypted_json = msg

        metadata = decrypted_json.get('metadata', {})
        self.logger = self.logger.bind(user_id=metadata.get('user_id'), ru_ref=metadata.get('ru_ref'))

        if not tx_id:
            self.tx_id = decrypted_json.get('tx_id')
        elif tx_id != decrypted_json.get('tx_id'):
            self.logger.info(
                'tx_ids from decrypted_json and message header do not match. Rejecting message',
                decrypted_tx_id=decrypted_json.get('tx_id'),
                message_tx_id=self.tx_id)
            raise QuarantinableError
        else:
            self.tx_id = tx_id

        valid = self.validate_survey(decrypted_json)

        if not valid:
            self.logger.info("Invalid survey data, skipping receipting and downstream processing")
            decrypted_json['invalid'] = True

        id_tag = self.store_survey(decrypted_json)
        self.logger.info("id_tag: {}".format(id_tag))

        if valid and self._requires_receipting(decrypted_json):
            self.send_receipt(decrypted_json)

        if valid and self._requires_downstream_processing(decrypted_json):
            self.send_notification(id_tag)

        if valid and self._requires_dap_processing(decrypted_json):
            self.send_to_dap_queue(decrypted_json)

        # If we don't unbind these fields, their current value will be retained for the next
        # submission.  This leads to incorrect values being logged out in the bound fields.
        self.logger = self.logger.unbind("user_id", "ru_ref", "tx_id")

    def decrypt_survey(self, encrypted_survey):
        self.logger.info("Decrypting survey")
        response = self.remote_call(settings.SDX_DECRYPT_URL, data=encrypted_survey)
        try:
            self.response_ok(response)
        except ClientError:
            self.logger.error("Survey decryption unsuccessful. Quarantining Survey.")
            raise QuarantinableError

        self.logger.info("Survey decryption successful")
        return response.json()

    def validate_survey(self, decrypted_json):
        self.logger.info("Validating survey")
        try:
            self.response_ok(self.remote_call(settings.SDX_VALIDATE_URL, json=decrypted_json))
        except ClientError:
            # If the validation fails, the message is to be marked "invalid"
            # and then stored. We don't then want to stop processing at this point.
            return False

        self.logger.info("Survey validation successful")
        return True

    def store_survey(self, decrypted_json):
        self.logger.info("Storing survey")
        response = self.remote_call(settings.SDX_RESPONSES_URL, json=decrypted_json)

        try:
            self.response_ok(response)
        except ClientError:
            self.logger.error("Survey storage unsuccessful. Quarantining Survey.")
            raise QuarantinableError

        self.logger.info("Survey storage successful")
        return response

    def _requires_receipting(self, decrypted_json):
        if self._is_feedback_survey(decrypted_json):
            self.logger.info("Feedback survey, skipping receipting")
            return False
        return True

    def make_receipt(self, decrypted_json):
        try:
            receipt_json = {
                'case_id': decrypted_json['case_id'],
                'tx_id': decrypted_json['tx_id'],
                'collection': {
                    'exercise_sid':
                    decrypted_json['collection']['exercise_sid']
                },
                'metadata': {
                    'ru_ref': decrypted_json['metadata']['ru_ref'],
                    'user_id': decrypted_json['metadata']['user_id']
                }
            }
        except KeyError:
            self.logger.exception("Unsuccessful publish, missing key values")
            raise QuarantinableError

        return receipt_json

    def _requires_dap_processing(self, decrypted_json):
        if self._is_feedback_survey(decrypted_json):
            self.logger.info("Feedback survey, skipping sending to DAP")
            return False
        if decrypted_json.get("survey_id") in ["023", "134", "147", "281", "283", "lms", "census"]:
            # RSI, MWSS, EPE, Dtrades
            self.logger.info("Sending to DAP", survey_id=decrypted_json.get("survey_id"))
            return True
        return False

    def make_dap_data(self, decrypted_json):
        """Creates the json payload required by minifi to send the submission to dap"""
        self.logger.info("Creating dap data")

        response = self.remote_call(f"{settings.SDX_RESPONSES_URL}/{decrypted_json['tx_id']}")
        try:
            self.response_ok(response)
        except ClientError:
            self.logger.error("Survey retrieval failed. Quarantining Survey.")
            raise QuarantinableError

        try:
            description = "{} survey response for period {} sample unit {}".format(
                decrypted_json['survey_id'],
                decrypted_json['collection']['period'],
                decrypted_json['metadata']['ru_ref'])
            dap_json = {
                'version': '1',
                'files': [{
                    'name': f"{decrypted_json['tx_id']}.json",
                    'URL': f"{settings.SDX_RESPONSES_URL}/{decrypted_json['tx_id']}",
                    'sizeBytes': response.headers['Content-Length'],
                    'md5sum': response.headers['Content-MD5']
                }],
                'sensitivity': 'High',
                'sourceName': settings.DAP_SOURCE_NAME,
                'manifestCreated': self._get_formatted_current_utc(),
                'description': description,
                'iterationL1': decrypted_json['collection']['period'],
                'dataset': decrypted_json['survey_id'],
                'schemaversion': '1'
            }
        except KeyError:
            self.logger.exception("Unsuccesful publish, missing key values")
            raise QuarantinableError

        self.logger.info("Created dap data")
        return dap_json

    def _get_formatted_current_utc(self):
        """
        Returns a formatted utc date with only 3 milliseconds as opposed to the ususal 6 that python provides.
        Additionally, we provide the Zulu time indicator (Z) at the end to indicate it being UTC time. This is
        done for consistency with timestamps provided in other languages.
        The format the time is returned is YYYY-mm-ddTHH:MM:SS.fffZ (e.g., 2018-10-10T08:42:24.737Z)
        """
        date_time = datetime.utcnow()
        milliseconds = date_time.strftime("%f")[:3]
        return f"{date_time.strftime('%Y-%m-%dT%H:%M:%S')}.{milliseconds}Z"

    def _requires_downstream_processing(self, decrypted_json):
        # if self._is_feedback_survey(decrypted_json):
        #     self.logger.info("Feedback survey, downstream processing")
        #     return True
        if decrypted_json.get("version") == "0.0.2":
            survey_id = decrypted_json.get("survey_id")
            self.logger.info("Skipping downstream processing", survey_id=survey_id)
            return False
        elif decrypted_json.get("survey_id") == "283":
            self.logger.info("Covid-19 survey, skipping downstream processing")
            return False
        return True

    @staticmethod
    def _is_feedback_survey(decrypted_json):
        response_type = str(decrypted_json.get("type"))
        return response_type.find("feedback") != -1

    def send_receipt(self, decrypted_json):
        if not decrypted_json.get("survey_id"):
            self.logger.error("No survey id")
            raise QuarantinableError

        self.logger.info("Receipting survey")
        receipt = self.make_receipt(decrypted_json)
        try:
            self.logger.info("About to publish receipt into rrm queue")
            self.logger.debug(str(receipt))
            self.rrm_publisher.publish(
                dumps(receipt),
                headers={'tx_id': decrypted_json['tx_id']},
                secret=settings.SDX_COLLECT_SECRET)
            self.logger.info("Receipt published")
        except PublishMessageError:
            self.logger.exception("Unsuccesful publish")
            raise RetryableError

    def send_notification(self, id_tag):
        self.logger.info("Sending to downstream")
        try:
            self.logger.info("About to publish notification to queue")
            # self.notifications.publish_message(id_tag, headers={'tx_id': self.tx_id})
            self.notifications.publish_message(headers={'tx_id': self.tx_id})
        except PublishMessageError:
            self.logger.exception("Unable to queue response notification")
            raise RetryableError

    def send_to_dap_queue(self, decrypted_json):
        self.logger.info("Sending data to dap queue")
        message = self.make_dap_data(decrypted_json)
        try:
            self.logger.info("Publishing data to dap queue")
            self.dap.publish_message(dumps(message), headers={'tx_id': self.tx_id})
        except PublishMessageError:
            self.logger.exception("Failed to publish to dap queue")
            raise RetryableError
        self.logger.info("Successfully published to dap queue")

    def remote_call(self, request_url, json=None, data=None):
        service = self.service_name(request_url)

        try:
            self.logger.info("Calling service", request_url=request_url, service=service)
            if json:
                return session.post(request_url, json=json, verify=True)
            if data:
                return session.post(request_url, data=data, verify=True)

            return session.get(request_url, verify=True)

        except MaxRetryError:
            self.logger.error("Max retries exceeded (5)", request_url=request_url)
            raise RetryableError
        except ConnectionError:
            self.logger.error("Connection error occurred. Retrying")
            raise RetryableError

    def response_ok(self, res):
        request_url = res.url

        service = self.service_name(request_url)

        res_logger = self.logger.bind(request_url=res.url, status=res.status_code)

        if res.status_code == 200 or res.status_code == 201:
            res_logger.info("Returned from service", response="ok", service=service)
            return

        elif 400 <= res.status_code < 500:
            if res.json().get('contains_invalid_character'):
                logger.error("Invalid character found in payload, quarantining submission")
                raise QuarantinableError
            res_logger.error("Returned from service", response="client error", service=service)
            raise ClientError

        else:
            res_logger.error("Returned from service", response="service error", service=service)
            raise RetryableError
