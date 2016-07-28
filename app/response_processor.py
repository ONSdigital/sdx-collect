from app import receipt
from app import settings
from app.settings import session
from requests.packages.urllib3.exceptions import MaxRetryError


class ResponseProcessor:
    def __init__(self, logger):
        self.logger = logger
        self.tx_id = ""
        if settings.RECEIPT_HOST == "skip":
            self.skip_receipt = True
        else:
            self.skip_receipt = False

    def process(self, encrypted_survey):
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
            return False

        # store
        store_ok = self.store_survey(decrypted_json)
        if not store_ok:
            return False

        receipt_ok = self.send_receipt(decrypted_json)
        if not receipt_ok:
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

    def send_receipt(self, decrypted_json):
        if self.skip_receipt:
            self.logger.debug("Skipping sending receipt to RRM")
            return True
        else:
            self.logger.debug("Sending receipt to RRM")

        endpoint = receipt.get_receipt_endpoint(decrypted_json)
        if endpoint is None:
            return False

        xml = receipt.get_receipt_xml(decrypted_json)
        if xml is None:
            return False

        headers = receipt.get_receipt_headers()

        response = self.remote_call(endpoint, data=xml.encode("utf-8"), headers=headers, verify=False, auth=(settings.RECEIPT_USER, settings.RECEIPT_PASS))
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
