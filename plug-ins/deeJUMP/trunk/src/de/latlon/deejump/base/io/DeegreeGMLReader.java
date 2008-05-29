/*
 * Created on 21.10.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.latlon.deejump.base.io;

import java.io.FileInputStream;

import org.deegree.model.feature.GMLFeatureCollectionDocument;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.JUMPReader;

import de.latlon.deejump.wfs.data.JUMPFeatureFactory;

/**
 * @author sncho
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class DeegreeGMLReader implements JUMPReader {

    public FeatureCollection read( DriverProperties dp )
                            throws Exception {

        String fileRoot = dp.getProperty( "File" );

        if ( fileRoot == null ) {
            throw new Exception( "fileRoot should not be null" );
        }

        org.deegree.model.feature.FeatureCollection deegreeFC = null;
        FeatureCollection jumpFC = null;

        GMLFeatureCollectionDocument gmlDoc = new GMLFeatureCollectionDocument();
        gmlDoc.load( new FileInputStream( fileRoot ), "file:///" + fileRoot );
        deegreeFC = gmlDoc.parse();
        jumpFC = JUMPFeatureFactory.createFromDeegreeFC( deegreeFC );

        return jumpFC;
    }

}
