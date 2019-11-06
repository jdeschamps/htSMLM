#!/bin/bash

# get MM2 home
MM2_HOME=$1
if ! [ -d "$MM2_HOME" ]; then
	echo "[$MM2_HOME] is not a directory."
else
	# compile project
	mvn clean package shade:shade
	
	# deploy to MM2
	cp -i "target\htSMLM-1.0-SNAPSHOTs.jar" "$MM2_HOME\EMU\htSMLM-1.0-SNAPSHOTs.jar"
fi
