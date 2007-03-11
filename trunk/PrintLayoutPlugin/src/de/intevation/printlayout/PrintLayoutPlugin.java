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

import org.w3c.dom.svg.SVGDocument;

import de.intevation.printlayout.util.PaperSizes;

import de.intevation.printlayout.batik.IncoreImageProtocolHandler;

import org.apache.batik.util.ParsedURL;

/**
 * The plugin binding to OpenJump.
 */
public class PrintLayoutPlugin 
extends      AbstractPlugIn 
implements   ThreadedPlugIn
{
	/**
	 * the black board of the plugin
	 */
	protected Blackboard blackboard;

	/**
	 * implements the initialize() method needed to
	 * initialize the plugin.
	 * @param context the plugin context
	 */
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

		ParsedURL.registerHandler(
			IncoreImageProtocolHandler.getInstance());
	}

	/**
	 * The name of the plugin. Actually 'Print/Layout'
	 * @return 'Print/Layout'
	 */
	public String getName() {
		return "Print/Layout";
	}
   

	/**
	 * implements the execute() method of the plugin.
	 * because its a long running plugin it return
	 * immediately. The real execution is done in run().
	 * @return always true
	 */
	public boolean execute(PlugInContext context)
	throws Exception
	{
		return true;
	}

	/**
	 * implements the run() method of the plugin.
	 * It creates a A4 paper sheet and opens up a LayoutFrame
	 * to edit it.
	 * @param monitor the TaskMonitor of the plugin
	 * @param context the plugin context
	 */
	public void run(TaskMonitor monitor, PlugInContext context)
	{
		monitor.allowCancellationRequests();

		SVGDocument document = PaperSizes.createSheet("A4");

		if (document == null)
			return;

		LayoutFrame viewer = new LayoutFrame(context);

		DocumentManager documentManger =
			viewer.getDocumentManager();

		documentManger.setDocument(document);

		viewer.setSize(210*2, 297*2);
		viewer.setVisible(true);
	}
}		
// end of file
