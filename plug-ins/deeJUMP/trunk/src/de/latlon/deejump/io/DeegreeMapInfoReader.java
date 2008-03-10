package de.latlon.deejump.io;

import static de.latlon.deejump.util.data.JUMPFeatureFactory.createFromDeegreeFC;
import static org.deegree.io.mapinfoapi.MapInfoReader.parseFeatures;

import java.util.HashMap;
import java.util.HashSet;

import org.deegree.framework.util.Pair;

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

        Pair<org.deegree.model.feature.FeatureCollection, HashMap<String, HashSet<HashMap<String, String>>>> pair;
        pair = parseFeatures( fileRoot );

        return createFromDeegreeFC( pair.first );
    }

}
