[![Build Status](https://travis-ci.org/ONSdigital/sdx-collect.svg?branch=master)](https://travis-ci.org/ONSdigital/sdx-collect)

![Logo](http://www.80snostalgia.com/files/fluperkins.jpg)

# sdx-collect
sdx-collect is part of the Questionnaire data transformer written in Java. It is a component of the Office of National Statistics (ONS) Survey Data Exchange (SDX) project which listens to a JMS queue for survey data.
On receipt it hands off to sdx-decrypt to decrypt the survey data, then stores the json by calling sdx-store.

## Installation

Using java 8 and maven

    $ mvn clean install

## Usage

To start sdx-collect, just run the server:

    $ java -jar target/sdx-collect*-dependencies.jar

sdx-collect exposes the following endpoints:

| endpoint                      | purpose                                                |
|-------------------------------|--------------------------------------------------------|
| http://localhost:8080/version | shows version information                              |
| http://localhost:8080/env     | shows configuration                                    |
| http://localhost:8080/health  | shows health information - is the queue connection ok? |
| http://localhost:8080/metrics | shows statistics of successful / failed messages       |
| http://localhost:8080/trace   | shows recent requests                                  |

## Lombok

sdx-collect uses Lombok, so to see method documentation, you'll need to install the lombok plugin into eclipse, intellij etc
https://projectlombok.org/download.html

## Example

Here is an example of survey data to encrypt and place onto the queue

```
{
  "type": "uk.gov.ons.edc.eq:surveyresponse",
  "version": "0.0.1",
  "origin": "uk.gov.ons.edc.eq",
  "survey_id": "023",
  "collection": {
    "exercise_sid": "hfjdskf",
    "instrument_id": "0203",
    "period": "1604"
  },
  "submitted_at": "2016-03-12T13:01:26Z",
  "metadata": {
    "user_id": "789473423",
    "ru_ref": "12345678901A"
  },
  "data": {
    "11": "1/4/2016",
    "12": "31/10/2016",
    "20": "1800000",
    "21": "60000",
    "22": "705000",
    "23": "900",
    "24": "74",
    "25": "50",
    "26": "100",
    "146": "some comment"
  }
}
```

Other components (sdx-transform-cs) generate the downstream files.
Examples of the files produced are in the src/test/resources folder

