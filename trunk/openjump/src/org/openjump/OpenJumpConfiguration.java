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
import org.openjump.core.ui.plugin.edittoolbox.ConstrainedMoveVertexPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedArcPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedCirclePlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedLineStringPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedPolygonPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.RotateSelectedItemPlugIn;
import org.openjump.core.ui.plugin.file.SaveImageAsSVGPlugIn;
import org.openjump.core.ui.plugin.layer.ToggleVisiblityPlugIn;
import org.openjump.core.ui.plugin.queries.SimpleQueryPlugIn;
import org.openjump.core.ui.plugin.view.ShowScalePlugIn;
import org.openjump.core.ui.plugin.view.ZoomToScalePlugIn;
import org.openjump.core.ui.plugin.wms.ZoomToWMSPlugIn;

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
		
		/***********************
		 *  menu FILE
		 **********************/
		SaveImageAsSVGPlugIn imageSvgPlugin= new SaveImageAsSVGPlugIn();
		imageSvgPlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));

		/***********************
		 *  menu EDIT
		 **********************/
		SelectItemsByFenceFromSelectedLayersPlugIn selectItemsFromLayersPlugIn = new SelectItemsByFenceFromSelectedLayersPlugIn();
		selectItemsFromLayersPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		SelectItemsByCircleFromSelectedLayersPlugIn selectItemsFromCirclePlugIn = new SelectItemsByCircleFromSelectedLayersPlugIn();
		selectItemsFromCirclePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		SelectAllLayerItemsPlugIn selectAllLayerItemsPlugIn = new SelectAllLayerItemsPlugIn();
		selectAllLayerItemsPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		ReplicateSelectedItemsPlugIn replicatePlugIn = new ReplicateSelectedItemsPlugIn();
		replicatePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/***********************
		 *  menu VIEW
		 **********************/
		
		ZoomToWMSPlugIn myZoomToWMSPlugIn = new ZoomToWMSPlugIn();
		myZoomToWMSPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		ZoomToScalePlugIn myZoomToScalePlugIn = new ZoomToScalePlugIn();
		myZoomToScalePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		ShowScalePlugIn myShowScalePlugIn = new ShowScalePlugIn();
		myShowScalePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/***********************
		 *  menu LAYER
		 **********************/
//		ToggleVisiblityPlugIn myToggleVisPlugIn = new ToggleVisiblityPlugIn();
//		myToggleVisPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/***********************
		 *  menu TOOLS
		 **********************/
		SimpleQueryPlugIn mySimpleQueryPlugIn = new SimpleQueryPlugIn();
		mySimpleQueryPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/***********************
		 *  menu WINDOW
		 **********************/

		/***********************
		 *  menu HELP
		 **********************/

		/***********************
		 *  Right click menus
		 **********************/

		/***********************
		 *  EDITing toolbox
		 **********************/
/*
		DrawConstrainedPolygonPlugIn myConstrainedPolygonPlugIn = new DrawConstrainedPolygonPlugIn();
		myConstrainedPolygonPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));

		DrawConstrainedLineStringPlugIn myConstrainedLSPlugIn = new DrawConstrainedLineStringPlugIn();
		myConstrainedLSPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		DrawConstrainedCirclePlugIn myConstrainedCPlugIn = new DrawConstrainedCirclePlugIn();
		myConstrainedCPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		DrawConstrainedArcPlugIn myConstrainedArcPlugIn = new DrawConstrainedArcPlugIn();
		myConstrainedArcPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));

		ConstrainedMoveVertexPlugIn myCMVPlugIn = new ConstrainedMoveVertexPlugIn();
		myCMVPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		RotateSelectedItemPlugIn myRotateSIPlugIn = new RotateSelectedItemPlugIn();
		myRotateSIPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
*/		
	}
}
