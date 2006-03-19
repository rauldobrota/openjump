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
 * created:  		07.01.2004
 * last modified:  	
 * 
 * description:
 * 	changes the elongation of a building / polygon 
 *  by a given ScaleFactor using PolygonChangeElongation algorithm
 *  (Angle and center point can be given as well if 
 *  plugin will be changed)
 * 
 *****************************************************/

package mapgen.ui.onselecteditems;

import mapgen.algorithms.polygons.PolygonChangeElongation;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.zoom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import java.util.Collection;

/**
 * @author sstein
 *
 **/
public class ChangeElongationSelectedBuildingPlugIn extends AbstractPlugIn implements ThreadedPlugIn{

    private ZoomToSelectedItemsPlugIn myZoom = new ZoomToSelectedItemsPlugIn();
    private static String T1 = "scalefactor";
    double scale = 1;

    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
    	featureInstaller.addMainMenuItem(
    	        this,								//exe
                new String[] {"PlugIns","Map Generalisation","Not Scale Dependent Algorithms" ,"Buildings"}, 	//menu path
                "Change Elongation of Building", //name methode .getName recieved by AbstractPlugIn 
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
	        "Change Building Elongation");
	    dialog.addDoubleField(T1, 1.0, 3);
	  }

	private void getDialogValues(MultiInputDialog dialog) {
	    this.scale = dialog.getDouble(T1);
	  }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
        
    	    //this.zoom2Feature(context);	    
    	    this.changeElong(context, this.scale);
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

	private boolean changeElong(PlugInContext context, double scale) throws Exception{
	    
	    System.gc(); //flush garbage collector
	    // --------------------------	    
	    //-- get selected items
	    final Collection geometries = context.getLayerViewPanel().getSelectionManager().getSelectedItems();	    
	    //--get first/single object in selection to analyse
      	Iterator i = geometries.iterator();
      	Object geom = i.next();
       	Polygon poly = null;       	
       	if ( geom instanceof Polygon){
       		poly = (Polygon) geom; //= erste Geometrie
    	    // --------------------------
           	List resultList = new ArrayList();
           	PolygonChangeElongation pce = new PolygonChangeElongation(poly,scale);
           	resultList.add(pce.getOutPolygon());
           	//resultList.add(pce.getCenter());
           	context.getWorkbenchFrame().setStatusMessage("horizontal angle: " + pce.getAngle());
    	    FeatureCollection myCollB = FeatureDatasetFactory.createFromGeometry(resultList);
    	    if (myCollB.size() > 0){
    		    context.addLayer(StandardCategoryNames.WORKING, "result", myCollB);
    		    }
       	}
       	else{
       	    context.getWorkbenchFrame().warnUser("no polygon selected");
       	}
        return true;        
	}
    
  
}
