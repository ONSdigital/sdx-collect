#!/usr/bin/env python
#  coding: UTF-8

import argparse
import os.path

DFLT_LOCN = os.path.expanduser("~")

__doc__ = """
SDX CLI Interface
"""


def add_common_options(parser):
    parser.add_argument(
        "--version", action="store_true", default=False,
        help="Print the current version number")
    parser.add_argument(
        "--work", default=DFLT_LOCN,
        help="Set a path to the working directory")
    return parser


def parser(description=__doc__):
    rv = argparse.ArgumentParser(
        description,
    )
    rv = add_common_options(rv)
    return rv
