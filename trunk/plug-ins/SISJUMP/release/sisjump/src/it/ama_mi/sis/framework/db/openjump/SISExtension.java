/*
 * Created on 24-nov-2006
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.ama_mi.sis.framework.db.openjump;

import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * @author Paolo
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SISExtension extends Extension {

	/**
	 * 
	 */
	public SISExtension() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.plugin.Configuration#configure(com.vividsolutions.jump.workbench.plugin.PlugInContext)
	 */
	public void configure(PlugInContext context) throws Exception {
		try {
			context.getWorkbenchContext().getRegistry().createEntry(
					DataStoreDriver.REGISTRY_CLASSIFICATION, new SISDBDataStoreDriver());
		} catch (Throwable e) {
			context.getWorkbenchFrame().warnUser("SISDBDataStoreDriver not loaded");
			context.getErrorHandler().handleThrowable(e);
		}
		
		try {
			new SISNodeOnArcPlugIn().initialize(context);
/*			
			Class clazz = Class.forName("it.ama_mi.sis.framework.db.openjump.SISNodeOnArcPlugIn");
			PlugIn plugin = (PlugIn)clazz.newInstance();
			plugin.initialize(context); */
		} catch (Throwable e) {
			context.getWorkbenchFrame().warnUser("SISNodeOnArcPlugIn not loaded");
			//context.getErrorHandler().handleThrowable(e);
		}
	}
}
