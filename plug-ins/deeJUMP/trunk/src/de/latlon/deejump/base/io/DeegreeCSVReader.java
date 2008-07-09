//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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
 Aennchenstr. 19
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

package de.latlon.deejump.base.io;

import static de.latlon.deejump.base.i18n.I18N.get;
import static de.latlon.deejump.wfs.data.JUMPFeatureFactory.createFromDeegreeFC;
import static java.lang.Integer.parseInt;
import static org.deegree.framework.log.LoggerFactory.getLogger;

import org.deegree.framework.log.ILogger;
import org.deegree.io.csv.CSVReader;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.JUMPReader;

/**
 * <code>DeegreeCSVReader</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DeegreeCSVReader implements JUMPReader {

    private static final ILogger LOG = getLogger( DeegreeCSVReader.class );

    public FeatureCollection read( DriverProperties dp )
                            throws Exception {
        String fileRoot = dp.getProperty( "File" );

        CSVReader reader = new CSVReader( ',', fileRoot );

        String wkt = dp.getProperty( "wkt" );
        if ( wkt != null ) {
            reader.setWKTColumn( parseInt( wkt ) );
        } else {
            reader.setPointColumns( parseInt( dp.getProperty( "xcol" ) ), parseInt( dp.getProperty( "ycol" ) ) );
        }

        try {
            return createFromDeegreeFC( reader.parseFeatureCollection() );
        } catch ( NumberFormatException nfe ) {
            LOG.logError( "Original exception", nfe );
            throw new Exception( get( "General.numberparsed" ) + " " + nfe.getMessage() );
        }
    }
}
