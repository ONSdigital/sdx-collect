#!/usr/bin/env python
#   coding: UTF-8

import base64
import unittest

from app.private_publisher import PrivatePublisher


class PrivatePublisherTests(unittest.TestCase):

    def test_encrypt_bytes_message(self):
        secret = base64.b64encode(b"x" * 32)
        message = "Test string".encode("utf-8")
        rv = PrivatePublisher.encrypt(message, secret=secret)
        self.assertIsInstance(rv, bytes)
        self.assertIsInstance(rv.decode("ascii"), str)
        self.assertIsInstance(base64.urlsafe_b64decode(rv.decode("ascii")), bytes)

    def test_encrypt_string_message(self):
        secret = base64.b64encode(b"x" * 32)
        message = "Test string"
        rv = PrivatePublisher.encrypt(message, secret=secret)
        self.assertIsInstance(rv, bytes)
        self.assertIsInstance(rv.decode("ascii"), str)
        self.assertIsInstance(base64.urlsafe_b64decode(rv.decode("ascii")), bytes)

    def test_roundtrip_bytes_message(self):
        secret = base64.b64encode(b"x" * 32)
        message = "Test string"
        token = PrivatePublisher.encrypt(message.encode("utf-8"), secret=secret)
        rv = PrivatePublisher.decrypt(token, secret=secret)
        self.assertEqual(message, rv)

    def test_roundtrip_string_message(self):
        secret = base64.b64encode(b"x" * 32)
        message = "Test string"
        token = PrivatePublisher.encrypt(message, secret=secret)
        rv = PrivatePublisher.decrypt(token, secret=secret)
        self.assertEqual(message, rv)

    def test_decrypt_with_string_token(self):
        secret = base64.b64encode(b"x" * 32)
        message = "Test string"
        token = PrivatePublisher.encrypt(message.encode("utf-8"), secret=secret)
        rv = PrivatePublisher.decrypt(token.decode("utf-8"), secret=secret)
        self.assertEqual(message, rv)
