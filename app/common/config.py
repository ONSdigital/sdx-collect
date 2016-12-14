#!/usr/bin/env python
#   coding: UTF-8

configTemplate = """
[sdx.collect]
secret = {secret}

[sdx.receipt.ctp]
secret = ${{sdx.collect:secret}}

[sdx.receipt.rrm]
secret = ${{sdx.collect:secret}}
"""


def check_safe_value(val):
    return True

if __name__ == "__main__":
    import symmetric

    data = {"secret": symmetric.generate_key()}
    output = configTemplate.format(**data)
    print(output)
