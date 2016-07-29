from app import settings
import logging
from structlog import wrap_logger
import os
from jinja2 import Environment, FileSystemLoader

env = Environment(loader=FileSystemLoader('%s/templates/' % os.path.dirname(__file__)))

logger = wrap_logger(logging.getLogger(__name__))


def get_statistical_unit_id(ru_ref):
    if ru_ref is None:
        return ''

    length = len(ru_ref)
    if length < 12:
        return ru_ref

    if length == 12 and ru_ref[-1:].isalpha():
        return ru_ref[0:11]

    return ru_ref


def get_receipt_endpoint(decrypted_json):
    try:
        ru_ref = decrypted_json['metadata']['ru_ref']
        statistical_unit_id = get_statistical_unit_id(ru_ref)
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
        return output

    except Exception as e:
        logger.error("Unable to render xml receipt", exception=repr(e))
        return None


def get_receipt_headers():
    headers = {}
    headers['Content-Type'] = "application/vnd.collections+xml"
    return headers
