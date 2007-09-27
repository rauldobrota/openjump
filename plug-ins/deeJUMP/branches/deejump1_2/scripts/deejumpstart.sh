#!/bin/sh
cd /usr/local/deegree/deejump/
/usr/java/j2sdk1.4.2_05/bin/java -Xmx512M -Duser.language=en -Dlog4j.configuration=file:log4j.xml -jar deejump.jar -properties workbench-properties.xml -plug-in-directory "lib/ext"