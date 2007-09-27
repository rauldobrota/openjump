rem after java -DproxySet=true -DproxyHost=xxxxx.xxx.xx -DproxyPort=80
set PROJ_LIB=./lib/native/proj4

start javaw.exe -Xms128M -Xmx256M -Duser.language=en -Dlog4j.configuration=file:log4j.xml -Djava.library.path=./lib/native/proj4/ -jar deejump.jar -properties workbench-properties.xml -plug-in-directory "lib\ext"