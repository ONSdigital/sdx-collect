.PHONY: dev build clean test vendor check-env

dev: check-env
	cd .. && pip3 uninstall -y sdx-common && pip3 install -I ./sdx-common
	pip3 install -r requirements.txt

build:
	pip3 install -I -r requirements.txt
	git clone https://github.com/ONSdigital/sdx-common.git
	pip3 install ./sdx-common
	rm -rf sdx-common

clean:
	rm -rv ./vendor/*

test:
	pip3 install -r test_requirements.txt
	flake8 --exclude ./lib/*
	python3 -m unittest tests/*.py

vendor:
	mkdir -p vendor
	cp -v ../sdx-common/dist/* vendor/

check-env:
ifeq ($(SDX_HOME),)
	$(error SDX_HOME is not set)
endif
