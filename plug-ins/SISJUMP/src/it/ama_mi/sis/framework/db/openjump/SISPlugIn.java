/*
 * Created on 24-nov-2006
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.ama_mi.sis.framework.db.openjump;

import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * @author Paolo
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SISPlugIn extends AbstractPlugIn {

	/**
	 * 
	 */
	public SISPlugIn() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 */
	public SISPlugIn(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.plugin.AbstractPlugIn#initialize(com.vividsolutions.jump.workbench.plugin.PlugInContext)
	 */
	public void initialize(PlugInContext context) throws Exception {
		super.initialize(context);
		
		context.getWorkbenchContext().getRegistry().createEntry(
				DataStoreDriver.REGISTRY_CLASSIFICATION,
				new SISDBDataStoreDriver());
	}
}
