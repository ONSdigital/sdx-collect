# sdx-collect

[![Build Status](https://github.com/ONSdigital/sdx-collect/workflows/Build/badge.svg)](https://github.com/ONSdigital/sdx-collect) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/858afbc345f64b288b8aef4c6600f82d)](https://www.codacy.com/app/ons-sdc/sdx-collect?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/sdx-collect&amp;utm_campaign=Badge_Grade) [![codecov](https://codecov.io/gh/ONSdigital/sdx-collect/branch/master/graph/badge.svg)](https://codecov.io/gh/ONSdigital/sdx-collect)

``sdx-collect`` is a component of the Office for National Statistics (ONS) Survey Data Exchange (SDX) project which listens to a queue for survey data from the eQ Survey Runner.

On receipt it hands off to sdx-decrypt to decrypt the survey data, validates the result with sdx-validate and then stores the json by calling sdx-store. Once complete the survey is receipted
via the appropriate receipting service.

## Installation
This application presently installs required packages from requirements files:
- `requirements.txt`: packages for the application, with hashes for all packages: see https://pypi.org/project/hashin/   
- `test-requirements.txt`: packages for testing and linting

It's also best to use `pyenv` and `pyenv-virtualenv`, to build in a virtual environment with the currently recommended version of Python.  To install these, see:
- https://github.com/pyenv/pyenv
- https://github.com/pyenv/pyenv-virtualenv
- (Note that the homebrew version of `pyenv` is easiest to install, but can lag behind the latest release of Python.)

### Getting started
Once your virtual environment is set, install the requirements:
```shell
$ make build
```

To test, first run `make build` as above, then run:
```shell
$ make test
```

It's also possible to install within a container using docker. From the sdx-collect directory:
```shell
$ docker build -t sdx-collect .
```
## Configuration

The following envioronment variables can be set:

| Environment variable      | Default                               | Description
|---------------------------|---------------------------------------|---------------
| SDX_DECRYPT_URL           | ``http://sdx-decrypt:5000/decrypt``   | URL of the ``sdx-decrypt`` service
| SDX_VALIDATE_URL          | ``http://sdx-validate:5000/validate`` | URL of the ``sdx-validate`` service
| SDX_RESPONSES_URL         | ``http://sdx-store:5000/responses``   | URL of the ``sdx-store`` service
| SDX_COLLECT_SECRET        | _none_                                | Key for decrypting messages from queue, must be the same as used for ``sdx-receipt``
| DAP_SOURCE_NAME           | ``sdx_development``                   | Name of the environment the DAP message was created in
| RABBIT_SURVEY_QUEUE       | ``survey``                            | Name of incoming queue
| RABBIT_QUARANTINE_QUEUE   | ``survey_quarantine``                 | Name of queue to quarantine bad decrypt messages to
| RABBIT_EXCHANGE           | ``message``                           | Exchange for incoming queue
| RABBIT_RRM_RECEIPT_QUEUE  | ``rrm_receipt``                       | Name of rrm receipt service queue

### License

Copyright (c) 2016 Crown Copyright (Office for National Statistics)

Released under MIT license, see [LICENSE](LICENSE) for details.
