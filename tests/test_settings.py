import unittest
import os

from app import settings


class TestSettings(unittest.TestCase):
    '''
    Simple test for proving VCAP_SERVICES parsing
    '''
    def setUp(self):
        with open("./tests/vcap_example.json") as fp:
            os.environ['VCAP_SERVICES'] = fp.read()

    def test_heartbeat(self):
        self.assertEqual("amqp://rabbit:rabbit@rabbit:5672/%2f", settings.RABBIT_URL)
