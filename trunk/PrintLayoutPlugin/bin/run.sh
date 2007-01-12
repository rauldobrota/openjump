#!/bin/bash

WHAT="$1"

WHAT=${WHAT:-com.vividsolutions.jump.workbench.JUMPWorkbench}

JUMPDIST=${JUMPDIST:-$HOME/prog/openjump/current}
JUMPHOME=${JUMPHOME:-$HOME/prog/openjump/openjump}

PRINTHOME=${PRINTHOME:-$HOME/prog/PrintLayoutPlugin}

LOG=$JUMPDIST/bin/log4j.xml
PROP=$PRINTHOME/etc/workbench-properties.xml

for i in $JUMPDIST/lib/*.jar $PRINTHOME/lib/*.jar ; do 
	CLASSPATH=$CLASSPATH:$i
done

CLASSPATH=$CLASSPATH:$PRINTHOME/classes

shift

java -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel \
     -Dlog4j.configuration=file:$LOG \
		 -Xms256M -Xmx256M \
		 -cp $CLASSPATH \
		 $WHAT \
		 -properties $PROP \
		 -plug-in-directory $JUMPDIST/lib/ext \
		 "$@"
