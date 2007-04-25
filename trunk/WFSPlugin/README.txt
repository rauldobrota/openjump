WFSPlugin - a plugin to retrieve WFS-layer version 1.0
------------------------------------------------------

This plugin offers the ability to include WFS-layer into OpenJump.
OGC WFS version 1.0 is currently supported with this plugin.

I - Install

  a - get the sources

      anonymous CVS access:

        cvs -d:pserver:anonymous@jump-pilot.cvs.sourceforge.net:/cvsroot/jump-pilot login

        cvs -z3
        -d:pserver:anonymous@jump-pilot.cvs.sourceforge.net:/cvsroot/jump-pilot co -P WFSPlugin


      developer CVS access:

        export CVS_RSH=ssh

        cvs -z3
        -d:ext:developername@jump-pilot.cvs.sourceforge.net:/cvsroot/jump-pilot co -P WFSPlugin

        (where developername is the respective login name)

