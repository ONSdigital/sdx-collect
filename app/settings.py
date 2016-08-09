import logging
import os
import requests
from requests.packages.urllib3.util.retry import Retry
from requests.adapters import HTTPAdapter

LOGGING_FORMAT = "%(asctime)s|%(levelname)s: sdx-collect: %(message)s"
LOGGING_LEVEL = logging.getLevelName(os.getenv('LOGGING_LEVEL', 'DEBUG'))

APP_ROOT = os.path.dirname(os.path.abspath(__file__))
APP_TMP = os.path.join(APP_ROOT, 'tmp')

SDX_STORE_URL = os.getenv("SDX_STORE_URL", "http://sdx-store:5000/responses")
SDX_DECRYPT_URL = os.getenv("SDX_DECRYPT_URL", "http://sdx-decrypt:5000/decrypt")
SDX_VALIDATE_URL = os.getenv("SDX_VALIDATE_URL", "http://sdx-validate:5000/validate")

RECEIPT_HOST = os.getenv("RECEIPT_HOST", "http://sdx-mock-receipt:5000")
RECEIPT_PATH = os.getenv("RECEIPT_PATH", "reportingunits")
RECEIPT_USER = os.getenv("RECEIPT_USER", "")
RECEIPT_PASS = os.getenv("RECEIPT_PASS", "")

RABBIT_QUEUE = os.getenv('RABBITMQ_QUEUE', 'survey')
RABBIT_DELAY_QUEUE = os.getenv('RABBIT_DELAY_QUEUE', 'survey_delay')
RABBIT_EXCHANGE = os.getenv('RABBITMQ_EXCHANGE', 'message')
QUEUE_RETRY_DELAY_IN_MS = 20000
QUEUE_MAX_MESSAGE_DELIVERIES = 3

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

# Configure the number of retries attempted before failing call
session = requests.Session()
retries = Retry(total=5, backoff_factor=0.1)
session.mount('http://', HTTPAdapter(max_retries=retries))
session.mount('https://', HTTPAdapter(max_retries=retries))
