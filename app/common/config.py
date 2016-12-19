#!/usr/bin/env python
#   coding: UTF-8

import configparser
import os.path
import re


def aggregate_options(cfg, name="default", keys=[]):
    """
    Create a keyword argument dictionary from the options in a configuration
    section. Override those values from the process environment.

    """
    if cfg.has_section(name):
        data = ((k, cfg.get(name, k)) for k in keys)
        rv = dict(pair for pair in data if pair[1] is not None)
    else:
        rv = {}

    variables = {
        k: "{0}_{1}".format(
            name.replace(".", "_").upper(), k.replace(".", "_").upper()
        )
        for k in keys
    }
    overlay = (
        (key, os.getenv(var))
        for key, var in variables.items()
    )
    rv.update(pair for pair in overlay if pair[1] is not None)
    return rv


def check_safe_value(val):
    r = re.compile("[$#]")
    return isinstance(val, str) and r.search(val) is None


def config_parser(content=None):
    rv = configparser.ConfigParser(
        interpolation=configparser.ExtendedInterpolation()
    )
    if content is not None:
        rv.read_string(content)
    return rv
