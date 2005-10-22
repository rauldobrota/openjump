set LIB=../lib
set CLASSPATH=%LIB%/bsh-2.0b4.jar;%LIB%/Buoy.jar;%LIB%/Jama-1.0.1.jar;%LIB%/jdom.jar;%LIB%/jts-1.6.jar;%LIB%/jump-workbench-@VERSION@.jar;%LIB%/jump-api-@VERSION@.jar;%LIB%/xercesImpl.jar;%LIB%/xml-apis.jar;%LIB%/log4j-1.2.8.jar;%LIB%/batik/batik-awt-util.jar;%LIB%/batik/batik-dom.jar;%LIB%/batik/batik-svggen.jar;%LIB%/batik/batik-util.jar;%LIB%/batik/batik-xml.jar
REM Add extension directory to path, so extensions can put DLL's there [Jon Aquino 2005-03-18]
set PATH=%PATH%;%LIB%/ext
start javaw -Dlog4j.configuration=file:./log4j.xml -Xms256M -Xmx256M -cp %CLASSPATH% com.vividsolutions.jump.workbench.JUMPWorkbench -properties workbench-properties.xml -plug-in-directory %LIB%/ext
