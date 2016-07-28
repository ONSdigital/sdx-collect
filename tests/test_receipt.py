import unittest
import json
from app import receipt
from tests.test_data import valid_decrypted, invalid_decrypted
import logging

logging.disable(logging.CRITICAL)


def get_file_as_string(filename):
    f = open(filename)
    contents = f.read()
    f.close()
    return contents.rstrip("\n")


class TestReceipt(unittest.TestCase):

    def test_receipt_endpoint_valid_json(self):
        decrypted_json = json.loads(valid_decrypted)
        endpoint = receipt.get_receipt_endpoint(decrypted_json)
        expected = "http://sdx-mock-receipt:5000/reportingunits/12345678901A/collectionexercises/hfjdskf/receipts"

        self.assertEqual(endpoint, expected)

    def test_receipt_endpoint_invalid_json(self):
        decrypted_json = json.loads(invalid_decrypted)
        endpoint = receipt.get_receipt_endpoint(decrypted_json)

        self.assertEqual(endpoint, None)

    def test_get_receipt_headers(self):
        headers = receipt.get_receipt_headers()

        self.assertTrue("Basic " in headers['Authorization'])
        self.assertEqual(headers['Content-Type'], "application/vnd.collections+xml")

    def test_render_xml_valid_json_txid(self):
        decrypted_json = json.loads(valid_decrypted)
        output_xml = receipt.get_receipt_xml(decrypted_json)
        expected_xml = get_file_as_string("./tests/xml/receipt_txid.xml")

        self.assertEqual(output_xml, expected_xml)

    def test_render_xml_valid_json_no_txid(self):
        decrypted_json = json.loads(valid_decrypted)
        del decrypted_json['tx_id']

        output_xml = receipt.get_receipt_xml(decrypted_json)
        expected_xml = get_file_as_string("./tests/xml/receipt_no_txid.xml")

        self.assertEqual(output_xml, expected_xml)

    def test_render_xml_invalid_json(self):
        decrypted_json = json.loads(invalid_decrypted)
        output_xml = receipt.get_receipt_xml(decrypted_json)

        self.assertEqual(output_xml, None)
