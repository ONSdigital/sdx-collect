from app import receipt
from app import settings
from app.settings import session
import logging
from structlog import wrap_logger
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
    def __init__(self, logger):
        self.logger = logger

    def process(self, encrypted_survey):
        # decrypt
        decrypt_result = self.decrypt_survey(encrypted_survey)
        decrypt_ok = response_ok(decrypt_result)
        if not decrypt_ok:
            return False

        decrypted_json = decrypt_result.json()
        metadata = decrypted_json['metadata']
        bound_logger = logger.bind(user_id=metadata['user_id'], ru_ref=metadata['ru_ref'])

        # validate
        validate_ok = self.validate_survey(decrypted_json)
        if not validate_ok:
            return False

        # store
        store_ok = self.store_survey(decrypted_json)
        if not store_ok:
            return False

        # receipt
        if settings.RECEIPT_HOST == "skip":
            bound_logger.debug("RECEIPT|SKIP|skipping sending receipt to RM")
            return True

        receipt_result = receipt.send(decrypted_json)
        if receipt_result.status_code != 201:
            bound_logger.error("RECEIPT|RESPONSE|ERROR: Receipt failed")
            return False

        else:
            bound_logger.debug("RECEIPT|RESPONSE|SUCCESS: Receipt success")
            return True

    def decrypt_survey(self, encrypted_survey):
        return remote_call(settings.SDX_DECRYPT_URL, data=encrypted_survey)

    def validate_survey(self, decrypted_json):
        return remote_call(settings.SDX_VALIDATE_URL, json=decrypted_json)

    def store_survey(self, decrypted_json):
        return remote_call(settings.SDX_STORE_URL, json=decrypted_json)
