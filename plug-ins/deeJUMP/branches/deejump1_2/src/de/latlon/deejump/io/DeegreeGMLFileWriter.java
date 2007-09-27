/*
 * Created on 13.12.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.latlon.deejump.io;

import java.io.FileOutputStream;

import org.deegree.model.feature.GMLFeatureAdapter;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPWriter;

import de.latlon.deejump.util.data.JUMPFeatureFactory;

/**
 * @author hamammi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DeegreeGMLFileWriter implements JUMPWriter{
	

	public static void main(String[] args) {
	}
	
	public void write(FeatureCollection featureCollection, DriverProperties dp) 
	        throws IllegalParametersException, Exception {
		String fileRoot = dp.getProperty("File");

		if (fileRoot == null) {
			throw new Exception("fileRoot should not be null");
		}
		org.deegree.model.feature.FeatureCollection deegreeFC = JUMPFeatureFactory.
		       createFromJUMPFeatureCollection(featureCollection);
		FileOutputStream fos = new FileOutputStream(fileRoot);
		new GMLFeatureAdapter().export(deegreeFC, fos);
		fos.close();
		
	}
}
