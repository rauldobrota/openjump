/*
 * Created on 17.10.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.latlon.deejump.io;


import de.latlon.deejump.util.data.JUMPFeatureFactory;

import org.deegree.model.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import org.deegree_impl.io.shpapi.ShapeFile;
import org.deegree_impl.model.feature.FeatureFactory;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.JUMPReader;

/**
 * @author ncho
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DeegreeShapeFileReader implements JUMPReader {
	
    /**
     * Main method to read a shapefile.  Most of the work is done in the org.geotools.* package.
     *
     *@param dp 'InputFile' or 'DefaultValue' to specify output .shp file.
     *
     */
    public FeatureCollection read(DriverProperties dp) throws Exception {
    	
    	String fileRoot = dp.getProperty("File");
    	
    	 if (fileRoot == null) {
    	 	throw new Exception("fileRoot should not be null");
        }

    	
        fileRoot = fileRoot.substring(0, dp.getProperty("File").length()-4);
    	
    	org.deegree.model.feature.FeatureCollection deegreeFC;
    	FeatureCollection jumpFC = null;
    	
		try {
			
			ShapeFile sf = new ShapeFile( fileRoot );			
			deegreeFC = FeatureFactory.createFeatureCollection("id", sf.getRecordNum());
			
			for (int i = 0; i < sf.getRecordNum(); i++) {
			
				Feature feat = sf.getFeatureByRecNo(i+1);			    
	            deegreeFC.appendFeature(feat);
			}
								
			jumpFC = JUMPFeatureFactory.createFromDeegreeFC( deegreeFC );
			
		} catch(Exception e) {
			e.printStackTrace();
		}       
        return jumpFC;
    }

}
