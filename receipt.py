import logging
import settings
import requests
import base64
from jinja2 import Environment, PackageLoader


env = Environment(loader=PackageLoader('transform', 'templates'))


def get_receipt_endpoint(decrypted_json):
    statistical_unit_id = decrypted_json['metadata']['ru_ref']
    exercise_sid = decrypted_json['collection']['exercise_sid']
    host = settings.RECEIPT_HOST
    path = settings.RECEIPT_PATH
    logging.debug("RECEIPT|HOST/PATH: %s/%s" % (host, path))
    uri = path + "/" + statistical_unit_id + "/collectionexercises/" + exercise_sid + "/receipts"
    return host + uri


def get_receipt_headers():
    headers = {}
    auth = settings.RECEIPT_USER + ":" + settings.RECEIPT_PASS
    encoded = base64.b64encode(bytes(auth, 'utf-8'))
    headers['Authorization'] = "Basic " + encoded
    headers['Content-Type'] = "application/vnd.collections+xml"
    return headers


def get_receipt_xml(decrypted_json):
    template = env.get_template('receipt.tmpl')
    return template.render(survey=decrypted_json)


def send(decrypted_json):
    if settings.RECEIPT_HOST == "skip":
        logging.debug("RECEIPT|SKIP|skipping sending receipt to RM")
        return True

    endpoint = get_receipt_endpoint(decrypted_json)
    xml = get_receipt_xml(decrypted_json)
    result = requests.post(endpoint, data=xml, headers=get_receipt_headers())
    respondent_id = decrypted_json['metadata']['user_id']

    if result.status_code != 201:
        logging.debug("RECEIPT|RESPONSE|ERROR: Receipt failed for respondent_id=%s" % (respondent_id))
    else:
        logging.debug("RECEIPT|RESPONSE|SUCCESS: Receipt success for respondent_id=%s" % (respondent_id))
    return result.status_code == 201
