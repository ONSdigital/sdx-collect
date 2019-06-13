import json
import logging
import os


LOGGING_LEVEL = logging.getLevelName(os.getenv("LOGGING_LEVEL", "DEBUG"))
LOGGING_FORMAT = "%(asctime)s.%(msecs)06dZ|%(levelname)s: sdx-collect: %(message)s"

APP_ROOT = os.path.dirname(os.path.abspath(__file__))
APP_TMP = os.path.join(APP_ROOT, 'tmp')

SDX_RESPONSES_URL = os.getenv("SDX_RESPONSES_URL", "http://sdx-store:5000/responses")
SDX_DECRYPT_URL = os.getenv("SDX_DECRYPT_URL", "http://sdx-decrypt:5000/decrypt")
SDX_VALIDATE_URL = os.getenv("SDX_VALIDATE_URL", "http://sdx-validate:5000/validate")

RABBIT_QUEUE = os.getenv('RABBIT_SURVEY_QUEUE', 'sdx_gateway_collect')
RABBIT_QUARANTINE_QUEUE = os.getenv('RABBIT_QUARANTINE_QUEUE', 'survey_quarantine')
RABBIT_EXCHANGE = 'message'

RABBIT_RRM_RECEIPT_QUEUE = 'rrm_receipt'
HEARTBEAT_INTERVAL = "?heartbeat=5"

DAP_SOURCE_NAME = os.getenv("DAP_SOURCE_NAME", "sdx_development")


SDX_COLLECT_SECRET = os.getenv("SDX_COLLECT_SECRET")
if SDX_COLLECT_SECRET is not None:
    SDX_COLLECT_SECRET = SDX_COLLECT_SECRET.encode("ascii")


def parse_vcap_services():
    vcap_services = os.getenv("VCAP_SERVICES")
    parsed_vcap_services = json.loads(vcap_services)
    rabbit_config = parsed_vcap_services.get('rabbitmq')

    rabbit_url = rabbit_config[0].get('credentials').get('uri') + HEARTBEAT_INTERVAL
    return rabbit_url


if os.getenv("CF_DEPLOYMENT", False):
    RABBIT_URL = parse_vcap_services()
else:
    RABBIT_URL = 'amqp://{user}:{password}@{hostname}:{port}/{vhost}'.format(
        hostname=os.getenv('RABBITMQ_HOST', 'rabbit'),
        port=os.getenv('RABBITMQ_PORT', 5672),
        user=os.getenv('RABBITMQ_DEFAULT_USER', 'rabbit'),
        password=os.getenv('RABBITMQ_DEFAULT_PASS', 'rabbit'),
        vhost=os.getenv('RABBITMQ_DEFAULT_VHOST', '%2f')
    ) + HEARTBEAT_INTERVAL

RABBIT_URLS = [RABBIT_URL]

RABBIT_SURVEY_QUEUE = 'sdx-survey-notification-durable'
RABBIT_DAP_QUEUE = 'sdx_dap'
