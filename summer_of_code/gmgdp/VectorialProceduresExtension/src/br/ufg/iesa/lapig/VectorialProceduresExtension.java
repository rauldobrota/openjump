package br.ufg.iesa.lapig;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * @description:
 *    The Vectorial Procedures Extension
 *     
 * @author Leandro Leal
 *
 **/
public class VectorialProceduresExtension extends Extension{

	/**
	 * calls onLayerPlugIn
	 */
	public void configure(PlugInContext context) throws Exception{
		new OnLayersPlugIn().initialize(context);
	}
	
}
