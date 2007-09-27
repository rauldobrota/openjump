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

package de.latlon.deejump.io;

import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;


/**
 * ... 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author: ut $
 * 
 * @version 2.0, $Revision: 105 $, $Date: 2006-03-08 10:44:08 +0100 (Mi, 08 M√§r 2006) $
 * 
 * @since 2.0
 */

public class DeegreeReaderWriterFileDataSource {

	
	public static class DeeShapefile extends StandardReaderWriterFileDataSource {
		public DeeShapefile() {
			super(new DeegreeShapeFileReader(), new DeegreeShapeFileWriter(),
					new String[] { "shp" });
		}
	}
	
	public static class DeeGMLFile extends StandardReaderWriterFileDataSource {
		public DeeGMLFile() {
			super(new DeegreeGMLReader(), new DeegreeGMLFileWriter(),
					new String[] { "gml", "xml" });
		}
	}
	
}


/* ********************************************************************
Changes to this class. What the people have been up to:
$Log$
Revision 1.1  2006/03/08 09:36:02  ut
major refactoring


********************************************************************** */