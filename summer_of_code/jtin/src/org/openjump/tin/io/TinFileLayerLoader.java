/**
 * 
 */
package org.openjump.tin.io;

import org.openjump.tin.io.JTFReader;
import org.openjump.tin.ImmutableTin;
import org.openjump.tin.TinLayer;
import org.openjump.tin.TriangulatedIrregularNetwork;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.io.BufferedInputStream;
import java.io.IOException;

import org.openjump.core.ui.io.file.AbstractFileLayerLoader;
import org.openjump.core.ui.util.TaskUtil;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Category;





/**
 * Loads a TIN from a JTF file into a TinLayer.
 * 
 * @see		JTFLayout
 * @author Christopher DeMars
 *
 */
public class TinFileLayerLoader extends AbstractFileLayerLoader {
	
	/** The {@link TriangulatedIrregularNetwork} class. */
	private Class<ImmutableTin> tinClass;
	
	/** The workbench context. */
	private WorkbenchContext workbenchContext;
	  
	/**
	 * Standard constructor
	 * 
	 * @param workbenchContext	the JUMP workbench this tin is being loaded into
	 * @param tinClass			the TIN to be loaded
	 * @param description		a description of the file type
	 * @param extensions		a list of extensions associated with that type
	 */
	public TinFileLayerLoader(WorkbenchContext workbenchContext, Class<ImmutableTin> tinClass, String description, List<String> extensions) {
		super(description, extensions);
		this.tinClass = tinClass;
		this.workbenchContext = workbenchContext;
	}

	
	/**
	 * Open the file specified by the URI with the map of option values.
	 * 
	 * @param monitor 	The TaskMonitor.
	 * @param uri 		The URI to the file to load.
	 * @param options 	The map of options.
	 * @return 			True if the file could be loaded false otherwise.
	 */
	public boolean open(TaskMonitor monitor, URI uri, Map<String, Object> options) {
		ImmutableTin tin;
		BufferedInputStream in;

		// open input stream
		try {
			in = new BufferedInputStream(uri.toURL().openStream());
		}
		catch (IOException e) {
			ErrorHandler errorHandler = workbenchContext.getErrorHandler();
			errorHandler.handleThrowable(e);
			return false;
		}
		
		// read in the tin
		try {
			tin = JTFReader.read(in);
		}
		catch (IOException e) {
			ErrorHandler errorHandler = workbenchContext.getErrorHandler();
			errorHandler.handleThrowable(e);
			return false;
		}
		
        LayerManager layerManager = workbenchContext.getLayerManager();
        TinLayer layer = new TinLayer (workbenchContext, uri.getPath(), layerManager, tin, tin.getSRID());
        Category category = TaskUtil.getSelectedCategoryName(workbenchContext);
        layerManager.addLayerable(category.getName(), layer);
        
        return true;
	}
	
}
