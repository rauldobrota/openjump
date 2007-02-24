/*
 * LayoutExtension.java
 * --------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * Configures the plugin as an extension.
 */
public class LayoutExtension
extends      Extension
{
	/**
	 * overrides configure() from base class. Used to bootstrap
	 * the plugin by the OpenJUMP framework.
	 * @param context the plugin context.
	 */
	public void configure(PlugInContext context) throws Exception {
		new PrintLayoutPlugin().initialize(context);
	}
} 
// end of file
