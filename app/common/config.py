#!/usr/bin/env python
#   coding: UTF-8

import configparser
import itertools
import re
import sys

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


if __name__ == "__main__":
    from cryptography.fernet import Fernet

    print("Generating fresh config file...", file=sys.stderr)
    data = {
        "secret": next(
            i for i in itertools.repeat(Fernet.generate_key().decode("utf-8"))
            if check_safe_value(i)
        )
    }
    output = configTemplate.format(**data)
    print(output, file=sys.stdout)
    print("... Done.", file=sys.stderr)
