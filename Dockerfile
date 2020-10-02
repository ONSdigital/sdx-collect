FROM onsdigital/flask-crypto-queue

RUN mkdir -p /app/logs

COPY requirements.txt /requirements.txt
COPY Makefile /Makefile
RUN make build

COPY app /app

CMD ["python", "./main.py"]