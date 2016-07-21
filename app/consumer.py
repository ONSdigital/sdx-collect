import logging
from app import settings
from structlog import wrap_logger
from app.async_consumer import AsyncConsumer
from app.response_processor import ResponseProcessor

logger = wrap_logger(logging.getLogger(__name__))


class Consumer(AsyncConsumer):
    def on_message(self, unused_channel, basic_deliver, properties, body):
        logger.info('Received message', delivery_tag=basic_deliver.delivery_tag, app_id=properties.app_id, body=body)
        try:
            skip_receipt = (settings.RECEIPT_HOST == "skip")
            processor = ResponseProcessor(logger, skip_receipt)
            response_str = body.decode("utf-8")
            processed_ok = processor.process(response_str)

            if processed_ok:
                self.acknowledge_message(basic_deliver.delivery_tag, processor.tx_id)

        except Exception as e:
            logger.error("ResponseProcessor failed", exception=e)

    def acknowledge_message(self, delivery_tag, tx_id):
        logger.info('Acknowledging message', d_tag=delivery_tag, tx_id=tx_id)
        self._channel.basic_ack(delivery_tag)


def main():
    logger.debug("Starting consumer")
    consumer = Consumer()
    try:
        consumer.run()
    except KeyboardInterrupt:
        consumer.stop()

if __name__ == '__main__':
    main()
