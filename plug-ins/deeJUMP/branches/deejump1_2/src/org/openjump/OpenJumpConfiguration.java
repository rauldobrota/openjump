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

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.openjump.core.ccordsys.srid.EnsureAllLayersHaveSRIDStylePlugIn;
import org.openjump.core.ui.plugin.edit.ReplicateSelectedItemsPlugIn;
import org.openjump.core.ui.plugin.edit.SelectAllLayerItemsPlugIn;
import org.openjump.core.ui.plugin.edit.SelectByTypePlugIn;
import org.openjump.core.ui.plugin.edit.SelectItemsByCircleFromSelectedLayersPlugIn;
import org.openjump.core.ui.plugin.edit.SelectItemsByFenceFromSelectedLayersPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.ConstrainedMoveVertexPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawCircleWithGivenRadiusPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedArcPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedCirclePlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedLineStringPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.DrawConstrainedPolygonPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.RotateSelectedItemPlugIn;
import org.openjump.core.ui.plugin.edittoolbox.SelectOneItemPlugIn;
import org.openjump.core.ui.plugin.file.SaveImageAsSVGPlugIn;
import org.openjump.core.ui.plugin.layer.AddSIDLayerPlugIn;
import org.openjump.core.ui.plugin.layer.ChangeSRIDPlugIn;
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
import org.openjump.core.ui.plugin.tools.LineSimplifyJTS15AlgorithmPlugIn;
import org.openjump.core.ui.plugin.tools.MeasureM_FPlugIn;
import org.openjump.core.ui.plugin.tools.ReducePointsISAPlugIn;
import org.openjump.core.ui.plugin.view.InstallKeyPanPlugIn;
import org.openjump.core.ui.plugin.view.MapToolTipPlugIn;
import org.openjump.core.ui.plugin.view.ShowFullPathPlugIn;
import org.openjump.core.ui.plugin.view.ShowScalePlugIn;
import org.openjump.core.ui.plugin.view.ZoomToScalePlugIn;
import org.openjump.core.ui.plugin.wms.ZoomToWMSPlugIn;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;


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
				
		MapToolTipPlugIn myMapTipPlugIn= new MapToolTipPlugIn();
		myMapTipPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
				 
		//--this caused problems with the postgis plugin [sstein]
		//  TODO: the problem has been solved (using try/catch) but still class has to be
		//        changed using LayerListener LayerEventType.ADDED event instead of 
		//		  layerSelectionChanged() from LayerNamePanelListener
		ShowFullPathPlugIn myFullPathPlugin = new ShowFullPathPlugIn();
		myFullPathPlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		
		/***********************
		 *  menu LAYER
		 **********************/
		
		ToggleVisiblityPlugIn myToggleVisPlugIn = new ToggleVisiblityPlugIn();
		myToggleVisPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		AddSIDLayerPlugIn myMrSIDPlugIn= new AddSIDLayerPlugIn();
		myMrSIDPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/*
		ChangeSRIDPlugIn myChangeSRIDPlugIn= new ChangeSRIDPlugIn();
		myChangeSRIDPlugIn.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		*/
		
		/***********************
		 *  menu TOOLS
		 **********************/
		
		/**** GENERATE ****/
		
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
		
		LineSimplifyJTS15AlgorithmPlugIn jtsSimplifier = new LineSimplifyJTS15AlgorithmPlugIn();
		jtsSimplifier.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		/**** tools main ****/
		
		//-- [sstein] do this to avoid that the programming menu is created after 
		//   MeasureM_FPlugIn is added to the tools menu
		PlugInContext pc = new PlugInContext(workbenchContext, null, null, null, null);
		FeatureInstaller fi = pc.getFeatureInstaller();
		JMenu menuTools = fi.menuBarMenu(MenuNames.TOOLS);
		fi.createMenusIfNecessary(menuTools, new String[]{MenuNames.TOOLS_PROGRAMMING});
		//---

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
		
		DrawCircleWithGivenRadiusPlugIn drawCirclePlugin = new DrawCircleWithGivenRadiusPlugIn();
		drawCirclePlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		
		//--  now initialized in #EditingPlugIn.java to fill toolbox
		/*
		ScaleSelectedItemsPlugIn myScaleItemsPlugin = new ScaleSelectedItemsPlugIn();
		myScaleItemsPlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		*/
		
		/***********************
		 *  others
		 **********************/
		
		// takes care of keyboard navigation
		new InstallKeyPanPlugIn().initialize( new PlugInContext(workbenchContext, null, null, null, null) );
		
		/*
		EnsureAllLayersHaveSRIDStylePlugIn ensureLayerSRIDPlugin = new EnsureAllLayersHaveSRIDStylePlugIn();
		ensureLayerSRIDPlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		*/
		
		/***********************
		 *  testing
		 **********************/
		/*
		ProjectionPlugIn projectionPlugin = new ProjectionPlugIn();
		projectionPlugin.initialize(new PlugInContext(workbenchContext, null, null, null, null));
		*/

	}
}
