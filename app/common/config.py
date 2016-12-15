#!/usr/bin/env python
#   coding: UTF-8

import configparser
import itertools
import os.path
import re
import sys

# Transitional until this package is installed with pip
try:
    sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
except Exception as e:
    print("Error: ", e, file=sys.stderr)

import common.cli

__doc__ = """
This module contains library functions to work with configuration data.

When you run it as a program, it will generate a fresh SDX configuration, either
as a `.cfg` or in `.env` format.

For help, call the `-h` option::

    python3 sdx-collect/app/common/config.py -h

"""

configTemplate = """
[sdx.collect]
secret = {secret}

[sdx.receipt.ctp]
secret = ${{sdx.collect:secret}}

[sdx.receipt.rrm]
secret = ${{sdx.collect:secret}}
""".lstrip()


def check_safe_value(val):
    r = re.compile("[$#]")
    return isinstance(val, str) and r.search(val) is None


def generate_config(secret=None):
    if not isinstance(secret, str):
        raise ValueError("secret string is required")
    return configTemplate.format(secret=secret)


def config_parser(content=None):
    rv = configparser.ConfigParser(
        interpolation=configparser.ExtendedInterpolation()
    )
    if content is not None:
        rv.read_string(content)
    return rv


def main(args):
    from cryptography.fernet import Fernet

    print("Generating fresh data...", file=sys.stderr)
    data = {
        "secret": next(
            i for i in itertools.repeat(Fernet.generate_key().decode("utf-8"))
            if check_safe_value(i)
        )
    }
    content = configTemplate.format(**data)
    if args.env:
        print("Formatting as environment variables:", file=sys.stderr)
        cfg = config_parser(content)
        for sec in cfg.sections():
            for key, val in cfg.items(sec):
                var = "_".join((sec.replace(".", "_").upper(), key.upper()))
                print("{var}={val}".format(var=var, val=val, file=sys.stdout))
    else:
        print("Formatting as a config file:", file=sys.stderr)
        print(content, file=sys.stdout)
        print("... Done.", file=sys.stderr)


def run():
    p = common.cli.parser(description=__doc__)
    p.add_argument(
        "--env", action="store_true", default=False,
        help="Generate the configuration as environment variables.")
    args = p.parse_args()
    rv = main(args)
    sys.exit(rv)

if __name__ == "__main__":
    run()
