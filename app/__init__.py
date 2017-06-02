import logging
from app import settings

__version__ = "1.3.1"

logging.basicConfig(level=settings.LOGGING_LEVEL, format=settings.LOGGING_FORMAT, datefmt=settings.LOGGING_DATE_FORMAT)
