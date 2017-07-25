#!/usr/bin/env python3
#   encoding: UTF-8

from sdc.rabbit import AsyncConsumer
from sdc.rabbit import MessageConsumer
from sdc.rabbit import QueuePublisher

from app.response_processor import ResponseProcessor


if __name__ == "__main__":
    consumer = AsyncConsumer(
        durable_queue=True,
        exchange="/",
        exchange_type="topic",
        rabbit_queue="test",
        [amqp_url]
    )
    quarantine_publisher = QueuePublisher(
        urls=[amqp_url],
        queue="test_quarantine"
    )
    message_consumer = MessageConsumer(
        consumer, quarantine_publisher,
        process=ResponseProcessor.process
    )
