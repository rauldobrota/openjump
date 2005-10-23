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

import javax.swing.JPopupMenu;

import org.openjump.core.ui.plugin.edit.ReplicateSelectedItemsPlugIn;
import org.openjump.core.ui.plugin.edit.SelectAllLayerItemsPlugIn;
import org.openjump.core.ui.plugin.edit.SelectByTypePlugIn;
import org.openjump.core.ui.plugin.edit.SelectItemsByCircleFromSelectedLayersPlugIn;
import org.openjump.core.ui.plugin.edit.SelectItemsByFenceFromSelectedLayersPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.ConstrainedMoveVertexPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedArcPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedCirclePlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedLineStringPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedPolygonPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.RotateSelectedItemPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.SelectOneItemPlugIn;
import org.openjump.core.ui.plugin.file.SaveImageAsSVGPlugIn;
import org.openjump.core.ui.plugin.layer.AddSIDLayerPlugIn;
import org.openjump.core.ui.plugin.layer.ToggleVisiblityPlugIn;
import org.openjump.core.ui.plugin.mousemenu.EditSelectedSidePlugIn;
import org.openjump.core.ui.plugin.mousemenu.MoveAlongAnglePlugIn;
import org.openjump.core.ui.plugin.mousemenu.RotatePlugIn;
import org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn;
import org.openjump.core.ui.plugin.queries.SimpleQueryPlugIn;
import org.openjump.core.ui.plugin.tools.BlendLineStringsPlugIn;
import org.openjump.core.ui.plugin.tools.ConvexHullPlugIn;
import org.openjump.core.ui.plugin.tools.DeleteEmptyGeometriesPlugIn;
import org.openjump.core.ui.plugin.tools.JoinWithArcPlugIn;
import org.openjump.core.ui.plugin.tools.MeasureM_FPlugIn;
import org.openjump.core.ui.plugin.tools.ReducePointsISAPlugIn;
import org.openjump.core.ui.plugin.view.MapToolTipPlugIn;
import org.openjump.core.ui.plugin.view.ShowFullPathPlugIn;
import org.openjump.core.ui.plugin.view.ShowScalePlugIn;
import org.openjump.core.ui.plugin.view.ZoomToScalePlugIn;
import org.openjump.core.ui.plugin.wms.ZoomToWMSPlugIn;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;


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
		
		SelectByTypePlugIn mySelectByGeomTypePlugIn = new SelectByTypePlugIn();
		mySelectByGeomTypePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/***********************
		 *  menu VIEW
		 **********************/
		
		ZoomToWMSPlugIn myZoomToWMSPlugIn = new ZoomToWMSPlugIn();
		myZoomToWMSPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		ZoomToScalePlugIn myZoomToScalePlugIn = new ZoomToScalePlugIn();
		myZoomToScalePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		ShowScalePlugIn myShowScalePlugIn = new ShowScalePlugIn();
		myShowScalePlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		//-- this plugin causes an error on mouse move and schema editing
		//MapToolTipPlugIn myMapTipPlugIn= new MapToolTipPlugIn();
		//myMapTipPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		 
		//--this plugin causes problems with the postgis plugin [sstein]
		//ShowFullPathPlugIn myFullPathPlugin = new ShowFullPathPlugIn();
		//myFullPathPlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		
		/***********************
		 *  menu LAYER
		 **********************/
		
		ToggleVisiblityPlugIn myToggleVisPlugIn = new ToggleVisiblityPlugIn();
		myToggleVisPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		AddSIDLayerPlugIn myMrSIDPlugIn= new AddSIDLayerPlugIn();
		myMrSIDPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/***********************
		 *  menu TOOLS
		 **********************/	
		/**** QUERY ****/
		SimpleQueryPlugIn mySimpleQueryPlugIn = new SimpleQueryPlugIn();
		mySimpleQueryPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		DeleteEmptyGeometriesPlugIn myDelGeomPlugin= new DeleteEmptyGeometriesPlugIn(); 
		myDelGeomPlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		/**** JOIN ****/
		ConvexHullPlugIn myConvHullPlugIn = new ConvexHullPlugIn();
		myConvHullPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		JoinWithArcPlugIn myJoinWithArcPlugIn= new JoinWithArcPlugIn();
		myJoinWithArcPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		BlendLineStringsPlugIn myLSBlender= new BlendLineStringsPlugIn();
		myLSBlender.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/**** GENERALIZATION ****/
		ReducePointsISAPlugIn mySimplifyISA = new ReducePointsISAPlugIn();
		mySimplifyISA.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/**** tools main ****/
		MeasureM_FPlugIn myFeetPlugIn = new MeasureM_FPlugIn();
		myFeetPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));

		
		/***********************
		 *  menu WINDOW
		 **********************/

		/***********************
		 *  menu HELP
		 **********************/

		/***********************
		 *  Right click menus
		 **********************/		
		JPopupMenu popupMenu = LayerViewPanel.popupMenu();
		popupMenu.addSeparator();        

		MoveAlongAnglePlugIn myMoveAlongAnglePlugin = new MoveAlongAnglePlugIn();
		myMoveAlongAnglePlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		RotatePlugIn myRotatePlugin = new RotatePlugIn();
		myRotatePlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		EditSelectedSidePlugIn myEditSidePlugin = new EditSelectedSidePlugIn();
		myEditSidePlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		//--this plugin causes problems with the postgis plugin [sstein]
		SaveDatasetsPlugIn mySaveDataSetPlugIn = new SaveDatasetsPlugIn();
		mySaveDataSetPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/***********************
		 *  EDITing toolbox
		 **********************/

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
		
		SelectOneItemPlugIn mySelectOnePlugin= new SelectOneItemPlugIn();
		mySelectOnePlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
	}
}
