FROM ubuntu:16.04

# Install essential packages
RUN apt-get update -y
RUN apt-get upgrade -y
RUN apt-get install -yq build-essential git python3 python3-dev python3-venv python3-pip libssl-dev

ADD app /app
ADD requirements.txt /requirements.txt
ADD startup.sh /startup.sh

RUN mkdir -p /app/logs

RUN pip3 install --no-cache-dir -U -I -r /requirements.txt

ENTRYPOINT ./startup.sh
