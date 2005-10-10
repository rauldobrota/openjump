/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * JUMP is Copyright (C) 2003 Vivid Solutions
 *
 * This program implements extensions to JUMP and is
 * Copyright (C) Stefan Steiniger.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 * Stefan Steiniger
 * perriger@gmx.de
 */
/*****************************************************
 * created:  		09.01.2004
 * last modified:  	16.05.2005 (undo and selection of several items)
 * 
 * @author sstein
 * 
 * description:
 * 	changes a building / polygon to a rectangle if either
 *  local MinWidthParts Conlficts appear or the AreaSize is to small.<p>
 *  The solution algorithm deletes the holes. <p>
 *  An iterative solution is possible (stopped after 50 iterations).
 *****************************************************/

package mapgen.ui.onselecteditems;


import java.util.Collection;
import java.util.Iterator;

import mapgen.algorithms.polygons.BuildingEnlargeToRectangle;
import mapgen.constraints.polygons.PolygonMinimalArea;
import ch.unizh.geo.agents.goals.BuildingGoals;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;

/**
 * @description:
 * 	changes a building / polygon to a rectangle if either
 *  local MinWidthParts Conlficts appear or the AreaSize is to small.<p>
 *  The solution algorithm deletes the holes. <p>
 *  An iterative solution is possible (stopped after 50 iterations).
 * 
 * @author sstein
 *
 **/
public class EnlargeBuildingToRectanglePlugIn extends AbstractPlugIn implements ThreadedPlugIn{

    private ZoomToSelectedItemsPlugIn myZoom = new ZoomToSelectedItemsPlugIn();
    private static String T1 = "MapScale";
    private static String T5 = "Do solve iterative?";
    int scale = 1;
    boolean solveIter = true;

    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
    	featureInstaller.addMainMenuItem(
    	        this,								//exe
                new String[] {"Map Generalisation","Scale Dependent Algorithms" ,"Buildings"}, 	//menu path
                this.getName(), //name methode .getName recieved by AbstractPlugIn 
                false,			//checkbox
                null,			//icon
                createEnableCheck(context.getWorkbenchContext())); //enable check        
    }
    
    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

        return new MultiEnableCheck()
                        .add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck())
                        .add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1));
    }
    
	public boolean execute(PlugInContext context) throws Exception{
		
		this.reportNothingToUndoYet(context);
	    MultiInputDialog dialog = new MultiInputDialog(
	            context.getWorkbenchFrame(), getName(), true);
	    setDialogValues(dialog, context);
	    GUIUtil.centreOnWindow(dialog);
	    dialog.setVisible(true);
	    if (! dialog.wasOKPressed()) { return false; }
	    getDialogValues(dialog);
	    return true;
	}
	
    private void setDialogValues(MultiInputDialog dialog, PlugInContext context)
	  {
	    dialog.setSideBarDescription(
	        "Simplify Building to Rectangle: the map scale is used to decide if building has to changed to rectangle " +
	        "and how much to be enlarged ");
	    dialog.addIntegerField(T1, 25000, 7,T1);
	    dialog.addCheckBox(T5,true);	    
	  }

	private void getDialogValues(MultiInputDialog dialog) {
	    this.scale = dialog.getInteger(T1);
	    this.solveIter = dialog.getBoolean(T5);
	  }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
        
    	    //this.zoom2Feature(context);	    
    	    this.changeToRectangle(context, this.scale, this.solveIter, monitor);
    	    System.gc();    		
    	}
	
	/**
	 * centers the selected feature
	 * @param context
	 * @throws Exception
	 */
	private void zoom2Feature(PlugInContext context) throws Exception{
		    
	    this.myZoom.execute(context);	    
	}

	protected Layer layer(PlugInContext context) {
		return (Layer) context.getLayerViewPanel().getSelectionManager()
				.getLayersWithSelectedItems().iterator().next();
	}
	
	private boolean changeToRectangle(PlugInContext context, int scale, 
	                           boolean solveIterative, TaskMonitor monitor) throws Exception{
	    
	    System.gc(); //flush garbage collector
	    // --------------------------	    
	    //-- get selected items
	    final Collection features = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems();

		EditTransaction transaction = new EditTransaction(features, this.getName(), layer(context),
						this.isRollingBackInvalidEdits(context), false, context.getWorkbenchFrame());
	    
	    int count=0; int noItems = features.size(); Geometry resultgeom = null;
	    //--get single object in selection to analyse
      	for (Iterator iter = features.iterator(); iter.hasNext();) {
      		count++;
      		Feature f = (Feature)iter.next();
	   		Geometry geom = f.getGeometry(); //= erste Geometrie
	   		Polygon poly = null;
	       	if ( geom instanceof Polygon){
	       		poly = (Polygon) geom; //= erste Geometrie
	    	    // --------------------------
	           	/*	       		
	           	List resultList = new ArrayList();
	           	List conflictListA = new ArrayList();
	           	List conflictListB = new ArrayList();
	           	*/
	           	//---- detect conflicts
	           	BuildingGoals goals = new BuildingGoals(scale);
	           	//BuildingLocalWidth blw = new BuildingLocalWidth(poly, 
	           	//        	goals.getMinWidthReal(),goals.getMinWidthFlexibility());
	           	PolygonMinimalArea pma = new PolygonMinimalArea(poly,goals.getMinAreaReal(),goals.getMinAreaFlexibility());
	           	//---
	           	//if( (blw.measure.hasConflicts() == true) || (pma.isfullfilled() == false)){
	            if(pma.isfullfilled() == false){	           		
		           	context.getWorkbenchFrame().setStatusMessage("conflicts detected!");           	
		           	//conflictListA.addAll(blw.measure.getDispVecPointEdgeLStringList());
		           	//conflictListA.addAll(blw.measure.getDispVecPointPointLStringList());
		           	//--- solve conflicts ---	           	
		           	if (solveIterative == false){
		           	    BuildingEnlargeToRectangle enlarge = new BuildingEnlargeToRectangle(poly,goals.getMinAreaReal());
		           	    //resultList.add(enlarge.getOutPolygon());
		           	    transaction.setGeometry(count-1, enlarge.getOutPolygon());
		           	}
		           	else{
		           	 //====================================   
		           	 // if solution should be done iterative
		           	 //====================================
		           	    BuildingEnlargeToRectangle enlarge = null;
		           	    int j = 0;
		           	    boolean solved = false;
		           	    while(solved == false){
			           	    enlarge = new BuildingEnlargeToRectangle(poly,goals.getMinAreaReal()); 		        
			           	    poly = enlarge.getOutPolygon();
			           	    //-- detect conflicts
			           	    //blw = new BuildingLocalWidth(poly, 
			           	    //    	goals.getMinWidthReal(),goals.getMinWidthFlexibility());
			               	pma = new PolygonMinimalArea(poly,goals.getMinAreaReal(),goals.getMinAreaFlexibility());
			               	//--
							//if((blw.measure.hasConflicts() == false) || (pma.isfullfilled() == true)){
						    if(pma.isfullfilled() == true){
						   	    solved = true;
								}
	
			           	    //--notbremse:
			           	    j = j + 1;
			           	    if(j == 50){
			           	        solved = true;
			           	        context.getWorkbenchFrame().warnUser("item " + count + " : solution alg. stopped at step: " + j);
			           	    }
			           	    //-- visualisation
			           	    /*
			               	List stepList = new ArrayList();
			           	    stepList.add(0,enlarge.getOutPolygon());
				    	    FeatureCollection myCollD = FeatureDatasetFactory.createFromGeometry(stepList);
				    	    if (myCollD.size() > 0){
				    		    context.addLayer(StandardCategoryNames.WORKING, "stepList", myCollD);
				    		    }	    	  
				    	    */
		           	    }
		           	    //resultList.add(enlarge.getOutPolygon());
		           	    transaction.setGeometry(count-1, enlarge.getOutPolygon());
		           	}
		           	// ===== visulisation =====
		           	/*
		    	    FeatureCollection myCollA = FeatureDatasetFactory.createFromGeometry(conflictListA);
		    	    if (myCollA.size() > 0){
		    		    context.addLayer(StandardCategoryNames.WORKING, "point-edge conflicts", myCollA);
		    		    }	          	
		    	    FeatureCollection myCollB = FeatureDatasetFactory.createFromGeometry(resultList);
		    	    if (myCollB.size() > 0){
		    		    context.addLayer(StandardCategoryNames.WORKING, "result", myCollB);
		    		    }
		    		*/
		        }// ========================       		
	           	else{
	           	    context.getWorkbenchFrame().setStatusMessage("no conflict detected!");
	           	}
	       	}
	       	else{
	       	    context.getWorkbenchFrame().warnUser("no polygon selected");
	       	}
		    String mytext = "item: " + count + " / " + noItems + " : squaring finalized";
		    monitor.report(mytext);	       		       	
      	}// end loop over item selection
       	transaction.commit();
        return true;        
	}
    
  
}
