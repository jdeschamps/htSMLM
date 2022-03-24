#!/bin/bash

# get MM2 home
MM2_HOME=$1

EMU_VERSION="1.1"


if ! [ -d "$MM2_HOME" ]; then
	echo "[$MM2_HOME] is not a directory."
else
	# tests if mvn is installed
	command -v mvn >/dev/null 2>&1 || { echo >&2 "Failed to call mvn, are you sure Maven is installed?";}
	
	# tests if the Micro-Manager jars are present and deploy them
	MMJ="$MM2_HOME\plugins\Micro-Manager\MMJ_.jar"
	MMAcqEngine="$MM2_HOME\plugins\Micro-Manager\MMAcqEngine.jar"
	MMCoreJ="$MM2_HOME\plugins\Micro-Manager\MMCoreJ.jar"
	EMU="$MM2_HOME\mmplugins\Emu.jar"
	MM2_PLUGINS_HOME="$MM2_HOME\mmplugins"
	
	if [ -f "$MMJ" ] && [ -f "$MMAcqEngine" ] && [ -f "$MMCoreJ" ] && [ -f "$EMU" ]; then
		# deploy MM2 jars
		mvn install:install-file -Dfile="$MMJ" -DgroupId=org.micromanager  -DartifactId=MMJ_ -Dversion=2.0.0-SNAPSHOT -Dpackaging=jar
		mvn install:install-file -Dfile="$MMAcqEngine" -DgroupId=org.micromanager  -DartifactId=MMAcqEngine -Dversion=2.0.0-SNAPSHOT -Dpackaging=jar
		mvn install:install-file -Dfile="$MMCoreJ" -DgroupId=org.micromanager  -DartifactId=MMCoreJ -Dversion=2.0.0-SNAPSHOT -Dpackaging=jar
		mvn install:install-file -Dfile="$EMU" -DgroupId=de.embl.rieslab  -DartifactId=EMU -Dversion=$EMU_VERSION -Dpackaging=jar
		
		# compile project
		mvn clean package shade:shade -Dmaven.test.skip=true
	
		# test if the EMU folder exist, otherwise create it
		MM2_EMU="$MM2_HOME\EMU"
		mkdir -p "$MM2_EMU"
	
		# deploy to MM2
		cp "target\htsmlm-1.0-SNAPSHOT.jar" "$MM2_HOME\EMU\htsmlm-1.0.jar"
	else
		echo "Could not find MMJ_.jar, MMAcqEngine.jar, MMCoreJ.jar or Emu.jar. Did you input the correct directory?"
	fi
fi
