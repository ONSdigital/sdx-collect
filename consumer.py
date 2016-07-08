import pika
import logging
from structlog import wrap_logger
import sys
import settings
import requests
import receipt

logging.basicConfig(stream=sys.stdout, level=settings.LOGGING_LEVEL, format=settings.LOGGING_FORMAT)
logger = wrap_logger(logging.getLogger(__name__))
logger.debug("START")


def decrypt_survey(encrypted_survey):
    return requests.post(settings.SDX_DECRYPT_URL, data=encrypted_survey)


def validate_survey(decrypted_json):
    return requests.post(settings.SDX_VALIDATE_URL, json=decrypted_json)


def store_survey(decrypted_json):
    return requests.post(settings.SDX_STORE_URL, json=decrypted_json)


def process(encrypted_survey):
    decrypted_result = decrypt_survey(encrypted_survey)
    if decrypted_result.status_code != 200:
        logger.error("Decrypt survey failed", request_url=settings.SDX_DECRYPT_URL)
        return

    decrypted_json = decrypted_result.json()
    metadata = decrypted_json['metadata']
    bound_logger = logger.bind(user_id=metadata['user_id'], ru_ref=metadata['ru_ref'])

    # TODO: what happens if it cant be decrypted?
    validated_result = validate_survey(decrypted_json)
    if validated_result.status_code != 200:
        bound_logger.error("Problem validating json", status=validated_result.status_code)
        return

    # TODO: what happens if it cant be stored?
    store_result = store_survey(decrypted_json)
    if store_result.status_code != 200:
        bound_logger.error("Unable to store survey", request_url=settings.SDX_STORE_URL)
        return

    if settings.RECEIPT_HOST == "skip":
        bound_logger.debug("RECEIPT|SKIP|skipping sending receipt to RM")
        return

    receipt_result = receipt.send(decrypted_json)
    if receipt_result.status_code != 201:
        bound_logger.error("RECEIPT|RESPONSE|ERROR: Receipt failed")
    else:
        bound_logger.debug("RECEIPT|RESPONSE|SUCCESS: Receipt success")


def on_message(channel, method_frame, header_frame, body):
    logger.debug(method_frame.delivery_tag)
    process(body)
    channel.basic_ack(delivery_tag=method_frame.delivery_tag)

connection = pika.BlockingConnection(pika.URLParameters(settings.RABBIT_URL))
channel = connection.channel()
channel.queue_declare(queue=settings.RABBIT_QUEUE)
channel.basic_consume(on_message, settings.RABBIT_QUEUE)

try:
    channel.start_consuming()
except KeyboardInterrupt:
    channel.stop_consuming()

connection.close()
