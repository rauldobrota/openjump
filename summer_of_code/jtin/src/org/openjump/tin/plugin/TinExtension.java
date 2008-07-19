/**
 * 
 */
package org.openjump.tin.plugin;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import org.openjump.core.ui.io.file.FileLayerLoader;
import org.openjump.tin.ImmutableTin;
import org.openjump.tin.TinLayer;
import org.openjump.tin.io.TinFileLayerLoader;
import org.openjump.tin.io.JTFLayout;
import org.openjump.tin.renderer.TinRendererFactory;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;



public class TinExtension extends Extension {


	public TinExtension() {
		// TODO Auto-generated constructor stub
	}


	public void configure(PlugInContext context) throws Exception {
	    RenderingManager.setRendererFactory(TinLayer.class,
	    	      new TinRendererFactory());
	    
	    ArrayList<String> extensions = new ArrayList<String>(1);
		extensions.add(JTFLayout.FILE_NAME_EXTENSION);
		context.getWorkbenchContext().getRegistry().createEntry(FileLayerLoader.KEY, 
				new TinFileLayerLoader(context.getWorkbenchContext(), ImmutableTin.class,
						"OpenJUMP TIN file", extensions));
		
	    JPopupMenu tinLayerPopupMenu = new JPopupMenu();
	    
	    WorkbenchFrame workbenchFrame = context.getWorkbenchFrame();
	    workbenchFrame.setExtendedState(workbenchFrame.getExtendedState()
	      | JFrame.MAXIMIZED_BOTH);
	    workbenchFrame.getNodeClassToPopupMenuMap().put(TinLayer.class, tinLayerPopupMenu);
	    
	    new CreateTinFromVectorLayerPlugin().initialize(context);
	}

}
