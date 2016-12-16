#!/usr/bin/env python
#   coding: UTF-8

from queue_publisher import QueuePublisher

from cryptography.fernet import Fernet


class PrivatePublisher(QueuePublisher):

    def publish_message(self, message, content_type=None, headers=None, secret=None):
        if isinstance(secret, str):
            f = Fernet(secret.encode("utf-8"))
            token = f.encrypt(message.encode("utf-8"))
            message = token.decode("utf-8")
        return super().publish_message(
            message, content_type=content_type, headers=headers
        )
