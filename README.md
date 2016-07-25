# sdx-collect

[![Build Status](https://travis-ci.org/ONSdigital/sdx-collect.svg?branch=python-consumer)](https://travis-ci.org/ONSdigital/sdx-collect)

The sdx-collect app is a component of the Office of National Statistics (ONS) Survey Data Exchange (SDX) project which listens to a queue for survey data.
On receipt it hands off to sdx-decrypt to decrypt the survey data, validates the result with sdx-validate and then stores the json by calling sdx-store.

## Configuration

The following envioronment variables can be set:

`SDX_DECRYPT_URL` - The URL of the sdx-decrypt service, defaults to http://sdx-decrypt:5000/decrypt

`SDX_VALIDATE_URL` - The URL of the sdx-validate service, defaults to http://sdx-validate:5000/validate

`SDX_STORE_URL` - The URL of the sdx-store service, defaults to http://sdx-store:5000/responses
