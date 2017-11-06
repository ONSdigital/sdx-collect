### Unreleased
  - Update default queue name

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
