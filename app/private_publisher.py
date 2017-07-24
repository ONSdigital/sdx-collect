from sdc.rabbit.publisher import QueuePublisher

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
        Secret key must be 32 url-safe base64-encoded bytes or string.

        Returned value is a string.

        """
        try:
            f = Fernet(secret)
        except ValueError:
            return None
        try:
            message = f.decrypt(token)
        except TypeError:
            message = f.decrypt(token.encode("utf-8"))
        return message.decode("utf-8")

    def publish_message(self, message, content_type=None, headers=None, secret=None):
        token = PrivatePublisher.encrypt(message, secret=secret)
        return super().publish_message(
            token, content_type=content_type, headers=headers
        )
