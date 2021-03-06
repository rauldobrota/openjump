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
 * Rectifies (squares) the walls of a building. Therefore the building main 
 * directions are obtained from the longest building walls. The algorithm works 
 * with respect to two thresholds:  First it allows a maximum change in the 
 * wall angle given by the user; second it respects a maximum point displacement 
 * of the corner points calculated from the user given target map scale value. 
 * The Algorithm is described by N. Regnauld, A. Edwardes and M. Barrault 
 * (ACI Workshop, 1999) and in Agent Work Package D1.
 *
 *****************************************************/

package mapgen.ui.onselecteditems;


import mapgen.agents.goals.BuildingGoals;
import mapgen.algorithms.polygons.BuildingSquaring;
import mapgen.constraints.buildings.BuildingSquareness;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.zoom.*;
import java.util.Iterator;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;

/**
 * @description:
 * Rectifies (squares) the walls of a building. Therefore the building main 
 * directions are obtained from the longest building walls. The algorithm works 
 * with respect to two thresholds:  First it allows a maximum change in the 
 * wall angle given by the user; second it respects a maximum point displacement 
 * of the corner points calculated from the user given target map scale value. 
 * The Algorithm is described by N. Regnauld, A. Edwardes and M. Barrault 
 * (ACI Workshop, 1999) and in Agent Work Package D1.
 * 
 * @author sstein
 *
 **/
public class SquareBuildingPlugIn extends AbstractPlugIn implements ThreadedPlugIn{

    private ZoomToSelectedItemsPlugIn myZoom = new ZoomToSelectedItemsPlugIn();
    private static String T1 = "MapScale";
    private static String T2 = "Max angle change in degrees";    
    int scale = 1;
    double maxAngle = 0;

    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
    	featureInstaller.addMainMenuItem(
    	        this,								//exe
                new String[] {"PlugIns","Map Generalisation","Scale Dependent Algorithms", "Buildings"}, 	//menu path
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
	        "Square Building: MapScale influences max point displacement");
	    dialog.addIntegerField(T1, 25000, 7,T1);
	    dialog.addDoubleField(T2, 15.0,4);
	  }

	private void getDialogValues(MultiInputDialog dialog) {
	    this.maxAngle = dialog.getDouble(T2);
	    this.scale = dialog.getInteger(T1);
	  }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
        
			monitor.allowCancellationRequests();
    	    //this.zoom2Feature(context);	    
    	    this.square(context, this.scale, this.maxAngle, monitor);
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
	
	private boolean square(PlugInContext context, int scale, 
	                           double maxDevAngle, TaskMonitor monitor) throws Exception{
	    
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
	       		poly = (Polygon) geom;
	    	    // --------------------------
	           	//---- detect conflicts	       		
	           	BuildingGoals goals = new BuildingGoals(scale);	       		
	           	//-- tolerance value is set to zero tolerance
	           	BuildingSquareness bs = new BuildingSquareness(poly, 0.0);
	           	double posAccuracy = 3*goals.getPositionAccuracyReal();
	           	//---
	           	if( (bs.getSeverity() > 0)){
		           	context.getWorkbenchFrame().setStatusMessage("conflicts detected!");  

	           	    BuildingSquaring squaring = new BuildingSquaring(poly,this.maxAngle, posAccuracy);
	           	    //BuildingSquaring squaring = new BuildingSquaring(poly,this.maxAngle);           	    
	    			transaction.setGeometry(count-1, squaring.getOutPolygon());
		        }       		
	           	else{
	           	    context.getWorkbenchFrame().setStatusMessage("no conflict detected!");
	           	}
	       	}
	       	else{
	       	    context.getWorkbenchFrame().warnUser("item is not a polygon");
	       	}
		    String mytext = "item: " + count + " / " + noItems + " : squaring finalized";
		    monitor.report(mytext);	       	
      	}//end loop for selection
       	transaction.commit();
        return true;        
	}
    
  
}
