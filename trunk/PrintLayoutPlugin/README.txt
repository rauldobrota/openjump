Print and layout plugin for OpenJump
------------------------------------

This plugin offers the ability to layout OpenJump maps on a virtual 
paper sheet, adding features as boxes, images, legends and so on.
Afterwards it is possible to send it to a printer, export it as 
SVG or PDF. 


I - Install

  a - get the sources

      anonymous CVS access:

        cvs -d:pserver:anonymous@jump-pilot.cvs.sourceforge.net:/cvsroot/jump-pilot login

        cvs -z3 -d:pserver:anonymous@jump-pilot.cvs.sourceforge.net:/cvsroot/jump-pilot co -P PrintLayoutLayoutPlugin


      developer CVS access:

        export CVS_RSH=ssh

        cvs -z3 -d:ext:developername@jump-pilot.cvs.sourceforge.net:/cvsroot/jump-pilot co -P PrintLayoutLayoutPlugin

        (with developername the respective login name)


   b - build

       in a Unix (alike) environment:

         open the file 'etc/build.properties' with your text editor
         an replace the path in the line

           openjumplib=${user.home}/prog/openjump/current

         with the path to your OpenJump installation (the directory
         containing 'lib' and 'bin' directories of OpenJump)

         build the plugin:

         cd etc
         ant dist

         That should create a dist dir containing the jar file of
         the plugin e.g:

         cd ..
         ls dist

         printlayoutplugin-20070117.jar

    c - using the plugin

        there a actually two ways 

        - for print/layout plugin development (unix alike)

          export JUMPDIST=<where you installed OpenJump>
          export PRINTHOME=<where you checked out the plugin sources>

          cd $PRINTHOME

          bin/run.sh

        - the cooked jar version (unix alike)

          * First build the jar as descripted in b.

          * copy the jar into the the lib/ext directory of
            your OpenJump installation.

          * !!! LIBARY DEPENDENCIES !!!

            Please read the file INSTALL.txt about the jar libraries
            you need to install additionally.
