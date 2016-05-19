[![Build Status](https://travis-ci.org/ONSdigital/perkin.svg?branch=master)](https://travis-ci.org/ONSdigital/perkin)

![Logo](http://www.80snostalgia.com/files/fluperkins.jpg)

# perkin
Perkin is a Questionnaire data transformer written in Java. It is a component of the Office of National Statistics (ONS) Survey Data Exchange (SDE) project which listens to a JMS queue for survey data.
On receipt it hands off to sdx-decrypt to decrypt the survey data, then transforms it into downstream formats and sends the data via ftp for processing by the ONS.

## Installation

Using java 8 and maven

    $ mvn clean install

## Usage

To start perkin, just run the server:

    $ java -jar target/Perkin*-dependencies.jar

perkin exposes the following endpoints:

| endpoint                      | purpose                                                |
|-------------------------------|--------------------------------------------------------|
| http://localhost:8080/version | shows version information                              |
| http://localhost:8080/env     | shows configuration                                    |
| http://localhost:8080/health  | shows health information - is the queue connection ok? |
| http://localhost:8080/metrics | shows statistics of successful / failed messages       |
| http://localhost:8080/trace   | shows recent requests                                  |

## Lombok

Perkin uses Lombok, so to see method documentation, you'll need to install the lombok plugin into eclipse, intellij etc
https://projectlombok.org/download.html

