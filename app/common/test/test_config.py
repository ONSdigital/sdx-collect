#!/usr/bin/env python
#   coding: UTF-8

import unittest

from app.common.config import check_safe_value


class ConfigTests(unittest.TestCase):

    secretBytesWithDollar = b'SKbH0bRYIa9irkLYxPkAi68JSt0_M1x3lci4nCIK7Ec='

    def test_bad_bytes_dollar(self):
        self.assertFalse(check_safe_value(ConfigTests.secretBytesWithDollar))
