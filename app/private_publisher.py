#!/usr/bin/env python
#   coding: UTF-8

from queue_publisher import QueuePublisher


class PrivatePublisher(QueuePublisher):

    def publish_message(self, message, content_type=None, headers=None):
        raise NotImplementedError
