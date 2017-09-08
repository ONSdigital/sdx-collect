import copy
import json
import logging
import os
import unittest
from unittest.mock import MagicMock, Mock

import mock
import responses
from requests import Response
from requests.packages.urllib3 import HTTPConnectionPool
from requests.packages.urllib3.exceptions import MaxRetryError
from sdc.rabbit.exceptions import RetryableError, QuarantinableError
from structlog import wrap_logger

from app.helpers.exceptions import ClientError
from app.response_processor import ResponseProcessor
from tests.test_data import feedback_decrypted, valid_decrypted
from app import settings


logger = wrap_logger(logging.getLogger(__name__))
valid_json = json.loads(valid_decrypted)
feedback = json.loads(feedback_decrypted)


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
        self.rp = ResponseProcessor()
        self.rp_invalid = ResponseProcessor()

    def _process(self, tx_id=None):
        self.rp.process("NxjsJBSahBXHSbxHBasx", tx_id=tx_id)

    def _process_invalid(self):
        self.rp_invalid.process("NxjsJBSahBXHSbxHBasx", "NxjsJBSahBXHSbxHBasx")

    def test_decrypt(self):
        # <decrypt>
        self.rp.validate_survey = MagicMock()
        self.rp.store_survey = MagicMock()
        self.rp.send_receipt = MagicMock()
        self.rp.send_notification = MagicMock()

        self.rp_invalid.validate_survey = MagicMock()
        self.rp_invalid.store_survey = MagicMock()
        self.rp_invalid.send_receipt = MagicMock()

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
            with self.assertRaises(QuarantinableError):
                self._process()

            # 200 - ok
            r.status_code = 200
            self._process()

            with self.assertRaises(QuarantinableError):
                self._process_invalid()

    def test_validate_returns_500(self):
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        # <validate>
        self.rp.store_survey = MagicMock()
        self.rp.send_receipt = MagicMock()
        self.rp.send_notification = MagicMock()

        r = Response()
        with mock.patch('app.response_processor.ResponseProcessor.remote_call') as call_mock:
            call_mock.return_value = r

            # 500 - retry
            r.status_code = 500
            with self.assertRaises(RetryableError):
                self._process()
            self.assertFalse(self.rp.send_receipt.called)
            self.assertFalse(self.rp.store_survey.called)

    def test_validate_returns_400(self):
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        # <validate>
        self.rp.store_survey = MagicMock()
        self.rp.send_receipt = MagicMock()

        r = Response()
        with mock.patch('app.response_processor.ResponseProcessor.remote_call') as call_mock:
            call_mock.return_value = r
            # 400 - bad
            r.status_code = 400
            self._process()

            # receipt is not called
            self.assertFalse(self.rp.send_receipt.called)
            # store is called
            self.assertTrue(self.rp.store_survey.called)

    def test_validate_returns_success(self):
        valid_json.pop("invalid", None)
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        # <validate>
        self.rp.store_survey = MagicMock()
        self.rp.send_receipt = MagicMock()
        self.rp.send_notification = MagicMock()

        r = Response()
        with mock.patch('app.response_processor.ResponseProcessor.remote_call') as call_mock:
            call_mock.return_value = r

            # 200 - ok
            r.status_code = 200
            self._process()
            # receipt is not called
            self.assertTrue(self.rp.send_receipt.called)
            # but store is
            self.assertTrue(self.rp.store_survey.called)

    def test_store(self):
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        self.rp.validate_survey = MagicMock()
        # <store>
        self.rp.send_receipt = MagicMock()
        self.rp.send_notification = MagicMock()

        r = Response()
        with mock.patch('app.response_processor.ResponseProcessor.remote_call') as call_mock:
            call_mock.return_value = r

            # 500 - retry
            r.status_code = 500
            with self.assertRaises(RetryableError):
                self._process()

            # 400 - bad
            r.status_code = 400
            with self.assertRaises(QuarantinableError):
                self._process()

            # 200 - ok
            r.status_code = 200
            self._process()

    def test_tx_id_set(self):
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        self.rp.validate_survey = MagicMock()
        self.rp.store_survey = MagicMock()
        self.rp.send_receipt = MagicMock()
        self.rp.send_notification = MagicMock()

        r = Response()
        with mock.patch('app.response_processor.ResponseProcessor.remote_call') as call_mock:
            r.status_code = 200
            self._process(tx_id=valid_json.get('tx_id'))
            self.assertEqual(self.rp.tx_id, valid_json.get('tx_id'))

    def test_send_receipt(self):
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        self.rp.validate_survey = MagicMock()
        self.rp.store_survey = MagicMock()
        self.rp.send_notification = MagicMock()
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

        # # rrm queue fail census
        census_json = valid_json
        census_json['survey_id'] = 'census'
        with self.assertLogs(level='INFO') as cm:
            self.rp.send_receipt(census_json)

        self.assertIn("Ignoring received CTP submission", cm.output[0])

        # rrm publish ok
        json_023 = valid_json
        json_023['survey_id'] = '023'
        self.rp.rrm_publisher.publish_message = MagicMock()
        self._process()

        self._process()

        # Queue types
        self.rp.rrm_publisher.publish_message = Mock(side_effect=RRMQueue)

        with self.assertRaises(RRMQueue):
            self.rp.send_receipt(valid_json)

        census_json = valid_json
        census_json['survey_id'] = 'census'

        with self.assertLogs(level='INFO') as cm:
            self.rp.send_receipt(census_json)

        self.assertIn("Ignoring received CTP submission", cm.output[0])

        invalid_json = copy.deepcopy(valid_json)
        invalid_json['survey_id'] = None

        with self.assertRaises(QuarantinableError):
            self.rp.send_receipt(invalid_json)

        invalid_json.pop('tx_id', None)

        with self.assertRaises(QuarantinableError):
            self.rp.send_receipt(invalid_json)

    def test_send_notification(self):
        self.rp.decrypt_survey = MagicMock(return_value=valid_json)
        self.rp.validate_survey = MagicMock()
        self.rp.store_survey = MagicMock()
        self.rp.send_receipt = MagicMock()

        # Subsequent tests expect valid key
        settings.SDX_COLLECT_SECRET = "seB388LNHgxcuvAcg1pOV20_VR7uJWNGAznE0fOqKxg=".encode('ascii')

        # cs notifications queue fail
        with self.assertRaises(RetryableError):
            self._process()

        # # census notifications logged
        census_json = valid_json
        census_json['survey_id'] = 'census'
        with self.assertLogs(level='INFO') as cm:
            self._process()

        self.assertIn("Ignoring received CTP submission", cm.output[1])

        # # cora notifications queue fail census
        cora_json = valid_json
        cora_json['survey_id'] = '144'
        with self.assertRaises(RetryableError):
            self._process()

        # # passes notifications feedback
        feedback_json = valid_json
        feedback_json['survey_id'] = 'feedback'
        self._process()

        # # passes notifications invalid survey
        self.rp.validate_survey = MagicMock(side_effect=ClientError)
        self._process()

        # cs notifications queue publish ok
        json_023 = valid_json
        json_023['survey_id'] = '023'
        json_023.pop('invalid', None)
        self.rp.cs_notifications.publish_message = MagicMock()
        self.rp.validate_survey = MagicMock()
        self._process()

    @responses.activate
    def test_remote_call_get(self):
        url = "http://www.testing.test/responses"
        responses.add(responses.GET, url, json={'status': 'ok'}, status=200)
        self.rp.remote_call(url)
        self.assertEqual(len(responses.calls), 1)

    @responses.activate
    def test_remote_call_post_json(self):
        url = "http://www.testing.test/responses"
        responses.add(responses.POST, url, json={'status': 'ok'}, status=200)
        self.rp.remote_call(url, json={"fruit": "banana"})
        self.assertEqual(len(responses.calls), 1)

    @responses.activate
    def test_remote_call_post_data(self):
        url = "http://www.testing.test/responses"
        responses.add(responses.POST, url, json={'status': 'ok'}, status=200)
        self.rp.remote_call(url, data="banana")
        self.assertEqual(len(responses.calls), 1)

    @responses.activate
    def test_remote_call_maxretryerror(self):
        url = "http://www.testing.test/responses"
        responses.add(responses.GET, url, body=MaxRetryError(HTTPConnectionPool, url))
        with self.assertLogs(level="ERROR") as cm:
            self.rp.remote_call(url)

        self.assertIn("Max retries exceeded (5)", cm[0][0].message)

    def test_send_feedback(self):
        self.rp.decrypt_survey = MagicMock(return_value=feedback)
        self.rp.validate_survey = MagicMock()
        self.rp.store_survey = MagicMock()

        self.rp.send_receipt = Mock(side_effect=RRMQueue)

        self._process()

    def test_service_name_return_responses(self):
        url = "www.testing.test/responses"
        service = self.rp.service_name(url)
        self.assertEqual(service, 'SDX-STORE')

    def test_service_name_return_decrypt(self):
        url = "www.testing.test/decrypt"
        service = self.rp.service_name(url)
        self.assertEqual(service, 'SDX-DECRYPT')

    def test_service_name_return_validate(self):
        url = "www.testing.test/validate"
        service = self.rp.service_name(url)
        self.assertEqual(service, 'SDX-VALIDATE')

    def test_service_name_return_none(self):
        url = "www.testing.test/test/12345"
        service = self.rp.service_name(url)
        self.assertEqual(service, None)

    def test_url_service_name_none(self):
        url = None
        service = self.rp.service_name(url)
        self.assertEqual(service, None)
