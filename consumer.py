import pika
import io
import logging
import sys
import settings
import requests

logging.basicConfig(stream=sys.stdout, level=settings.LOGGING_LEVEL, format=settings.LOGGING_FORMAT)

logging.debug("sdx-collect|START")

def transform(encrypted_survey):
    # decrypt
    decrypted_result = requests.post(settings.SDX_DECRYPT_URL, data=encrypted_survey)
    decrypted_json = decrypted_result.json()
    # validate
    # TODO: what happens if it cant be decrypted?
    validated_result = requests.post(settings.SDX_VALIDATE_URL, json=decrypted_json)
    logging.debug("sdx-validate status code: " + str(validated_result.status_code))
    if validated_result.status_code == 200:
        # store
        # TODO: what happens if it cant be stored?
        store_result = requests.post(settings.SDX_STORE_URL, json=decrypted_json)
        logging.debug("sdx-store result: " + str(store_result.status_code))
    else:
        # TODO: handle error?
        logging.debug("Problem validating json: " + decrypted_json)
        
    
def on_message(channel, method_frame, header_frame, body):
    logging.debug(method_frame.delivery_tag)
    transform(body)
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
