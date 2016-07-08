import logging
import os

LOGGING_FORMAT = "%(asctime)s|%(levelname)s: sdx-collect: %(message)s"
LOGGING_LOCATION = "logs/collect.log"
LOGGING_LEVEL = logging.DEBUG

APP_ROOT = os.path.dirname(os.path.abspath(__file__))
APP_TMP = os.path.join(APP_ROOT, 'tmp')

SDX_STORE_URL = os.getenv("SDX_STORE_URL", "http://sdx-store:5000/responses")
SDX_DECRYPT_URL = os.getenv("SDX_DECRYPT_URL", "http://sdx-decrypt:5000/decrypt")
SDX_VALIDATE_URL = os.getenv("SDX_VALIDATE_URL", "http://sdx-validate:5000/validate")

RECEIPT_HOST = os.getenv("RECEIPT_HOST", "http://receipt:5000/")
RECEIPT_PATH = os.getenv("RECEIPT_PATH", "reportingunits")
RECEIPT_USER = os.getenv("RECEIPT_USER", "")
RECEIPT_PASS = os.getenv("RECEIPT_PASS", "")

RABBIT_QUEUE = os.getenv('RABBITMQ_QUEUE', 'survey')

RABBIT_URL = 'amqp://{user}:{password}@{hostname}:{port}/{vhost}?connection_attempts={connection_attempts}&retry_delay={retry_delay}'.format(
    hostname=os.getenv('RABBITMQ_HOST', 'rabbit'),
    port=os.getenv('RABBITMQ_PORT', 5672),
    user=os.getenv('RABBITMQ_DEFAULT_USER', 'rabbit'),
    password=os.getenv('RABBITMQ_DEFAULT_PASS', 'rabbit'),
    vhost=os.getenv('RABBITMQ_DEFAULT_VHOST', '%2f'),
    connection_attempts=os.getenv('RABBITMQ_DEFAULT_CONN_ATTEMPTS', 5),
    retry_delay=os.getenv('RABBITMQ_DEFAULT_RETRY_DELAY', 5)
)
