import unittest
import json
from app import receipt
from tests.test_data import valid_decrypted, invalid_decrypted
import logging

logger = logging.getLogger(__name__)


def get_file_as_string(filename):
    f = open(filename)
    contents = f.read()
    f.close()
    return contents


class TestReceipt(unittest.TestCase):

    def test_receipt_endpoint_valid_json(self):
        decrypted_json = json.loads(valid_decrypted)
        endpoint_success, endpoint = receipt.get_receipt_endpoint(decrypted_json)
        expected = "http://sdx-mock-receipt:5000/reportingunits/1234570071A/collectionexercises/hfjdskf/receipts"

        self.assertTrue(endpoint_success)
        self.assertEqual(endpoint, expected)

    def test_receipt_endpoint_invalid_json(self):
        decrypted_json = json.loads(invalid_decrypted)
        endpoint_success, endpoint = receipt.get_receipt_endpoint(decrypted_json)

        self.assertFalse(endpoint_success)
        self.assertEqual(endpoint, None)

    def test_get_receipt_headers(self):
        headers = receipt.get_receipt_headers()

        self.assertTrue("Basic " in headers['Authorization'])
        self.assertEqual(headers['Content-Type'], "application/vnd.collections+xml")

    def test_render_xml_valid_json(self):
        decrypted_json = json.loads(valid_decrypted)
        success, output_xml = receipt.get_receipt_xml(decrypted_json)
        expected_xml = get_file_as_string("./tests/xml/valid_receipt.xml")

        self.assertTrue(success)
        self.assertEqual(output_xml, expected_xml)

    def test_render_xml_invalid_json(self):
        decrypted_json = json.loads(invalid_decrypted)
        success, output_xml = receipt.get_receipt_xml(decrypted_json)

        self.assertFalse(success)
        self.assertEqual(output_xml, None)
