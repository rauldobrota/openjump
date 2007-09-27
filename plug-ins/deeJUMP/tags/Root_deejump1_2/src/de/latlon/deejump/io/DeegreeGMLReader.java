/*
 * Created on 21.10.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.latlon.deejump.io;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.deegree_impl.model.feature.GMLFeatureAdapter;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.JUMPReader;

import de.latlon.deejump.util.data.JUMPFeatureFactory;

/**
 * @author sncho
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DeegreeGMLReader implements JUMPReader {
	
	/** Creates new ShapeReader */    
	public DeegreeGMLReader() {  
		//nothing to do	
	}
	
	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.io.JUMPReader#read(com.vividsolutions.jump.io.DriverProperties)
	 */
	public FeatureCollection read(DriverProperties dp) throws Exception {
	
		String fileRoot = dp.getProperty("File");
    	
    	 if (fileRoot == null) {
    	 	throw new Exception("fileRoot should not be null");
        }
      
        org.deegree.model.feature.FeatureCollection deegreeFC; 
        FeatureCollection jumpFC = null;
        	Reader reader = new InputStreamReader(new FileInputStream(fileRoot));
        	deegreeFC = GMLFeatureAdapter.wrap(reader);
        	jumpFC = JUMPFeatureFactory.createFromDeegreeFC(deegreeFC);
        	reader.close();
        
		return jumpFC;
	}

}
