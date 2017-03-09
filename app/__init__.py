import logging
from app import settings

__version__ = "1.3.0"

logging.basicConfig(level=settings.LOGGING_LEVEL, format=settings.LOGGING_FORMAT)
