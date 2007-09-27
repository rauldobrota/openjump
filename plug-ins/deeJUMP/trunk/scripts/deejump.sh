#!/bin/sh

dirs="$HOME/workspace/openjump $HOME/workspace/deejump $HOME/workspace/base"
clspth=
  for dir in $dirs
   do
	   if( test -d $dir/lib/ ) then
       for file in `find $dir/lib/ -name \*jar`
			 do
        clspth=$clspth:$file
       done
		 fi
	   if( test -d $dir/dist/ ) then
       for file in `find $dir/dist/ -name \*jar`
			 do
        clspth=$clspth:$file
       done
  	 fi
  done

$JAVA_HOME/bin/java -cp $clspth -Xmx512M de.latlon.deejump.DeeJUMPWorkbench

