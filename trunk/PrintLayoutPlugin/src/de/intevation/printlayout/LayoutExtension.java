/*
 * LayoutExtension.java
 * --------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 */
package de.intevation.printlayout;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class LayoutExtension
extends      Extension
{
	public void configure(PlugInContext context) throws Exception {
		new PrintLayoutPlugin().initialize(context);
	}
} 
// end of file
