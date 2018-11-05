FROM onsdigital/flask-crypto-queue

RUN mkdir -p /app/logs

ENTRYPOINT ./startup.sh

COPY requirements.txt /requirements.txt
COPY Makefile /Makefile
RUN make build

COPY startup.sh /startup.sh
COPY app /app