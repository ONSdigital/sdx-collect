#!/usr/bin/env python
#   coding: UTF-8

from app.queue_publisher import QueuePublisher

from cryptography.fernet import Fernet


class PrivatePublisher(QueuePublisher):

    @staticmethod
    def encrypt(message, secret):
        """
        Message may be a string or bytes.
        Secret key must be 32 url-safe base64-encoded bytes.

        """
        try:
            f = Fernet(secret)
        except ValueError:
            return None
        try:
            token = f.encrypt(message)
        except TypeError:
            token = f.encrypt(message.encode("utf-8"))
        return token

    @staticmethod
    def decrypt(token, secret):
        """
        Secret key must be 32 url-safe base64-encoded bytes

        Returned value is a string.
        """
        try:
            f = Fernet(secret)
            message = f.decrypt(token)
        except ValueError:
            return None
        return message.decode("utf-8")

    def publish_message(self, message, content_type=None, headers=None, secret=None):
        if isinstance(secret, str):
            f = Fernet(secret.encode("utf-8"))
            token = f.encrypt(message.encode("utf-8"))
            message = token.decode("utf-8")
        return super().publish_message(
            message, content_type=content_type, headers=headers
        )
