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
 * created:  		16.12.2004
 * last modified:  	14.05.2005 (segmentation)
 * 					16.05.2005 (undo and selection of several items)
 * 					24.08.2005 (extend too short lines)
 * 
 * @author sstein
 * 
 * description:
 * 		smooths a selected line, stopping criterion is a maximal line displacement <p>
 * 		line segmentation criterion is angle pi/3<p>
 * 		this algorithm is intended for lines but do work with polygons as well, more or less<p>
 *		the smoothings algorithm is based on smoothing with snakes functions 
 *		(see Steiniger and Meier: 'Snakes: a technique for line smoothing and displacement in map generalisation.', 
 *		2004 ICA Workshop Leicester)<p>
 * 
 *****************************************************/

package mapgen.ui.onselecteditems;

import ch.unizh.geo.algorithms.snakes.SnakesSmoothingLineNew;
import ch.unizh.geo.geomutilities.InterpolateLinePoints;
import ch.unizh.geo.measures.TAFandCurvature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
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
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;

import java.util.ArrayList;
import java.util.Iterator;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;

/**
 * @description:
 * 		smooths a selected line, stopping criterion is a maximal line displacement <p>
 * 		line segmentation criterion is angle pi/3<p>
 * 		this algorithm is intended for lines but do work with polygons as well, more or less<p>
 *		the smoothings algorithm is based on smoothing with snakes functions 
 *		(see Steiniger and Meier: 'Snakes: a technique for line smoothing and displacement in map generalisation.', 
 *		2004 ICA Workshop Leicester)<p>
 *
 * @author sstein
 *
 **/
public class LineSmoothSimpleVersionPlugIn extends AbstractPlugIn implements ThreadedPlugIn{

    private ZoomToSelectedItemsPlugIn myZoom = new ZoomToSelectedItemsPlugIn();
    //private static String T1 = "alpha:";
    //private static String T2 = "beta:";
    private static String T3 = "maximum point displacement in meter:";
    private static String T4 = "do segmentation on line breaks?";
    double alpha = 1;
    double beta = 1;
    double maxPDisp = 0;
    boolean doSegmentation = false;
    private double segmentCurvThreshold = Math.PI/3;
    private int geomType = 0; // 1 = line, 2= polygon, 0 = others,
    private int polyRing = 0;
    private int minPoints = 6;

    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
    	featureInstaller.addMainMenuItem(
    	        this,								//exe
                new String[] {"PlugIns","Map Generalisation","Not Scale Dependent Algorithms", "Lines"}, 	//menu path
                "Line Smoothing Simple Version", //name methode .getName recieved by AbstractPlugIn 
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
	        "Snakes smoothing for a selected line");
	    //dialog.addDoubleField(T1, 1.0, 5);
	    //dialog.addDoubleField(T2, 1.0, 5);
	    dialog.addDoubleField(T3,1.0,5);
	    dialog.addCheckBox(T4,false);
	  }

	private void getDialogValues(MultiInputDialog dialog) {
	    //this.alpha = dialog.getDouble(T1);
	    //this.beta = dialog.getDouble(T2);
	    this.maxPDisp = dialog.getDouble(T3);
	    this.doSegmentation = dialog.getBoolean(T4);
	  }

	protected Layer layer(PlugInContext context) {
		return (Layer) context.getLayerViewPanel().getSelectionManager()
				.getLayersWithSelectedItems().iterator().next();
	}
	
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
        
    		monitor.allowCancellationRequests();
    	    //this.zoom2Feature(context);
    	    this.smooth(context, this.maxPDisp, this.doSegmentation, monitor);
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

	private boolean smooth(PlugInContext context, double maxDisp, boolean segmentate, TaskMonitor monitor) throws Exception{
	    
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
      		System.out.println("========= smooth item: " + count + " ============ ");
      		Feature f = (Feature)iter.next();
	   		Geometry geom = f.getGeometry(); //= erste Geometrie   		
	   		LineString line = null;
	   		Polygon poly = null;
	   		if(geom instanceof LineString){
	   			line = (LineString)geom;
	   			this.geomType = 1;
	   		}
	   		else if(geom instanceof Polygon){
	   			poly = (Polygon)geom;
	   			line = poly.getExteriorRing();
	   			this.geomType = 2;
	   			this.polyRing = 0;
	   		}
	      	else{
	      		this.geomType = 0;
	      		context.getWorkbenchFrame().warnUser("geometry not line or polygon");
	      	}
		    /****************************************/
	       	if (this.geomType > 0){
      			if(line.getNumPoints() <= this.minPoints){
      				line = InterpolateLinePoints.addMiddlePoints(line);
      				System.out.println("LineSmoothSimpleVersion.smooth1: to short " +
      						"line found with" + line.getNumPoints() + "points. Points added");
      			}
		       	int[] pointidx = null;		       	
		       	if(segmentate==true){       	    
		       	    System.out.println("segmentation  = true");
		       	 System.out.println("angle criteria in rad: " + this.segmentCurvThreshold);
		       	    pointidx = this.calcSegments(line, this.segmentCurvThreshold); 
		       	}
		       	else{
		       	    System.out.println("segmentation  = false");
		       	}
		       	//-- smoothing
		   	    SnakesSmoothingLineNew ssmooth = new SnakesSmoothingLineNew(line, maxDisp, this.alpha, this.beta, segmentate, pointidx);
		   	    LineString result = ssmooth.getSmoothedLine();	   	    
		   	    //-- update geometry --------
		   	    if (this.geomType == 1){	//linestring
		   	   	    Coordinate[] coords =line.getCoordinates();
		   	   	    for (int j=0; j < coords.length; j++){
		   	   	    		coords[j] = result.getCoordinateN(j);
		   	   	    }
		   	   	    resultgeom = line;
		   	    }
		   	    else if (this.geomType == 2){ //polygon
		   	    	LineString extring = poly.getExteriorRing(); 
		   	   	    Coordinate[] coords =extring.getCoordinates();
		   	   	    for (int j=0; j < coords.length; j++){
			   	   	    		coords[j] = result.getCoordinateN(j);
		   	   	    }	   	   	    
		   	   	    //-- smooth innner rings if exists, and update as well
		   	    	if (poly.getNumInteriorRing() > 0){
		   	    		for(int j=0; j < poly.getNumInteriorRing(); j++){
			   	    		monitor.report("Calculate for inner Ring: " + j);
		   	    			line = poly.getInteriorRingN(j);
		   	      			if(line.getNumPoints() <= this.minPoints){
		   	      				line = InterpolateLinePoints.addMiddlePoints(line);
		   	      				System.out.println("LineSmoothSimpleVersion.smooth2: to short " +
		   	      						"line found with" + line.getNumPoints() + "points. Points added");
		   	      			}
			   	 	       	pointidx = null;       	
			   		       	if(segmentate==true){       	    
			   		       	    System.out.println("segmentation  = true");
			   		       	    pointidx = this.calcSegments(line, this.segmentCurvThreshold); 
			   		       	}
			   		       	else{
			   		       	    System.out.println("segmentation  = false");
			   		       	}
			   		       	//-- smoothing	   	    			
		   	    	   	    ssmooth = new SnakesSmoothingLineNew(line, maxDisp, this.alpha, this.beta, segmentate, pointidx);
		   	    	   	    result = ssmooth.getSmoothedLine();
		   	    	   	    coords =line.getCoordinates();
		   	    	   	    for (int u=0; u < coords.length; u++){
		   	 	   	   	    		coords[u] = result.getCoordinateN(u);
		   	    	   	    }
		   	    		}
		   	    	}
		   	    	resultgeom = poly;
		   	    }	   	     
			    String mytext = "item: " + count + " / " + noItems + " : smoothing finalized";
			    if (ssmooth.isCouldNotSmooth() == true){mytext = "item: " + count + " / " + noItems + " : not smoothed since to small threshold!!!";}
			    //context.getWorkbenchFrame().setStatusMessage(mytext);
			    monitor.report(mytext);
			    //-- commit changes to undo history
				transaction.setGeometry(count-1, resultgeom);
	       	}//end if : polygon or linestring
      	} //end for loop over selected objects 
		transaction.commit();
        return true;        
	}
	
	private int[] calcSegments(LineString line, double segmentationValue){
	    TAFandCurvature myTafCalc = new TAFandCurvature(line);
	    double[] curv = myTafCalc.getCurvature();
	    ArrayList myPoints = new ArrayList();
	    System.out.println("curvature values:");
	    for(int j=0; j < curv.length; j++){	        
	        System.out.print(curv[j] + ", ");
	        if ( Math.abs(curv[j]) > segmentationValue){
	        	if ((j > 0) && (j < curv.length-1)){
	        		myPoints.add(new Integer(j));
	        	}
	        }
	    }
	    System.out.println(" ");
	    System.out.println("no. of segmentation points: " + myPoints.size());
	    int[] pointIdxList= new int[myPoints.size()];
	    int j=0;
	    for (Iterator iter = myPoints.iterator(); iter.hasNext();) {
            Integer element = (Integer) iter.next();            
            pointIdxList[j] = element.intValue();
            System.out.print(element.intValue() + "  ");
            j=j+1;
        }
	    System.out.println("  ");
	    return pointIdxList;
	} 
  
}
