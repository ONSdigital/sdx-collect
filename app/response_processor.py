from app import receipt
from app import settings
from app.settings import session
import logging
from structlog import wrap_logger
import json
from requests.packages.urllib3.exceptions import MaxRetryError

logger = wrap_logger(logging.getLogger(__name__))


def remote_call(request_url, json=None, data=None):
    try:
        logger.info("Calling service", request_url=request_url)
        r = None

        if json:
            r = session.post(request_url, json=json)
        elif data:
            r = session.post(request_url, data=data)
        else:
            r = session.get(request_url)

        return r

    except MaxRetryError:
        logger.error("Max retries exceeded (5)", request_url=request_url)


def response_ok(res):
    if res.status_code == 200:
        logger.info("Returned from service", request_url=res.url, status_code=res.status_code)
        return True

    else:
        logger.error("Returned from service", request_url=res.url, status_code=res.status_code)
        return False


class ResponseProcessor:
    def __init__(self, logger, skip_receipt=True):
        self.logger = logger
        self.skip_receipt = skip_receipt

    def process(self, encrypted_survey):
        # decrypt
        decrypt_ok, decrypt_response = self.decrypt_survey(encrypted_survey)
        if not decrypt_ok:
            self.logger.error("Unable to decrypt survey")
            return False

        decrypted_json = json.loads(decrypt_response)
        metadata = decrypted_json['metadata']
        bound_logger = self.logger.bind(user_id=metadata['user_id'], ru_ref=metadata['ru_ref'])

        # validate
        validate_ok = self.validate_survey(decrypted_json)
        if not validate_ok:
            bound_logger.error("Unable to validate survey")
            return False

        # store
        store_ok = self.store_survey(decrypted_json)
        if not store_ok:
            bound_logger.error("Unable to store survey")
            return False

        # receipt
        if self.skip_receipt:
            bound_logger.debug("RECEIPT|SKIP: Skipping sending receipt to RM")
            return True

        receipt_ok = self.send_receipt(decrypted_json)
        if not receipt_ok:
            bound_logger.error("RECEIPT|RESPONSE|ERROR: Receipt failed")
        else:
            bound_logger.debug("RECEIPT|RESPONSE|SUCCESS: Receipt success")

        return receipt_ok

    def decrypt_survey(self, encrypted_survey):
        response = remote_call(settings.SDX_DECRYPT_URL, data=encrypted_survey)
        decrypt_ok = response_ok(response)
        if decrypt_ok:
            return (True, response)
        else:
            return (False, None)

    def validate_survey(self, decrypted_json):
        response = remote_call(settings.SDX_VALIDATE_URL, json=decrypted_json)
        return response_ok(response)

    def store_survey(self, decrypted_json):
        response = remote_call(settings.SDX_STORE_URL, json=decrypted_json)
        return response_ok(response)

    def send_receipt(self, decrypted_json):
        return receipt.send(decrypted_json)
