import logging
import os
import os.path
import sys

__doc__ = """
SDX collection processor.

"""
# Transitional until this package is installed with pip
try:
    sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
except Exception as e:
    print("Error: ", e, file=sys.stderr)

from structlog import wrap_logger
from app.async_consumer import AsyncConsumer
from app.response_processor import ResponseProcessor
from app.queue_publisher import QueuePublisher
from app import settings

logging.basicConfig(level=settings.LOGGING_LEVEL, format=settings.LOGGING_FORMAT)
logger = wrap_logger(logging.getLogger(__name__))


def publish_to_retry_queue(message, count):
    publisher = QueuePublisher(logger, settings.RABBIT_URLS, settings.RABBIT_SURVEY_DELAY_QUEUE, arguments={
        'x-message-ttl': settings.QUEUE_RETRY_DELAY_IN_MS,
        'x-dead-letter-exchange': settings.RABBIT_EXCHANGE,
        'x-dead-letter-routing-key': settings.RABBIT_SURVEY_QUEUE,

    })
    return publisher.publish_message(message, headers={'x-delivery-count': count})


def get_delivery_count_from_properties(properties):
    delivery_count = 0
    if properties.headers and 'x-delivery-count' in properties.headers:
        delivery_count = properties.headers['x-delivery-count']

    return delivery_count


class Consumer(AsyncConsumer):

    def __init__(self, args=None, cfg=None):
        self._args = args
        self._cfg = cfg
        super().__init__()

    def on_message(self, unused_channel, basic_deliver, properties, body):
        logger.info('Received message', delivery_tag=basic_deliver.delivery_tag, app_id=properties.app_id)

        delivery_count = get_delivery_count_from_properties(properties)
        delivery_count += 1

        options = ResponseProcessor.options()
        processor = ResponseProcessor(logger)

        try:
            message = body.decode("utf-8")
            processed_ok = processor.process(message, **options)

            if processed_ok:
                self.acknowledge_message(basic_deliver.delivery_tag, tx_id=processor.tx_id)
            else:
                if delivery_count == settings.QUEUE_MAX_MESSAGE_DELIVERIES:
                    logger.error("Reached maximum number of retries", tx_id=processor.tx_id, delivery_count=delivery_count, message=message)
                    self.reject_message(basic_deliver.delivery_tag, tx_id=processor.tx_id)
                else:
                    if publish_to_retry_queue(body, delivery_count):
                        logger.info("Published to retry queue", tx_id=processor.tx_id, queue=settings.RABBIT_SURVEY_DELAY_QUEUE, delivery_count=delivery_count)
                        self.reject_message(basic_deliver.delivery_tag, tx_id=processor.tx_id)
                    else:
                        logger.error("Failed to publish to retry queue", tx_id=processor.tx_id, queue=settings.RABBIT_SURVEY_DELAY_QUEUE)

        except Exception as e:
            logger.error("ResponseProcessor failed", exception=e, tx_id=processor.tx_id)


def main(args=None):
    logger.debug("Starting consumer")

    consumer = Consumer(args)
    try:
        consumer.run()
    except KeyboardInterrupt:
        consumer.stop()

if __name__ == '__main__':
    main()
