import unittest

from app.consumer import check_globals


class CheckGlobals(unittest.TestCase):

    def test_check_globals_negative(self):
        class MockModule:

            SDX_VAR1 = "some/path"
            SDX_VAR2 = None

        self.assertFalse(check_globals(MockModule))

    def test_check_globals_positive(self):
        class MockModule:

            SDX_VAR1 = "some/path"
            SDX_VAR2 = 8080

        self.assertTrue(check_globals(MockModule))
