import sys
from json import load

from sdc.rabbit.exceptions import QuarantinableError

from app.response_processor import ResponseProcessor


def reprocess(path_to_decrypted_json):

    with open(path_to_decrypted_json) as json_file:
        decrypted_json = load(json_file)

    try:
        response_processor = ResponseProcessor()
        response_processor.process(decrypted_json, tx_id=decrypted_json.get('tx_id'), decrypt=False)
        print("reprocess successful")

    except QuarantinableError as qe:
        print("reprocess failed!\n" + str(qe))


if __name__ == "__main__":
    path_to_json = sys.argv[1]
    reprocess(path_to_json)
