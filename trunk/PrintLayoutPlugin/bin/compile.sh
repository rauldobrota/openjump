#!/bin/bash

JUMPHOME=${JUMPHOME:-$HOME/prog/openjump/current}

PRINTHOME=${PRINTHOME:-$HOME/prog/PrintLayoutPlugin}

cp=$CLASSPATH

for i in $JUMPHOME/lib/*.jar $PRINTHOME/lib/*.jar ; do 
	cp=$cp:$i
done


CLASSPATH=$cp javac -d $PRINTHOME/classes "$@"
