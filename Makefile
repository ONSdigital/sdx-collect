dev: check-env
	git clone https://github.com/ONSdigital/sdx-common.git
	pip3 uninstall -y ./sdx-common && pip3 install -I ./sdx-common
	rm -rf sdx-common
	pip3 install -r requirements.txt

build:
	pip3 install -r requirements.txt

test:
	pip3 install -r test_requirements.txt
	flake8 --exclude ./lib/*
	python3 -m unittest tests/*.py

check-env:
ifeq ($(SDX_HOME),)
	$(error SDX_HOME is not set)
endif
