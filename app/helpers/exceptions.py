class BadMessageError(Exception):
    # A bad message is broken in some way that will never be accepted by
    # the endpoing and as such should be rejected (it will still be logged
    # and stored so no data is lost)
    pass


class RetryableError(Exception):
    # A retryable error is apparently transient and may be due to temporary
    # network issues or misconfiguration, but the message is valid and should
    # be retried
    pass


class DecryptError(Exception):
    # Can't even decrypt the message. May be corrupt or keys may be out of step.
    pass
