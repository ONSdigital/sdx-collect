FROM onsdigital/flask-crypto-queue

RUN apt-get update -y
RUN apt-get upgrade -y
RUN apt-get install -yq git gcc make build-essential python3-dev python3-reportlab

RUN mkdir -p /app/logs

COPY app /app
COPY startup.sh /startup.sh
COPY requirements.txt /app/requirements.txt

RUN apt-get install build-essential libssl-dev libffi-dev -y
RUN pip3 install --no-cache-dir -U -r /app/requirements.txt

ENTRYPOINT ./startup.sh
