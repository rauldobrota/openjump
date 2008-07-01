/**
 * 
 */
package org.openjump.tin.plugin;

import java.util.ArrayList;

import org.openjump.core.ui.io.file.FileLayerLoader;
import org.openjump.tin.ImmutableTin;
import org.openjump.tin.TinLayer;
import org.openjump.tin.io.TinFileLayerLoader;
import org.openjump.tin.renderer.TinRenderer;
import org.openjump.tin.renderer.TinRendererFactory;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;

/**
 * @author paradox
 *
 */
public class TinExtension extends Extension {

	/**
	 * 
	 */
	public TinExtension() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.plugin.Configuration#configure(com.vividsolutions.jump.workbench.plugin.PlugInContext)
	 */
	public void configure(PlugInContext context) throws Exception {
	    RenderingManager.setRendererFactory(TinLayer.class,
	    	      new TinRendererFactory());
	    
	    ArrayList<String> extensions = new ArrayList<String>(1);
		extensions.add("jtf");
		context.getWorkbenchContext().getRegistry().createEntry(FileLayerLoader.KEY, 
				new TinFileLayerLoader(context.getWorkbenchContext(), ImmutableTin.class,
						"OpenJUMP TIN file", extensions));

	    new OpenDefaultTestTinPlugin().initialize(context);
	}

}
