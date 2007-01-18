/*
 * PrintLayoutPlugin.java
 * ----------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import com.vividsolutions.jump.workbench.ui.MenuNames;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;

import com.vividsolutions.jump.util.Blackboard;

import com.vividsolutions.jump.task.TaskMonitor;  

public class PrintLayoutPlugin 
extends      AbstractPlugIn 
implements   ThreadedPlugIn
{
	protected Blackboard blackboard;

	public void initialize(PlugInContext context)
	throws Exception
	{
		EnableCheckFactory check = 
		  new EnableCheckFactory(context.getWorkbenchContext());

		context
			.getFeatureInstaller()
			.addMainMenuItem(
				this, new String[] { MenuNames.FILE }, getName(),
				false, null, check.createAtLeastNLayersMustExistCheck(1));

		 blackboard = new Blackboard();
	}

	public String getName() {
		return "Print/Layout";
	}
   

	public boolean execute(PlugInContext context)
	throws Exception
	{
		return true;
	}

	public void run(TaskMonitor monitor, PlugInContext context)
	{
		monitor.allowCancellationRequests();

		LayoutFrame viewer = new LayoutFrame(context);

		viewer.setSize(210*2, 297*2);

		viewer.setVisible(true);
	}
}		
// end of file
