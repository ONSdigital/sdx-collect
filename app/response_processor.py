from app import receipt
from app import settings
from app.settings import session
from requests.packages.urllib3.exceptions import MaxRetryError


class ResponseProcessor:
    def __init__(self, logger, skip_receipt=True):
        self.logger = logger
        self.skip_receipt = skip_receipt

    def process(self, encrypted_survey):
        # decrypt
        decrypt_ok, decrypted_json = self.decrypt_survey(encrypted_survey)
        if not decrypt_ok:
            return False

        metadata = decrypted_json['metadata']
        self.logger = self.logger.bind(user_id=metadata['user_id'], ru_ref=metadata['ru_ref'])

        # validate
        validate_ok = self.validate_survey(decrypted_json)
        if not validate_ok:
            return False

        # store
        store_ok = self.store_survey(decrypted_json)
        if not store_ok:
            return False

        # receipt
        if self.skip_receipt:
            self.logger.debug("RECEIPT|SKIP: Skipping sending receipt to RM")
            return True

        receipt_ok = self.send_receipt(decrypted_json)
        if not receipt_ok:
            self.logger.error("RECEIPT|RESPONSE|ERROR: Receipt failed")
        else:
            self.logger.debug("RECEIPT|RESPONSE|SUCCESS: Receipt success")

        return receipt_ok

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

    def send_receipt(self, decrypted_json):
        endpoint_success, endpoint = receipt.get_receipt_endpoint(decrypted_json)
        if not endpoint_success:
            return False

        render_success, xml = receipt.get_receipt_xml(decrypted_json)
        if not render_success:
            return False

        return receipt.send(endpoint, xml.encode("utf-8"))

    def remote_call(self, request_url, json=None, data=None):
        try:
            self.logger.info("Calling service", request_url=request_url)
            r = None

            if json:
                r = session.post(request_url, json=json)
            elif data:
                r = session.post(request_url, data=data)
            else:
                r = session.get(request_url)

            return r

        except MaxRetryError:
            self.logger.error("Max retries exceeded (5)", request_url=request_url)

    def response_ok(self, res):
        if res.status_code == 200:
            self.logger.info("Returned from service", request_url=res.url, status_code=res.status_code)
            return True

        else:
            self.logger.error("Returned from service", request_url=res.url, status_code=res.status_code)
            return False
