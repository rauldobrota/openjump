package de.latlon.deejump.base.io;

import static de.latlon.deejump.wfs.data.JUMPFeatureFactory.createFromDeegreeFC;

import org.deegree.io.mapinfoapi.MapInfoReader;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.JUMPReader;

/**
 * <code>DeegreeMapInfoReader</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DeegreeMapInfoReader implements JUMPReader {

    public FeatureCollection read( DriverProperties dp )
                            throws Exception {
        String fileRoot = dp.getProperty( "File" );

        MapInfoReader reader = new MapInfoReader( fileRoot );
        reader.parseFeatures();

        return createFromDeegreeFC( reader.getFeatureCollection() );
    }

}
