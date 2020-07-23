"""
This script allows you to manually trigger the processing
of submissions that have already been decrypted.
Requires a file path to the decrypted json as an argument.
"""

import sys
from json import load

from sdc.rabbit.exceptions import QuarantinableError

from app.response_processor import ResponseProcessor


def reprocess(path_to_decrypted_json):
    """Restarts processing after the decryption phase

    Parameters
    ----------
    path_to_decrypted_json : str
        The file location of the decrypted survey submission as json
    """

    with open(path_to_decrypted_json) as json_file:
        decrypted_json = load(json_file)

    try:
        response_processor = ResponseProcessor()
        response_processor.process(decrypted_json, tx_id=decrypted_json.get('tx_id'), decrypt=False)
        print("reprocess successful")

    except QuarantinableError as qe:
        print("reprocess failed!\n" + str(qe))


if __name__ == "__main__":
    # requires a filepath as the one and only argument
    path_to_json = sys.argv[1]
    reprocess(path_to_json)
