from app import settings
import logging
from structlog import wrap_logger
import requests
import base64
import os
from jinja2 import Environment, FileSystemLoader

env = Environment(loader=FileSystemLoader('%s/templates/' % os.path.dirname(__file__)))

logger = wrap_logger(logging.getLogger(__name__))


def get_receipt_endpoint(decrypted_json):
    try:
        statistical_unit_id = decrypted_json['metadata']['ru_ref']
        exercise_sid = decrypted_json['collection']['exercise_sid']
    except KeyError as e:
        logger.error("Unable to get required data from json", exception=repr(e))
        return (False, None)

    host = settings.RECEIPT_HOST
    path = settings.RECEIPT_PATH
    logger.debug("RECEIPT|HOST/PATH: %s%s" % (host, path))
    uri = path + "/" + statistical_unit_id + "/collectionexercises/" + exercise_sid + "/receipts"
    endpoint = host + uri
    return (True, endpoint)


def get_receipt_headers():
    headers = {}
    auth = settings.RECEIPT_USER + ":" + settings.RECEIPT_PASS
    encoded = base64.b64encode(bytes(auth, 'utf-8'))
    headers['Authorization'] = "Basic " + str(encoded)
    headers['Content-Type'] = "application/vnd.collections+xml"
    return headers


def get_receipt_xml(decrypted_json):
    try:
        template = env.get_template('receipt.xml.tmpl')
        output = template.render(survey=decrypted_json)
        return (True, output)

    except Exception as e:
        logger.error("Unable to render xml receipt", exception=repr(e))
        return (False, None)


def send(decrypted_json):
    endpoint_success, endpoint = get_receipt_endpoint(decrypted_json)
    if not endpoint_success:
        return False

    render_success, xml = get_receipt_xml(decrypted_json)
    if not render_success:
        return False

    headers = get_receipt_headers()
    response = requests.post(endpoint, data=xml, headers=headers)
    return True if response.status_code == 201 else False
