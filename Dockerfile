FROM onsdigital/flask-crypto-queue

ADD app /app
ADD startup.sh /startup.sh

RUN mkdir -p /app/logs

ENTRYPOINT ./startup.sh
