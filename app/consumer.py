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
from app import __version__
from app.async_consumer import AsyncConsumer
from app.response_processor import ResponseProcessor
from app import settings
from app.queue_publisher import QueuePublisher

logging.basicConfig(level=settings.LOGGING_LEVEL, format=settings.LOGGING_FORMAT)
logger = wrap_logger(logging.getLogger(__name__))

from app.helpers.exceptions import BadMessageError, DecryptError, RetryableError


class Consumer(AsyncConsumer):

    def __init__(self, args=None, cfg=None):
        self._args = args
        self._cfg = cfg
        self.quarantine_publisher = QueuePublisher(logger,
                                                   settings.RABBIT_URLS,
                                                   settings.RABBIT_QUARANTINE_QUEUE)
        super().__init__()

    def get_delivery_count_from_properties(self, properties):
        """
        Returns the delivery count for a message from the rabbit queue. The
        value is auto-set by rabbitmq.
        """
        delivery_count = 0
        if properties.headers and 'x-delivery-count' in properties.headers:
            delivery_count = properties.headers['x-delivery-count']
        return delivery_count + 1

    def get_tx_id_from_properties(self, properties):
        """
        Returns the tx_id for a message from a rabbit queue. The value is
        auto-set by rabbitmq.
        """
        try:
            tx_id = properties.headers['tx_id']
            logger.info("Retrieved tx_id from message properties", tx_id=tx_id)
            return tx_id
        except KeyError as e:
            logger.error("No tx_id in message properties. Sending message to quarantine")
            raise e

    def on_message(self, unused_channel, basic_deliver, properties, body):

        delivery_count = self.get_delivery_count_from_properties(properties)

        try:
            tx_id = self.get_tx_id_from_properties(properties)
        except KeyError as e:
            self.reject_message(basic_deliver.delivery_tag)
            logger.error("Bad message properties",
                         action="quarantined",
                         exception=e,
                         delivery_count=delivery_count)

        logger.info(
            'Received message',
            queue=self.QUEUE,
            delivery_tag=basic_deliver.delivery_tag,
            delivery_count=delivery_count,
            app_id=properties.app_id,
            tx_id=tx_id,
        )

        processor = ResponseProcessor(logger)

        try:
            processor.process(body.decode("utf-8"))
            self.acknowledge_message(basic_deliver.delivery_tag, tx_id=tx_id)

        except DecryptError as e:
            # Throw it into the quarantine queue to be dealt with
            self.reject_message(basic_deliver.delivery_tag, tx_id=tx_id)
            logger.error("Bad decrypt",
                         action="quarantined",
                         exception=e,
                         tx_id=tx_id,
                         delivery_count=delivery_count)

        except BadMessageError as e:
            # If it's a bad message then we have to reject it
            self.reject_message(basic_deliver.delivery_tag, tx_id=tx_id)
            logger.error("Bad message",
                         action="rejected",
                         exception=e, tx_id=tx_id,
                         delivery_count=delivery_count)

        except (RetryableError, Exception) as e:
            self.nack_message(basic_deliver.delivery_tag, tx_id=tx_id)
            logger.error("Failed to process",
                         action="nack",
                         exception=e,
                         tx_id=tx_id,
                         delivery_count=delivery_count)


def main(args=None):
    logger.info("Starting consumer", version=__version__)

    if settings.SDX_COLLECT_SECRET is None:
        logger.error("No SDX_COLLECT_SECRET env var supplied")
        sys.exit(1)

    consumer = Consumer(args)
    try:
        consumer.run()
    except KeyboardInterrupt:
        consumer.stop()


if __name__ == '__main__':
    main()
