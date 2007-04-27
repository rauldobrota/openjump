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

import javax.swing.JFrame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The plugin binding to OpenJump.
 */
public class PrintLayoutPlugin 
extends      AbstractPlugIn 
implements   ThreadedPlugIn
{
	/**
	 * This is the name of the plug in.
	 */
	public static final String PLUGIN_NAME = "Print/Layout";

	/**
	 * This is the current version of the plug in.
	 */
	public static final String PLUGIN_VERSION = "1.0.0";

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

		IncoreImageProtocolHandler iiph =
			IncoreImageProtocolHandler.getInstance();

		ParsedURL.registerHandler(iiph);
	}

	/**
	 * The name of the plugin. Actually 'Print/Layout'
	 * @return 'Print/Layout'
	 */
	public String getName() {
		return PLUGIN_NAME;
	}

	public static String getNameAndVersion() {
		return PLUGIN_NAME + " - " + PLUGIN_VERSION;
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

		IncoreImageProtocolHandler iiph =
			IncoreImageProtocolHandler.getInstance();

		iiph.incrementReferenceCount();

		LayoutFrame viewer = new LayoutFrame(context);

		DocumentManager documentManager =
			viewer.getDocumentManager();

		documentManager.setDocument(document);

		documentManager.addPostProcessor(
			new PreviewMapReplacer(context));

		viewer.setSize(210*2, 297*2);
		viewer.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		viewer.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				PrintLayoutPlugin.this.clear((LayoutFrame)we.getSource());
			}
		});
		viewer.setVisible(true);
	}

	/**
	 * Called on closing the viewer frame.
	 * @param viewer the viewer to close.
	 */
	protected void clear(LayoutFrame viewer) {
		IncoreImageProtocolHandler iiph =
			IncoreImageProtocolHandler.getInstance();
		iiph.decrementReferenceCount();
		viewer.dispose();
		viewer.clear();
	}
}		
// end of file
