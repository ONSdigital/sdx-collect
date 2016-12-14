#!/usr/bin/env python
#   coding: UTF-8

import unittest

from app.common.config import check_safe_value
from app.common.config import generate_config


class CheckSafeValueTests(unittest.TestCase):

    secretBytes = b"SKbH0bRYIa9irkLYxPkAi68JSt0_M1x3lci4nCIK7Ec="
    secretBytesWithDollar = b"SKbH0bRYIa9irkLYxPkAi68J$t0_M1x3lci4nCIK7Ec="
    secretBytesWithHash = b"SKb#0bRYIa9irkLYxPkAi68JSt0_M1x3lci4nCIK7Ec="
    secretString = "xr37-bYBRm1HYsJXuoq1X-gkt3WUxqgiSALpzuvtlSc="
    secretStringWithDollar = "xr37-bYBRm1HYsJXuoq1X-gkt3WUxqgiSALpzuvtl$c="
    secretStringWithHash = "xr37-bYBRm1#YsJXuoq1X-gkt3WUxqgiSALpzuvtlSc="

    def test_bytes(self):
        self.assertFalse(check_safe_value(CheckSafeValueTests.secretBytes))

    def test_bytes_with_dollar(self):
        self.assertIn(b"$", CheckSafeValueTests.secretBytesWithDollar)
        self.assertFalse(check_safe_value(CheckSafeValueTests.secretBytesWithDollar))

    def test_bytes_with_hash(self):
        self.assertIn(b"#", CheckSafeValueTests.secretBytesWithHash)
        self.assertFalse(check_safe_value(CheckSafeValueTests.secretBytesWithHash))

    def test_none(self):
        self.assertFalse(check_safe_value(None))

    def test_string(self):
        self.assertTrue(check_safe_value(CheckSafeValueTests.secretString))

    def test_string_with_dollar(self):
        self.assertIn("$", CheckSafeValueTests.secretStringWithDollar)
        self.assertFalse(check_safe_value(CheckSafeValueTests.secretStringWithDollar))

    def test_string_with_hash(self):
        self.assertIn("#", CheckSafeValueTests.secretStringWithHash)
        self.assertFalse(check_safe_value(CheckSafeValueTests.secretStringWithHash))

class ConfigTests(unittest.TestCase):

    def test_config_needs_secret(self):
        self.assertRaises(ValueError, generate_config)
        self.assertRaises(ValueError, generate_config, None)
