OpenJUMP readme file
--------------------
Version 1.2 D (pre-release)

August 16th, 2007

Contents
--------
1. License
2. Installation instructions
3. Running OpenJUMP
4. Support
5. OpenJUMP history
6. Credits


1. License
----------
OpenJUMP is distributed under the GPL license. A description of this license
can be found in the "gpl.txt" file on the same location than this readme file.
OpenJUMP uses the BATIK libraries to write svg format. The BATIK libraries are
used under the terms of the APACHE license, which can be found in the "apache.txt" 
file or on www.apache.org.


2. Installation instructions
----------------------------
The platform-independent version of OpenJUMP comes under the form of a compressed archive.
To install, decompress the archive in your hard drive, for example into c:/OpenJUMP
You will see the following folder structure:
c:/OpenJUMP/
c:/OpenJUMP/bin
c:/OpenJUMP/lib


3. Running OpenJUMP
-------------------
Run the startup scripts contained in the /bin folder:
- For windows, double-click on "openjump.bat"
- For Linux/Unix, launch openjump-unix.sh or openjump.sh
- For Mac, launch openjump.sh
- an improved script for unix exists: openjump-unix2.sh

Further notes can be found on our wiki: www.openjump.org

Users of looks extension should place all the jar files from looks-extension 
directly into /lib/ext.


Startup options
-----------------
Several startup options are available, either for the Java Virtual Machine, or for the
OpenJUMP core. To change them, edit the startup script
accordingly, editing the line beginning by "start javaw".

Java VM options (a complete list can be found in the Java VM documentation)
-Xms defines the allocated memory for the virtual machine at startup. Example: -Xms256M
 will allocate 256M of memory for OpenJUMP
-Xmx defines the maximum allocated memory for the virtual machine. Example: -Xmx256M
-Dproperty=value set a system property. For the moment, these properties are used:
  -Dswing.defaultlaf  for defining the OpenJUMP Look and Feel. Several possibilities:
     -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel for the Metal L&F
     -Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel for the Windows L&F
  -Dlog4j.configuration for defining the configuration file for Log4j. Normally:
     Dlog4j.configuration=file:./log4j.xml
     
OpenJUMP command lines
-properties filename : specifies the name of the file where OpenJUMP properties are stored.
 Default is -properties workbench-properties.xml
-plug-in-directory path : defines the location of the plugin directory. 
 Default is %LIB%/ext where %LIB% is defined earlier in the startup script.
-i18n locale : defines the locale (language, etc.) used by OpenJUMP. For example:
  - For starting OpenJUMP in French: use -i18n fr
  - Other languages available: de (german), es (spanish), pt_BR (brazilian portuguese)
  - Default is english if the specified language is not implemented.

  
4. Support
----------
General questions regarding OpenJUMP can be found in:
- www.jump-project.org  the original JUMP site
- www.openjump.org the OpenJUMP home
- jump-pilot.sourceforge.net the old OpenJUMP developper site


5. OpenJUMP history
-------------------
OpenJUMP is a "fork" of the JUMP "Java Unified Mapping Platform" software, developed
by Vividsolutions and released in 2003.
During 2004, some enthusiastic developers joined together to enhance further the 
features of JUMP. They launched an independent development branch called OpenJUMP.
This name gives credit to the original JUMP development, and at the same time 
describes the particularity of this project to be fully open to anyone wanting
to contribute.
Since May 2005 a complete development source is available at:
www.sourceforge.net/projects/jump-pilot
And a website for OpenJUMP is under construction at: www.openjump.org


6. Credits
----------
Many thanks to all the contributors of OpenJUMP for their time and efforts:

Original development team of JUMP was:
- Martin Davis, Jon Aquino, Alan Chang from Vividsolutions (www.vividsolutions.com)
- David Blasby and Paul Ramsey from Refractions Research Inc (www.refractions.net) 

OpenJUMP regular (*and past) contributors are (non exaustive list!):
- Jonathan Aquino*, 
- Landon Blake (Sunburned Surveyor),
- Erwan Bocher,
- Joe Desbonet,
- Michael Michaud,
- Stefan Steiniger,
- Ugo Taddei, 
- Uwe Dall�ge,
- Andreas Schmitz
- Stephan Holl
- Paul Austin
- Martin Davis
- Sascha Teichmann

* past contributors
- Axel Orth*,
- Ole Rahn*,
- Basile Chandesris*,
- Ezequias Rodrigues da Rocha*,
- Steve Tanner*,


Projects and Companies
- Larry Becker and Robert Littlefield (SkyJUMP team)
  partly at Integrated Systems Analysts, Inc.
  for providing their Jump ISA tools code and numerous other improvements
- Pirol Project from University of Applied Sciences Osnabr�ck
  for providing the attribute editor
  (contact: Malte Weller)
- Lat/Lon GmbH (deeJUMP team)
  for providing some plugins and functionality (i.e. WFS and WMS Plugins)
  contact: Stephan Holl/Markus M�ller/Ugo Taddei
- VividSolutions Inc. 
  for support and answering the never ending stream of questions, especially:
  Martin Davis (now at Refractions Inc.)
  David Zwiers
- Intevation GmbH
  for collaborative PlugIn development (Print Layout PlugIn)
  contact: Jan Oliver Wagner

Translation contributors are:
- French: Basile Chandesris, Erwan Bocher, Steve Tanner, Micha�l Michaud
- Spanish: Steve Tanner, Fco Lavin, Nacho Uve
- German: Florian Rengers, Stefan Steiniger
- Portuguese (brazilian): Ezequias Rodrigues da Rocha, Cristiano das Neves Almeida
- Finnish: Jukka Rahkonnen
- English: Landon Blake

others:
- L. Paul Chew for providing the Delauney triangulation algorithm to create the Voronoi edges

