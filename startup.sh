#!/bin/bash

if [ -z ${LOG_LEVEL+x} ]; 
then 
	export LOG_LEVEL=INFO; 
	echo "LOG_LEVEL has been defaulted to '$LOG_LEVEL'"; 
else 
	echo "LOG_LEVEL is set to '$LOG_LEVEL'"; 
fi

if [ -z ${ROOT_LOG_LEVEL+x} ]; 
then 
	export ROOT_LOG_LEVEL=INFO; 
	echo "ROOT_LOG_LEVEL has been defaulted to '$ROOT_LOG_LEVEL'"; 
else 
	echo "ROOT_LOG_LEVEL is set to '$ROOT_LOG_LEVEL'"; 
fi

java -Xmx4094m \
          -Drestolino.packageprefix=com.github.onsdigital.perkin.api \
	  -Dlog.level=$LOG_LEVEL \
	  -Droot.log.level=$ROOT_LOG_LEVEL \
          -jar target/*-jar-with-dependencies.jar
