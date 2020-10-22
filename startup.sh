#!/bin/bash

export SDX_VALIDATE_URL=http://$VALIDATE_SERVICE_HOST:$VALIDATE_SERVICE_PORT/validate

python -m app.main
