#!/bin/sh
#To make this script executable, use chmod a+x JUMPWorkbench-unix.sh
LIB=../lib
CLASSPATH=$LIB/bsh-2.0b4.jar:$LIB/Jama-1.0.1.jar:$LIB/jdom.jar:$LIB/jts-1.6.jar:$LIB/jump-workbench-@VERSION@.jar:$LIB/jump-api-@VERSION@.jar:$LIB/xercesImpl.jar:$LIB/xml-apis.jar:$LIB/log4j-1.2.8.jar:$LIB/looks-1.3.1.jar
java -Dlog4j.configuration=file:./log4j.xml -Xms256M -Xmx256M -cp $CLASSPATH com.vividsolutions.jump.workbench.JUMPWorkbench -properties workbench-properties.xml -plug-in-directory $LIB/ext
