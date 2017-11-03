FROM onsdigital/flask-crypto-queue

RUN mkdir -p /app/logs

COPY app /app
COPY startup.sh /startup.sh
COPY requirements.txt /requirements.txt
COPY Makefile /Makefile

RUN make build

ENTRYPOINT ./startup.sh
