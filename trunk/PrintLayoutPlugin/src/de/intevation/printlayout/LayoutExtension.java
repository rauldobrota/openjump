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
