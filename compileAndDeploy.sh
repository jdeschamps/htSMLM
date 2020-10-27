#!/bin/bash

# get MM2 home
MM2_HOME=$1
if ! [ -d "$MM2_HOME" ]; then
	echo "[$MM2_HOME] is not a directory."
else
	# compile project
	mvn clean package shade:shade
	
	# test if the EMU folder exist, otherwise create it
	MM2_EMU="$MM2_HOME\EMU"
	mkdir -p "$MM2_EMU"
	
	# deploy to MM2
	cp "target\htsmlm-1.0-SNAPSHOT.jar" "$MM2_HOME\EMU\htsmlm-1.0.jar"
fi
