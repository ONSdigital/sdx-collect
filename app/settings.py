import logging
import os
import requests
from requests.packages.urllib3.util.retry import Retry
from requests.adapters import HTTPAdapter

LOGGING_FORMAT = "%(asctime)s|%(levelname)s: sdx-collect: %(message)s"
LOGGING_LEVEL = logging.getLevelName(os.getenv('LOGGING_LEVEL', 'DEBUG'))

APP_ROOT = os.path.dirname(os.path.abspath(__file__))
APP_TMP = os.path.join(APP_ROOT, 'tmp')

SDX_RESPONSES_URL = os.getenv("SDX_RESPONSES_URL")
SDX_DECRYPT_URL = os.getenv("SDX_DECRYPT_URL")
SDX_VALIDATE_URL = os.getenv("SDX_VALIDATE_URL")

RABBIT_SURVEY_QUEUE = os.getenv('RABBIT_SURVEY_QUEUE')
RABBIT_QUARANTINE_QUEUE = os.getenv('RABBIT_QUARANTINE_QUEUE')
RABBIT_EXCHANGE = os.getenv('RABBITMQ_EXCHANGE')

RABBIT_RRM_RECEIPT_QUEUE = os.getenv('RECEIPT_RRM_QUEUE')
RABBIT_CTP_RECEIPT_QUEUE = os.getenv('RECEIPT_CTP_QUEUE')

SDX_COLLECT_SECRET = os.getenv("SDX_COLLECT_SECRET")
if SDX_COLLECT_SECRET is not None:
    SDX_COLLECT_SECRET = SDX_COLLECT_SECRET.encode("ascii")

RABBIT_URL = 'amqp://{user}:{password}@{hostname}:{port}/{vhost}'.format(
    hostname=os.getenv('RABBITMQ_HOST'),
    port=os.getenv('RABBITMQ_PORT'),
    user=os.getenv('RABBITMQ_DEFAULT_USER'),
    password=os.getenv('RABBITMQ_DEFAULT_PASS'),
    vhost=os.getenv('RABBITMQ_DEFAULT_VHOST')
)

RABBIT_URL2 = 'amqp://{user}:{password}@{hostname}:{port}/{vhost}'.format(
    hostname=os.getenv('RABBITMQ_HOST2'),
    port=os.getenv('RABBITMQ_PORT2'),
    user=os.getenv('RABBITMQ_DEFAULT_USER'),
    password=os.getenv('RABBITMQ_DEFAULT_PASS'),
    vhost=os.getenv('RABBITMQ_DEFAULT_VHOST')
)

RABBIT_URLS = [RABBIT_URL, RABBIT_URL2]

# Configure the number of retries attempted before failing call
session = requests.Session()
retries = Retry(total=5, backoff_factor=0.1)
session.mount('http://', HTTPAdapter(max_retries=retries))
session.mount('https://', HTTPAdapter(max_retries=retries))
