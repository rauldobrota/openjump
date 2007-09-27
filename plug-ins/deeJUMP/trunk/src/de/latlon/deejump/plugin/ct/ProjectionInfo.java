//$Header$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2006 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstraﬂe 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 
 ---------------------------------------------------------------------------*/

package de.latlon.deejump.plugin.ct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author: taddei $
 *
 * @version $Revision: 148 $, $Date: 2007-05-08 11:39:33 +0200 (Di, 08 Mai 2007) $
 */

public class ProjectionInfo {

    /**
     * @param args
     */
    public static void main( String[] args ) {
       
        String PROJ = System.getenv( "PROJ_LIB" );
        System.out.println( PROJ );
        
        File f = new File( PROJ );
        
        File[] files = f.listFiles();
        for ( int i = 0; i < files.length; i++ ) {
            if ( files[i].isFile() ){
                // and no jar, no so no dll
                System.out.println( files[i].getAbsolutePath() );
            }
        }
        
    }

    private ProjectionMetadata[] readContents( String filename ) 
        throws IOException{
        
        List mdList = new ArrayList ( 1000 );
        
        BufferedReader br = new BufferedReader( new FileReader( filename ) );
        StringBuffer sb = new StringBuffer( 50000 );
        String s = null;
        while (( s = br.readLine() ) != null) {
            
//            have t read 2 lines first...
            
            sb.append( s );
        }
        s = sb.toString();
        br.close();
        
        ProjectionMetadata[] metadata = null;
        return metadata;
    }
    
    static class  ProjectionMetadata {
        
        private final String code;
        private final String desc;

        ProjectionMetadata( String code, String desc) {
            this.code = code;
            this.desc = desc;
            
        }

        String getCode() {
            return code;
        }

        String getDesc() {
            return desc;
        }
        
    }
    
}


/* ********************************************************************
Changes to this class. What the people have been up to:
$Log$
Revision 1.2  2007/05/08 09:35:55  taddei
deejump final (UT)

********************************************************************** */