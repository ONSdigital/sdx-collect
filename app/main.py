#!/usr/bin/env python3
#   encoding: UTF-8

import os.path
import sys

from sdc.rabbit import AsyncConsumer
from sdc.rabbit import MessageConsumer
from sdc.rabbit import QueuePublisher

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.response_processor import ResponseProcessor
import app.settings


if __name__ == "__main__":
    consumer = AsyncConsumer(
        durable_queue=True,
        exchange=app.settings.RABBIT_EXCHANGE,
        exchange_type="topic",
        rabbit_queue=app.settings.RABBIT_QUEUE,
        rabbit_urls=app.settings.RABBIT_URLS
    )
    quarantine_publisher = QueuePublisher(
        urls=app.settings.RABBIT_URLS,
        queue=app.settings.RABBIT_QUARANTINE_QUEUE
    )
    message_consumer = MessageConsumer(
        consumer, quarantine_publisher,
        process=ResponseProcessor.process
    )

    try:
        consumer.run()
    except KeyboardInterrupt:
        consumer.stop()
