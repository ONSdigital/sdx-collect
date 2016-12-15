import json
import logging
import os
import unittest
from unittest.mock import MagicMock

from structlog import wrap_logger

import app.common.config
from app.response_processor import ResponseProcessor
from tests.test_data import fake_encrypted, valid_decrypted

logger = wrap_logger(logging.getLogger(__name__))
valid_json = json.loads(valid_decrypted)


class TestResponseProcessorSettings(unittest.TestCase):

    def test_settings_from_config(self):
        cfg = app.common.config.config_parser(
            content=app.common.config.generate_config(secret="x" * 44)
        )
        rv = ResponseProcessor.options(cfg, name="sdx.collect")
        self.assertEqual({"secret": "x" * 44}, rv)

    def test_settings_from_env(self):
        os.environ["SDX_COLLECT_SECRET"] = "y" * 44
        self.assertTrue(os.getenv("SDX_COLLECT_SECRET"))
        try:
            cfg = app.common.config.config_parser(
                content=app.common.config.generate_config(secret="x" * 44)
            )
            rv = ResponseProcessor.options(cfg, name="sdx.collect")
            self.assertEqual({"secret": "y" * 44}, rv)
        finally:
            del os.environ["SDX_COLLECT_SECRET"]

    def test_no_settings(self):
        cfg = app.common.config.config_parser()
        rv = ResponseProcessor.options(cfg, name="sdx.collect")
        self.assertEqual({}, rv)

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
        rp.store_survey = MagicMock(return_value=True)
        rp.rrm_publisher.publish_message = MagicMock(return_value=True)
        rp.skip_receipt = True
        response = rp.process(fake_encrypted)

        rp.decrypt_survey.assert_called_with(fake_encrypted)
        # When validate returns a failure, the process still actually is meant
        # to continue, store and return successfully. So even though it's a
        # "failure" we still expect True
        self.assertTrue(response)

    def test_validate_success_store_failure(self):
        rp = ResponseProcessor(logger)
        rp.decrypt_survey = MagicMock(return_value=(True, valid_json))
        rp.validate_survey = MagicMock(return_value=True)
        rp.store_survey = MagicMock(return_value=False)
        response = rp.process(fake_encrypted)

        rp.store_survey.assert_called_with(valid_json)
        self.assertFalse(response)
