FROM onsdigital/flask-crypto-queue


COPY . /app
RUN mkdir -p /app/logs

RUN pip3 install --no-cache-dir -U -r /app/requirements.txt

WORKDIR /app

ENTRYPOINT ["./startup.sh"]