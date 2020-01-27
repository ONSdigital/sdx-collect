### Unreleased

### 3.17.0 2020-01-27
  - Quarantine submissions and feedback if they have null characters (`\u0000`) in the text

### 3.16.1 2019-11-08
  - Add python 3.8 to travis builds

### 3.16.0 2019-09-04
  - Tidy function arguments and use fstrings
  - Update sdc-rabbit to 1.7.0 to fix reconnection issues
  - Update various dependencies

### 3.15.0 2019-08-13
  - Change name of SDX Downstream queue

### 3.14.0 2019-08-01
  - Reverted to default heartbeat

### 3.13.0 2019-06-20
  - Remove python 3.4 and 3.5 from travis builds
  - Add python 3.7 to travis builds
  - Upgrade packages, including sdc-rabbit, tornado and pika to allow upgrade to python 3.7

### 3.12.1 2019-05-14
  - Fix bug where tx_id from previous submission was bound to the logger before decryption was done
  - Update urllib3 to fix security issue

### 3.12.0 2019-03-25
  - Set RSI to go to DAP

### 3.11.1 2019-02-19
  - Update packages to fix security issue

### 3.11.0 2019-02-08
  - Set census surveys to go to DAP and be receipted

### 3.10.0 2018-11-13
  - Set D-Trades to go to DAP

### 3.9.1 2018-10-30
  - Fix bug where LMS feedback surveys were trying to be picked up by minifi
  - Add version log on startup

### 3.9.0 2018-10-22
  - Queue message to be consumed by minifi for LMS surveys

### 3.8.0 2018-08-15
  - Update dependencies and remove unused ones
  
### 3.7.0 2018-06-27
  - Remove second rabbit host.

### 3.6.0 2018-06-20
  - Support LMS test 1 by skipping downstream

### 3.5.0 2018-03-06
  - Send case_id to rrm-receipt queue if present in decrypted JSON
  - Import PrivatePublisher from app module rather than publisher module

### 3.4.0 2018-01-17
  - Add heartbeat interval to rabbit mq url

### 3.3.0 2017-11-21
  - Update default queue name
  - Remove sdx-collect requirement
  - Add Cloudfoundry deployment files

### 3.2.0 2017-11-01
  - Change to use pytest to improve test output and code coverage stats
  - Fixes incorrect logging if validation fails
  - Added RetryableError to remote_call so that HTTP errors give rise to rabbit NAKS and hence retry

### 3.1.0 2017-10-16
  - Hardcode unchanging variables in settings.py to make configuration management simpler
  - Add service config to config file

### 3.0.1 2017-09-25
  - Change queue name

### 3.0.0 2017-09-25
  - Publish all surveys to same downstream by removing cora specific queueing
  - Remove sdx common clone in Dockerfile
  - Send headers to RRM receipt publisher

### 2.0.0 2017-09-11
  - Ensure integrity and version of library dependencies
  - No longer process CTP submissions
  - Send response notifications

### 1.5.0 2017-07-25
  - Change all instances of ADD to COPY in Dockerfile
  - Remove use of SDX_HOME variable in makefile
  - Only receipt if the JSON data is valid
  - Integrate with sdc-rabbit library

### 1.4.1 2017-07-11
  - Fix #180 where message is not quarantined when a DecryptError is raised

### 1.4.0 2017-07-10
  - Adds a 'dev' build target for local dev work
  - Use common Pika log level setter
  - Pre validate survey ids so that invalid/empty strings aren't routed to census
  - Change logging messages to add the service called or returned from
  - Get tx_id from message header before decrypting
  - Add codacy badge
  - Correct license attribution
  - Import async_consumer from sdx-common
  - Updating date format in logs using sdx-common
  - Add support for codecov to see unit test coverage
  - Update and pin version of sdx-common to 0.7.0

### 1.3.1 2017-03-15
  - Add version number to log

### 1.3.0 2017-02-16
  - Add explicit message ack/nack
  - Add quarantine queue for bad decrypt messages
  - Add change log
  - Remove reject on max retries. Stops message being rejected if endpoint is down for prolonged period
  - Add queue name to log message
  - Add `PREFETCH=1` to rabbit config to address '104 Socket' errors
  - Update env vars for receipt queue names

### 1.2.0 2016-12-13
  - Add new queues for rrm and ctp receipting services

### 1.1.0 2016-11-28
  - Remove logging of failed data. Now sets flag and stores failed submission to DB.

### 1.0.1 2016-11-10
  - Remove sensitive data from logging

### 1.0.0 2016-08-09
  - Initial release
