build:
	pip install -r requirements.txt

test:
	pip install -r test_requirements.txt
	flake8 --exclude ./lib/*
	python3 -m unittest tests/*.py
	python3 -m unittest app/common/test/*.py
