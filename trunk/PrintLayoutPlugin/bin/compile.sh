#!/bin/bash

JUMPHOME=${JUMPHOME:-$HOME/prog/openjump/current}

PRINTHOME=${PRINTHOME:-$HOME/prog/printlayout}

for i in $JUMPHOME/lib/*.jar $PRINTHOME/lib/*.jar ; do 
	CLASSPATH=$CLASSPATH:$i
done

CLASSPATH=$cp javac -d $PRINTHOME/classes "$@"
