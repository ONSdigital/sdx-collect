import pika
import io
import logging
import sys
import settings
import requests
import receipt

logging.basicConfig(stream=sys.stdout, level=settings.LOGGING_LEVEL, format=settings.LOGGING_FORMAT)

logging.debug("sdx-collect|START")

def decrypt_survey(encrypted_survey):
    result = requests.post(settings.SDX_DECRYPT_URL, data=encrypted_survey)
    return result.json()

def validate_survey(decrypted_json):
    return requests.post(settings.SDX_VALIDATE_URL, json=decrypted_json)

def store_survey(decrypted_json):
    return requests.post(settings.SDX_STORE_URL, json=decrypted_json)

def process(encrypted_survey):
    decrypted_json = decrypt_survey(encrypted_survey)
    # TODO: what happens if it cant be decrypted?
    validated_result = validate_survey(decrypted_json)
    if validated_result.status_code == 200:
        # TODO: what happens if it cant be stored?
        store_survey(decrypted_json)
        receipt.send(decrypted_json)
    else:
        # TODO: handle error?
        logging.debug("Problem validating json: " + decrypted_json)
        
    
def on_message(channel, method_frame, header_frame, body):
    logging.debug(method_frame.delivery_tag)
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
