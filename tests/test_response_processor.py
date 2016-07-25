import unittest
import logging
from structlog import wrap_logger
import json
from unittest.mock import MagicMock
from app.response_processor import ResponseProcessor
from tests.test_data import fake_encrypted, valid_decrypted

logger = wrap_logger(logging.getLogger(__name__))
valid_json = json.loads(valid_decrypted)


class TestResponseProcessor(unittest.TestCase):

    def test_decrypt_failure(self):
        rp = ResponseProcessor(logger)
        rp.decrypt_survey = MagicMock(return_value=(False, None))
        response = rp.process(fake_encrypted)

        rp.decrypt_survey.assert_called_with(fake_encrypted)
        self.assertFalse(response)

    def test_decrypt_success_validate_failure(self):
        rp = ResponseProcessor(logger)
        rp.decrypt_survey = MagicMock(return_value=(True, valid_json))
        rp.validate_survey = MagicMock(return_value=False)
        response = rp.process(fake_encrypted)

        rp.decrypt_survey.assert_called_with(fake_encrypted)
        self.assertFalse(response)

    def test_validate_success_store_failure(self):
        rp = ResponseProcessor(logger)
        rp.decrypt_survey = MagicMock(return_value=(True, valid_json))
        rp.validate_survey = MagicMock(return_value=True)
        rp.store_survey = MagicMock(return_value=False)
        response = rp.process(fake_encrypted)

        rp.store_survey.assert_called_with(valid_json)
        self.assertFalse(response)

    def test_skip_receipt(self):
        rp = ResponseProcessor(logger)
        rp.decrypt_survey = MagicMock(return_value=(True, valid_json))
        rp.validate_survey = MagicMock(return_value=True)
        rp.store_survey = MagicMock(return_value=True)
        rp.skip_receipt = True
        response = rp.process(fake_encrypted)

        rp.store_survey.assert_called_with(valid_json)
        self.assertTrue(response)

    def test_receipt_send_failue(self):
        rp = ResponseProcessor(logger)
        rp.decrypt_survey = MagicMock(return_value=(True, valid_json))
        rp.validate_survey = MagicMock(return_value=True)
        rp.store_survey = MagicMock(return_value=True)
        rp.send_receipt = MagicMock(return_value=False)
        response = rp.process(fake_encrypted)

        rp.send_receipt.assert_called_with(valid_json)
        self.assertFalse(response)

    def test_receipt_send_success(self):
        rp = ResponseProcessor(logger)
        rp.decrypt_survey = MagicMock(return_value=(True, valid_json))
        rp.validate_survey = MagicMock(return_value=True)
        rp.store_survey = MagicMock(return_value=True)
        rp.send_receipt = MagicMock(return_value=True)
        response = rp.process(fake_encrypted)

        rp.send_receipt.assert_called_with(valid_json)
        self.assertTrue(response)
