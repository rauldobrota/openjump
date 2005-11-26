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
 * created:  		26.11.2005
 * last modified:  	
 *  
 * @author sstein
 * 
 * description:
 * 	eliminates a building / polygon if the AreaSize is to small.
 *****************************************************/

package mapgen.ui.onselecteditems;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import mapgen.constraints.polygons.PolygonMinimalArea;

import ch.unizh.geo.agents.goals.BuildingGoals;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;

/**
 * @description:
 * 	eliminates a building / polygon if the AreaSize is to small.
 * 
 * @author sstein
 *
 **/
public class EliminateSmallBuildingsPlugIn extends AbstractPlugIn implements ThreadedPlugIn{

    private ZoomToSelectedItemsPlugIn myZoom = new ZoomToSelectedItemsPlugIn();
    private static String T1 = "MapScale";
    int scale = 1;    

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
	        "Eliminate too small buidlings using the size of area as criterion. A tolerance value is used so the building can be a bit smaller than necessary");
	    dialog.addIntegerField(T1, 25000, 7,T1);	    	    
	  }

	private void getDialogValues(MultiInputDialog dialog) {
	    this.scale = dialog.getInteger(T1);	    
	  }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
        
    	    //this.zoom2Feature(context);	    
    	    FeatureCollection fc = this.eliminate(context, this.scale, monitor);
    	    context.addLayer(StandardCategoryNames.WORKING, "buildings with minArea", fc); 
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
	
	private FeatureCollection eliminate(PlugInContext context, int scale, 
	                           TaskMonitor monitor) throws Exception{
	    
	    System.gc(); //flush garbage collector
	    // --------------------------	    
	    //-- get selected items
	    final Collection features = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems();
    
	    int count=0; int noItems = features.size(); Geometry resultgeom = null;

	    //--get single object in selection to analyse
	    FeatureDataset resultFeatures = null;
	    ArrayList problematicEdges = new ArrayList();
       	//List resultList = new ArrayList();
       	FeatureSchema fs = new FeatureSchema();
       	int eliminated = 0;
      	for (Iterator iter = features.iterator(); iter.hasNext();) {
      		count++;
      		Feature ft = (Feature)iter.next();
      		//-- clone to avoid that original features get changed
      		Feature f= (Feature)ft.clone(); 
      		if (count == 1){      			
      			//-- not sure to do that, since feature schemas of selected objects might be different 
      			fs = copyFeatureSchema(f.getSchema());
      			resultFeatures = new FeatureDataset(fs);
      		}      		
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
	           	PolygonMinimalArea pma = new PolygonMinimalArea(poly,goals.getMinAreaReal(),goals.getMinAreaFlexibility());
	           	//---
	            if(pma.isfullfilled() == false){	           		
		           	context.getWorkbenchFrame().setStatusMessage("to small bldg id: " + f.getID());
		           	eliminated++;
	            }
	           	else{//if area is larger as necessary or within tolerance 
	           	    resultFeatures.add(f);
	           	}
	       	}
	       	else{
	       	    context.getWorkbenchFrame().warnUser("no polygon selected");
	       	}
		    String mytext = "item: " + count + " / " + noItems + " : tested";
		    monitor.report(mytext);	       		       	
      	}// end loop over item selection
      	context.getWorkbenchFrame().warnUser("eliminated: " + eliminated + " from: " + count);
        return resultFeatures;        
	}
	
	private FeatureSchema copyFeatureSchema(FeatureSchema oldSchema){
		FeatureSchema fs = new FeatureSchema();
		for (int i = 0; i < oldSchema.getAttributeCount(); i++) {
			AttributeType at = oldSchema.getAttributeType(i);
			String aname = oldSchema.getAttributeName(i);
			fs.addAttribute(aname,at);
			fs.setCoordinateSystem(oldSchema.getCoordinateSystem());
		}		
		return fs;
	}
  
}
