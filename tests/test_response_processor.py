import json
import logging
import os
import unittest
from unittest.mock import MagicMock, Mock
import mock
from requests import Response

from structlog import wrap_logger

from app.response_processor import ResponseProcessor
from tests.test_data import valid_decrypted
from app.helpers.exceptions import DecryptError, RetryableError, BadMessageError
from app import settings

logger = wrap_logger(logging.getLogger(__name__))
valid_json = json.loads(valid_decrypted)


class RRMQueue(Exception):
    # Hacky, but using exception to check something is on the RIGHT queue in the test
    pass


class CTPQueue(Exception):
    pass


class TestResponseProcessorSettings(unittest.TestCase):

    @unittest.skipIf(
        "SDX_COLLECT_SECRET" in os.environ,
        "variables match live environment"
    )
    def test_no_settings_only_env(self):
        try:
            os.environ["SDX_COLLECT_SECRET"] = "y" * 44
            self.assertTrue(os.getenv("SDX_COLLECT_SECRET"))
            rv = ResponseProcessor.options()
            self.assertEqual({"secret": b"y" * 44}, rv)
        finally:
            del os.environ["SDX_COLLECT_SECRET"]

    def test_no_settings(self):
        rv = ResponseProcessor.options()
        self.assertEqual({}, rv)


class TestResponseProcessor(unittest.TestCase):

    def setUp(self):
        self.rp = ResponseProcessor(logger)

    def _process(self):
        self.rp.process("NxjsJBSahBXHSbxHBasx")

    def test_decrypt(self):
        # <decrypt>
        self.rp.validate_survey = MagicMock()
        self.rp.store_survey = MagicMock()
        self.rp.send_receipt = MagicMock()

        r = Response()

        with mock.patch('app.response_processor.ResponseProcessor.remote_call') as call_mock:
            call_mock.return_value = r
            r._content = valid_decrypted.encode('utf-8')

            # 500 - retry
            r.status_code = 500
            with self.assertRaises(RetryableError):
                self._process()

            # 400 - bad
            r.status_code = 400
            with self.assertRaises(DecryptError):
                self._process()

            # 200 - ok
            r.status_code = 200
            self._process()

    def test_validate(self):
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        # <validate>
        self.rp.store_survey = MagicMock()
        self.rp.send_receipt = MagicMock()

        r = Response()
        with mock.patch('app.response_processor.ResponseProcessor.remote_call') as call_mock:
            call_mock.return_value = r

            # 500 - retry
            r.status_code = 500
            with self.assertRaises(RetryableError):
                self._process()

            # 400 - bad
            # Is allowed to continue so that it may be stored
            r.status_code = 400
            self._process()

            # 200 - ok
            r.status_code = 200
            self._process()

    def test_store(self):
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        self.rp.validate_survey = MagicMock()
        # <store>
        self.rp.send_receipt = MagicMock()

        r = Response()
        with mock.patch('app.response_processor.ResponseProcessor.remote_call') as call_mock:
            call_mock.return_value = r

            # 500 - retry
            r.status_code = 500
            with self.assertRaises(RetryableError):
                self._process()

            # 400 - bad
            r.status_code = 400
            with self.assertRaises(BadMessageError):
                self._process()

            # 200 - ok
            r.status_code = 200
            self._process()

    def test_send_receipt(self):
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        self.rp.validate_survey = MagicMock()
        self.rp.store_survey = MagicMock()
        # <send_receipt>

        # Bad key - none set (shouldn't occur as service will not start without key)
        settings.SDX_COLLECT_SECRET = None
        with self.assertRaises(TypeError):
            self._process()

        # Subsequent tests expect valid key
        settings.SDX_COLLECT_SECRET = "seB388LNHgxcuvAcg1pOV20_VR7uJWNGAznE0fOqKxg=".encode('ascii')

        # rrm queue fail
        with self.assertRaises(RetryableError):
            self._process()

        # rrm publish ok
        self.rp.rrm_publisher.publish_message = MagicMock()
        self._process()

        self._process()

        # Queue types
        self.rp.rrm_publisher.publish_message = Mock(side_effect=RRMQueue)
        self.rp.ctp_publisher.publish_message = Mock(side_effect=CTPQueue)

        with self.assertRaises(RRMQueue):
            self.rp.send_receipt(valid_json)

        census_json = valid_json
        census_json['survey_id'] = 'census'

        with self.assertRaises(CTPQueue):
            self.rp.send_receipt(valid_json)
