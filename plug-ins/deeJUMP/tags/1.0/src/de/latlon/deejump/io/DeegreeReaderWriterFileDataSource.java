//$Header$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2005 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/exse/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstra�e 19
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

package de.latlon.deejump.io;

import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;

/**
 * ...
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author: taddei $
 * 
 * @version 2.0, $Revision: 147 $, $Date: 2007-05-08 11:28:04 +0200 (Di, 08 Mai 2007) $
 * 
 * @since 2.0
 */

public class DeegreeReaderWriterFileDataSource {

    /**
     * <code>DeeShapefile</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author:$
     * 
     * @version $Revision:$, $Date:$
     */
    public static class DeeShapefile extends StandardReaderWriterFileDataSource {
        /**
         * 
         */
        public DeeShapefile() {
            super( new DeegreeShapeFileReader(), new DeegreeShapeFileWriter(), new String[] { "shp" } );
        }
    }

    /**
     * <code>DeeGMLFile</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author:$
     * 
     * @version $Revision:$, $Date:$
     */
    public static class DeeGMLFile extends StandardReaderWriterFileDataSource {
        /**
         * 
         */
        public DeeGMLFile() {
            super( new DeegreeGMLReader(), new DeegreeGMLFileWriter(), new String[] { "gml", "xml" } );
        }
    }

    /**
     * <code>DeeMapInfoFile</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author:$
     * 
     * @version $Revision:$, $Date:$
     */
    public static class DeeMapInfoFile extends StandardReaderWriterFileDataSource {

        /**
         * 
         */
        public DeeMapInfoFile() {
            super( new DeegreeMapInfoReader(), null, new String[] { "mid", "mif" } );
        }

    }

}
