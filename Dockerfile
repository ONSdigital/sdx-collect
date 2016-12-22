FROM onsdigital/flask-crypto-queue

ADD app /app
ADD requirements.txt /requirements.txt
ADD startup.sh /startup.sh

RUN mkdir -p /app/logs

ENTRYPOINT ./startup.sh
