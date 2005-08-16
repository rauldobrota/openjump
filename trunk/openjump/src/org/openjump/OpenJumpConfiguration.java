/*
 * Created on Aug 11, 2005
 * 
 * description:
 *   This class loads all openjump plugins.
 *   The method loadOpenJumpPlugIns() is called from 
 *   com.vividsolutions.jump.workbench.JUMPConfiguaration. 
 *
 *
 */
package org.openjump;

import org.openjump.core.ui.plugin.edit.ReplicateSelectedItemsPlugIn;
import org.openjump.core.ui.plugin.edit.SelectAllLayerItemsPlugIn;
import org.openjump.core.ui.plugin.edit.SelectItemsByCircleFromSelectedLayersPlugIn;
import org.openjump.core.ui.plugin.edit.SelectItemsByFenceFromSelectedLayersPlugIn;
import org.openjump.core.ui.plugin.file.SaveImageAsSVGPlugIn;
import org.openjump.core.ui.plugin.view.ShowScalePlugIn;
import org.openjump.core.ui.plugin.view.ZoomToScalePlugIn;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * @description:
 *   This class loads all openjump plugins.
 *   The method loadOpenJumpPlugIns() is called from 
 *   com.vividsolutions.jump.workbench.JUMPConfiguaration. 

 * @author sstein
 *
 */
public class OpenJumpConfiguration{

	public static void loadOpenJumpPlugIns(final WorkbenchContext workbenchContext)throws Exception {
		
		
		/*-----------------------------------------------
		 *  add here first the field which holds the plugin
		 *  and afterwards initialize it for the menu
		 *-----------------------------------------------*/
		ZoomToScalePlugIn myZoomToScalePlugIn = new ZoomToScalePlugIn();
		myZoomToScalePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		ShowScalePlugIn myShowScalePlugIn = new ShowScalePlugIn();
		myShowScalePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		SelectItemsByFenceFromSelectedLayersPlugIn selectItemsFromLayersPlugIn = new SelectItemsByFenceFromSelectedLayersPlugIn();
		selectItemsFromLayersPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		SelectItemsByCircleFromSelectedLayersPlugIn selectItemsFromCirclePlugIn = new SelectItemsByCircleFromSelectedLayersPlugIn();
		selectItemsFromCirclePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		SelectAllLayerItemsPlugIn selectAllLayerItemsPlugIn = new SelectAllLayerItemsPlugIn();
		selectAllLayerItemsPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		ReplicateSelectedItemsPlugIn replicatePlugIn = new ReplicateSelectedItemsPlugIn();
		replicatePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		SaveImageAsSVGPlugIn imageSvgPlugin= new SaveImageAsSVGPlugIn();
		imageSvgPlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
	}
}
