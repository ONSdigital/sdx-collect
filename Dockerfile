FROM onsdigital/flask-crypto

ADD app /app
ADD requirements.txt /requirements.txt
ADD startup.sh /startup.sh

RUN mkdir -p /app/logs

RUN pip3 install --no-cache-dir -U -I -r /requirements.txt

ENTRYPOINT ./startup.sh
