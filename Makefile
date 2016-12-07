build:
	pip install -r requirements.txt

test: build
	pip install -r test_requirements.txt
	flake8 --exclude ./lib/*
	python3 -m unittest tests/*.py