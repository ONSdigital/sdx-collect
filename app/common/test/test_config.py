#!/usr/bin/env python
#   coding: UTF-8

import unittest

from app.common.config import check_safe_value


class ConfigTests(unittest.TestCase):

    secretBytes = b"SKbH0bRYIa9irkLYxPkAi68JSt0_M1x3lci4nCIK7Ec="
    secretBytesWithDollar = b"SKbH0bRYIa9irkLYxPkAi68J$t0_M1x3lci4nCIK7Ec="
    secretBytesWithHash = b"SKb#0bRYIa9irkLYxPkAi68JSt0_M1x3lci4nCIK7Ec="
    secretString = "xr37-bYBRm1HYsJXuoq1X-gkt3WUxqgiSALpzuvtlSc="
    secretStringWithDollar = "xr37-bYBRm1HYsJXuoq1X-gkt3WUxqgiSALpzuvtl$c="
    secretStringWithHash = "xr37-bYBRm1#YsJXuoq1X-gkt3WUxqgiSALpzuvtlSc="

    def test_bytes(self):
        self.assertFalse(check_safe_value(ConfigTests.secretBytes))

    def test_bytes_with_dollar(self):
        self.assertIn(b"$", ConfigTests.secretBytesWithDollar)
        self.assertFalse(check_safe_value(ConfigTests.secretBytesWithDollar))

    def test_bytes_with_hash(self):
        self.assertIn(b"#", ConfigTests.secretBytesWithHash)
        self.assertFalse(check_safe_value(ConfigTests.secretBytesWithHash))

    def test_string(self):
        self.assertTrue(check_safe_value(ConfigTests.secretString))

    def test_string_with_dollar(self):
        self.assertIn("$", ConfigTests.secretStringWithDollar)
        self.assertFalse(check_safe_value(ConfigTests.secretStringWithDollar))

    def test_string_with_hash(self):
        self.assertIn("#", ConfigTests.secretStringWithHash)
        self.assertFalse(check_safe_value(ConfigTests.secretStringWithHash))
