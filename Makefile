.PHONY: build clean test

build: clean
	git clone -b 0.7.0 https://github.com/ONSdigital/sdx-common.git
	pip3 install ./sdx-common
	rm -rf sdx-common
	git clone -b 0.1.0 https://github.com/ONSdigital/sdc-rabbit-python.git
	pip3 install ./sdc-rabbit-python
	pip3 install -r requirements.txt
	rm -rf ./sdc-rabbit-python

dev:
	cd .. && pip3 uninstall -y sdx-common && pip3 install -I ./sdx-common
	pip3 install -r requirements.txt

test:
	pip3 install -r test_requirements.txt
	flake8 --exclude ./lib/*
	python3 -m unittest tests/*.py

clean:
	rm -rf ./sdx-common
	rm -rf ./sdc-rabbit-python
