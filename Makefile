.PHONY: build test

build:
	pip3 install -r requirements.txt

test:
	pip3 install -r test_requirements.txt
	flake8 --exclude ./lib/*
	pytest -v --cov-report term-missing --cov app

