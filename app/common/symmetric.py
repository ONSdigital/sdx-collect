#!/usr/bin/env python
#   coding: UTF-8

from cryptography.fernet import Fernet


def generate_key():
    return Fernet.generate_key()
