import requests
from requests.packages.urllib3.util.retry import Retry
from requests.adapters import HTTPAdapter

__version__ = "3.19.2"

# Configure the number of retries attempted before failing call
session = requests.Session()
retries = Retry(total=5, backoff_factor=0.1)
session.mount('http://', HTTPAdapter(max_retries=retries))
session.mount('https://', HTTPAdapter(max_retries=retries))
