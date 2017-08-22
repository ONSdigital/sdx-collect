class DecryptError(Exception):
    """
    Can't even decrypt the message. May be corrupt or keys may be out of step.
    """
    pass


class ClientError(Exception):
    """
    400 range error returned.
    """
    pass
