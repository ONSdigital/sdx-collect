build:
	pip3 install -r requirements.txt
	pip3 install -I git+git://github.com/ONSdigital/sdx-common.git@feature/pika-log-level

test:
	pip3 install -r test_requirements.txt
	flake8 --exclude ./lib/*
	python3 -m unittest tests/*.py
