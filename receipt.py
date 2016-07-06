import logging
from structlog import wrap_logger
import settings
import requests
import base64
import os
from jinja2 import Environment, FileSystemLoader


env = Environment(loader=FileSystemLoader('%s/templates/' % os.path.dirname(__file__)))

logger = wrap_logger(
    logging.getLogger(__name__)
)


def get_receipt_endpoint(decrypted_json):
    statistical_unit_id = decrypted_json['metadata']['ru_ref']
    exercise_sid = decrypted_json['collection']['exercise_sid']
    host = settings.RECEIPT_HOST
    path = settings.RECEIPT_PATH
    logger.debug("RECEIPT|HOST/PATH: %s/%s" % (host, path))
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
    endpoint = get_receipt_endpoint(decrypted_json)
    xml = get_receipt_xml(decrypted_json)
    return requests.post(endpoint, data=xml, headers=get_receipt_headers())
