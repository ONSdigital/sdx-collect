from app import settings
import logging
from structlog import wrap_logger
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
        return None

    host = settings.RECEIPT_HOST
    path = settings.RECEIPT_PATH
    logger.debug("RECEIPT|HOST/PATH: %s/%s" % (host, path))
    uri = path + "/" + statistical_unit_id + "/collectionexercises/" + exercise_sid + "/receipts"
    endpoint = host + "/" + uri
    logger.debug("RECEIPT|ENDPOINT: %s" % endpoint)
    return endpoint


def get_receipt_xml(decrypted_json):
    try:
        template = env.get_template('receipt.xml.tmpl')
        output = template.render(survey=decrypted_json)
        return output.encode("utf-8")

    except Exception as e:
        logger.error("Unable to render xml receipt", exception=repr(e))
        return None


def get_receipt_headers():
    headers = {}
    auth = settings.RECEIPT_USER + ":" + settings.RECEIPT_PASS
    encoded = base64.b64encode(bytes(auth, 'utf-8'))
    headers['Authorization'] = "Basic " + str(encoded)
    headers['Content-Type'] = "application/vnd.collections+xml"
    return headers
