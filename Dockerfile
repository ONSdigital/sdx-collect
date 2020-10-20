FROM onsdigital/flask-crypto-queue

COPY app /app

RUN mkdir -p /app/logs

COPY requirements.txt /app/requirements.txt
RUN pip3 install --no-cache-dir -U -r /app/requirements.txt

CMD ["python", "./app/main.py"]