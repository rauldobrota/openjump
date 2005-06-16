set LIB=../lib
set CLASSPATH=%LIB%/bsh-2.0b1.jar;%LIB%/Jama-1.0.1.jar;%LIB%/jdom.jar;%LIB%/jts-1.4.1-RC1.jar;%LIB%/jump-workbench-@VERSION@.jar;%LIB%/jump-api-@VERSION@.jar;%LIB%/xercesImpl.jar;%LIB%/xml-apis.jar
REM Add extension directory to path, so extensions can put DLL's there [Jon Aquino 2005-03-18]
set PATH=%PATH%;%LIB%/ext
start javaw -Xms256M -Xmx256M -cp %CLASSPATH% com.vividsolutions.jump.workbench.JUMPWorkbench -properties workbench-properties.xml -plug-in-directory %LIB%/ext
