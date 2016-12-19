#!/usr/bin/env python
#   coding: UTF-8

import os
import unittest

from app.common.config import aggregate_options
from app.common.config import check_safe_value
from app.common.config import config_parser


class AggregateOptionTests(unittest.TestCase):

    def test_settings_from_config(self):
        cfg = config_parser()
        cfg["sdx.collect"] = {"secret": "x" * 44}
        rv = aggregate_options(cfg, name="sdx.collect", keys=["secret"])
        self.assertEqual({"secret": "x" * 44}, rv)

    @unittest.skipIf(
        "SDX_COLLECT_SECRET" in os.environ,
        "variables match live environment"
    )
    def test_no_settings_only_env(self):
        try:
            os.environ["SDX_COLLECT_SECRET"] = "y" * 44
            self.assertTrue(os.getenv("SDX_COLLECT_SECRET"))
            cfg = config_parser()
            rv = aggregate_options(cfg, name="sdx.collect", keys=["secret"])
            self.assertEqual({"secret": "y" * 44}, rv)
        finally:
            del os.environ["SDX_COLLECT_SECRET"]

    @unittest.skipIf(
        "SDX_COLLECT_SECRET" in os.environ,
        "variables match live environment"
    )
    def test_env_overrides_settings(self):
        try:
            os.environ["SDX_COLLECT_SECRET"] = "y" * 44
            self.assertTrue(os.getenv("SDX_COLLECT_SECRET"))
            cfg = config_parser()
            cfg["sdx.collect"] = {"secret": "x" * 44}
            rv = aggregate_options(cfg, name="sdx.collect", keys=["secret"])
            self.assertEqual({"secret": "y" * 44}, rv)
        finally:
            del os.environ["SDX_COLLECT_SECRET"]

    def test_no_settings(self):
        cfg = config_parser()
        rv = aggregate_options(cfg, name="sdx.collect", keys=["secret"])
        self.assertEqual({}, rv)


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
        self.assertFalse(
            check_safe_value(CheckSafeValueTests.secretBytesWithDollar)
        )

    def test_bytes_with_hash(self):
        self.assertIn(b"#", CheckSafeValueTests.secretBytesWithHash)
        self.assertFalse(
            check_safe_value(CheckSafeValueTests.secretBytesWithHash)
        )

    def test_none(self):
        self.assertFalse(check_safe_value(None))

    def test_string(self):
        self.assertTrue(check_safe_value(CheckSafeValueTests.secretString))

    def test_string_with_dollar(self):
        self.assertIn("$", CheckSafeValueTests.secretStringWithDollar)
        self.assertFalse(
            check_safe_value(CheckSafeValueTests.secretStringWithDollar)
        )

    def test_string_with_hash(self):
        self.assertIn("#", CheckSafeValueTests.secretStringWithHash)
        self.assertFalse(
            check_safe_value(CheckSafeValueTests.secretStringWithHash)
        )
