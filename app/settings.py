import logging
import os
import requests
from requests.packages.urllib3.util.retry import Retry
from requests.adapters import HTTPAdapter


LOGGING_LEVEL = logging.getLevelName(os.getenv("LOGGING_LEVEL", "DEBUG"))
LOGGING_FORMAT = "%(asctime)s.%(msecs)06dZ|%(levelname)s: sdx-collect: %(message)s"

APP_ROOT = os.path.dirname(os.path.abspath(__file__))
APP_TMP = os.path.join(APP_ROOT, 'tmp')

SDX_RESPONSES_URL = os.getenv("SDX_RESPONSES_URL", "http://sdx-store:5000/responses")
SDX_DECRYPT_URL = os.getenv("SDX_DECRYPT_URL", "http://sdx-decrypt:5000/decrypt")
SDX_VALIDATE_URL = os.getenv("SDX_VALIDATE_URL", "http://sdx-validate:5000/validate")

RABBIT_QUEUE = os.getenv('RABBIT_SURVEY_QUEUE', 'survey')
RABBIT_QUARANTINE_QUEUE = os.getenv('RABBIT_QUARANTINE_QUEUE', 'survey_quarantine')
RABBIT_EXCHANGE = os.getenv('RABBITMQ_EXCHANGE', 'message')

RABBIT_RRM_RECEIPT_QUEUE = os.getenv('RECEIPT_RRM_QUEUE', 'rrm_receipt')
RABBIT_CTP_RECEIPT_QUEUE = os.getenv('RECEIPT_CTP_QUEUE', 'ctp_receipt')

SDX_COLLECT_SECRET = os.getenv("SDX_COLLECT_SECRET")
if SDX_COLLECT_SECRET is not None:
    SDX_COLLECT_SECRET = SDX_COLLECT_SECRET.encode("ascii")

RABBIT_URL = 'amqp://{user}:{password}@{hostname}:{port}/{vhost}'.format(
    hostname=os.getenv('RABBITMQ_HOST', 'rabbit'),
    port=os.getenv('RABBITMQ_PORT', 5672),
    user=os.getenv('RABBITMQ_DEFAULT_USER', 'rabbit'),
    password=os.getenv('RABBITMQ_DEFAULT_PASS', 'rabbit'),
    vhost=os.getenv('RABBITMQ_DEFAULT_VHOST', '%2f')
)

RABBIT_URL2 = 'amqp://{user}:{password}@{hostname}:{port}/{vhost}'.format(
    hostname=os.getenv('RABBITMQ_HOST2', 'rabbit'),
    port=os.getenv('RABBITMQ_PORT2', 5672),
    user=os.getenv('RABBITMQ_DEFAULT_USER', 'rabbit'),
    password=os.getenv('RABBITMQ_DEFAULT_PASS', 'rabbit'),
    vhost=os.getenv('RABBITMQ_DEFAULT_VHOST', '%2f')
)

RABBIT_URLS = [RABBIT_URL, RABBIT_URL2]

RABBIT_CTP_QUEUE = os.getenv('SDX_COLLECT_RABBIT_CTP_QUEUE',
                             'sdx-ctp-survey-notifications')
RABBIT_SURVEY_QUEUE = os.getenv('SDX_COLLECT_RABBIT_QUEUE',
                                'sdx-survey-notification-durable')

# Configure the number of retries attempted before failing call
session = requests.Session()
retries = Retry(total=5, backoff_factor=0.1)
session.mount('http://', HTTPAdapter(max_retries=retries))
session.mount('https://', HTTPAdapter(max_retries=retries))
