#!/usr/bin/env python
#   coding: UTF-8

import itertools
import re

configTemplate = """
[sdx.collect]
secret = {secret}

[sdx.receipt.ctp]
secret = ${{sdx.collect:secret}}

[sdx.receipt.rrm]
secret = ${{sdx.collect:secret}}
"""


def check_safe_value(val):
    r = re.compile("[$#]")
    return isinstance(val, str) and r.search(val) is None

if __name__ == "__main__":
    import symmetric

    data = {
        "secret": next(
            i for i in itertools.repeat(symmetric.generate_key().decode("utf-8"))
            if check_safe_value(i)
        )
    }
    output = configTemplate.format(**data)
    print(output)
