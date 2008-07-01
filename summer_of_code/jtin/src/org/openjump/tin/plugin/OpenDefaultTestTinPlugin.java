/**
 * 
 */
package org.openjump.tin.plugin;

import java.util.ArrayList;

import org.openjump.core.ui.io.file.FileLayerLoader;
import org.openjump.tin.io.TinFileLayerLoader;
import org.openjump.tin.renderer.TinRenderer;
import org.openjump.tin.renderer.TinRendererFactory;
import org.openjump.tin.ImmutableTin;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;


/**
 * @author paradox
 *
 */
public class OpenDefaultTestTinPlugin extends AbstractPlugIn {

	/**
	 * 
	 */
	public OpenDefaultTestTinPlugin() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 */
	public OpenDefaultTestTinPlugin(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public void initialize (PlugInContext context) throws Exception {
		context.getFeatureInstaller().addMainMenuItem(this, 
				new String[] {"Tools", "TIN"} , getName(), false, null, null);
	}
	
	public boolean execute (PlugInContext context) throws Exception {
/*
		ArrayList<String> extensions = new ArrayList<String>(1);
		extensions.add(".jtf");
		context.getWorkbenchContext().getRegistry().createEntry(FileLayerLoader.KEY, 
				new TinFileLayerLoader(context.getWorkbenchContext(), ImmutableTin.class,
						"OpenJUMP TIN file", extensions));
*/
		return true;
	}

   
	public String getName() {
        return "OpenDefaultTestTin";
    }

}
