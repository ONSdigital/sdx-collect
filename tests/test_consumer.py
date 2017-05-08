import unittest

from app import consumer


class DotDict(dict):

    __getattr__ = dict.get


class ConsumerTests(unittest.TestCase):

    def setUp(self):
        self.consumer = consumer.Consumer()
        self.props = DotDict({'headers': {'tx_id': 'test'}})
        self.props_no_txid = DotDict({'headers': {}})

    def test_get_tx_id_from_properties(self):
        self.assertEqual('test', self.consumer.get_tx_id_from_properties(self.props))
        self.assertEqual(None, self.consumer.get_tx_id_from_properties(self.props_no_txid))
