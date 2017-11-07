.PHONY: build test

build:
	pipenv --three
	pipenv install --dev

test:
	pipenv run flake8 --exclude ./lib/*
	pipenv run pytest -v --cov app

