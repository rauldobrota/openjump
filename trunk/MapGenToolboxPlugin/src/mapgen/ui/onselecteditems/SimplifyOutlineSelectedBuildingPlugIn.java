
/*****************************************************
 * created:  		07.06.2005
 * last modified:  	
 * 
 * @author sstein
 * 
 * description:
 * 
 *****************************************************/

package mapgen.ui.onselecteditems;


import ch.unizh.geo.agents.goals.BuildingGoals;
import mapgen.algorithms.polygons.BuildingOutlineSimplify;
import mapgen.constraints.buildings.BuildingLocalWidth;
import mapgen.constraints.buildings.BuildingShortestEdge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.feature.FeatureSchema;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import java.util.Collection;

/**
 * @description:
 * 		simplifies the outline of a builing
 *      a new layer with the result is created, with an additional Attribute
 * 					
 * @author sstein
 *
 **/
public class SimplifyOutlineSelectedBuildingPlugIn extends AbstractPlugIn implements ThreadedPlugIn{

    private ZoomToSelectedItemsPlugIn myZoom = new ZoomToSelectedItemsPlugIn();
    private static String T1 = "MapScale";
    private static String T2 = "If YES specify maximum No of iterations?";
    private static String T5 = "Do solve iterative?";
    private final String newAttributString = "SimplifyOutline";
    int scale = 1;
    int iterMax = 20;
    boolean solveIter = false;

    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
    	featureInstaller.addMainMenuItem(
    	        this,								//exe
                new String[] {"Map Generalisation", "Scale Dependent Algorithms","Buildings"}, 	//menu path
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
	        "Simplify building outline: map scale is used to detect to short (inlegible) edges ");
	    dialog.addIntegerField(T1, 25000, 7,T1);
	    dialog.addCheckBox(T5,true);
	    dialog.addIntegerField(T2, 50, 4,T2);
	  }

	private void getDialogValues(MultiInputDialog dialog) {
	    this.scale = dialog.getInteger(T1);
	    this.solveIter = dialog.getBoolean(T5);
	    this.iterMax = dialog.getInteger(T2);
	  }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
        
    	    //this.zoom2Feature(context);	    
    	    FeatureCollection fc = this.simplify(context, this.scale, this.solveIter, monitor);
    	    context.addLayer(StandardCategoryNames.WORKING, "simplified buildings", fc);
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
	

	private FeatureCollection simplify(PlugInContext context, int scale, 
	                          boolean solveIterative, TaskMonitor monitor) throws Exception{
	    
	    System.gc(); //flush garbage collector
	    // --------------------------	    
	    //-- get selected items
	    final Collection features = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems();
	    
	    /**
		EditTransaction transaction = new EditTransaction(features, this.getName(), layer(context),
						this.isRollingBackInvalidEdits(context), false, context.getWorkbenchFrame());
	    **/
	    
	    int count=0; int noItems = features.size(); Geometry resultgeom = null;	    
	    //FeatureDataset problematicFeatures = null;
	    FeatureDataset resultFeatures = null;
	    ArrayList problematicEdges = new ArrayList();
       	//List resultList = new ArrayList();
       	FeatureSchema fs = new FeatureSchema();
	    //--get single object in selection to analyse
      	for (Iterator iter = features.iterator(); iter.hasNext();) {
      		count++;
      		Feature f = (Feature)iter.next();
      		System.out.println("========== Item featureID: " + f.getID() + " ==========");      		
      		
      		//-- set schema by using the first selected item
      		//check if attribute name exists
      		boolean attributeExists = false;
      		if (count == 1){      			
      			//-- not sure to do that, since feature schemas of selected objects might be different 
      			fs = copyFeatureSchema(f.getSchema());
       			attributeExists = fs.hasAttribute(this.newAttributString);          			
       			if (attributeExists == false){	      		      			
	      			fs.addAttribute(this.newAttributString, AttributeType.STRING);
	      			//problematicFeatures = new FeatureDataset(fs);
       			}
      			resultFeatures = new FeatureDataset(fs);
      		}
      		//--create new Feature with one new attribute and copy attributvalues
      		Feature fnew = new BasicFeature(fs);
      		Object[] attribs = f.getAttributes();
      		if (attributeExists == false){
	      		Object[] attribsnew = new Object[attribs.length+1];
	      		for (int i = 0; i < attribs.length; i++) {
					attribsnew[i] = attribs[i];
				}
	      		attribsnew[attribs.length]= "init";
	      		fnew.setAttributes(attribsnew);
	      	}
      		else{
      			fnew.setAttributes(attribs);
      		}
      		//--
	   		Geometry geom = f.getGeometry(); //= erste Geometrie
	       	Polygon poly = null;       	
	       	if ( geom instanceof Polygon){
	       		poly = (Polygon) geom; //= erste Geometrie
	    	    // --------------------------
	       		
	           	List conflictListA = new ArrayList();
	           	List conflictListB = new ArrayList();
	           	List conflictListD = new ArrayList();
	           	
	           	//---- detect conflicts
	           	BuildingGoals goals = new BuildingGoals(scale);
	           	BuildingLocalWidth plw = new BuildingLocalWidth(poly, 
	           	        	goals.getMinWidthReal(),goals.getMinWidthFlexibility());
	           	BuildingShortestEdge conflicts = new BuildingShortestEdge(poly, 
	           			goals.getShortestEdgeReal(), goals.getShortestEdgeFlexibility());
	           	//---
	           	if(conflicts.measure.hasConflicts() == true){
		           	context.getWorkbenchFrame().setStatusMessage("conflicts detected! : " + conflicts.measure.getNrOfToShortEdges());           	
		           	conflictListA.addAll(plw.measure.getDispVecPointEdgeLStringList());
		           	conflictListB.addAll(plw.measure.getDispVecPointPointLStringList());
		           	conflictListD.addAll(conflicts.measure.getLineStringList());
		           	//--- solve conflicts ---
		           	//-----------
		           	// no iteration
		           	//-----------
		           	if (solveIterative == false){
		           	    BuildingOutlineSimplify bosimplify = new BuildingOutlineSimplify(poly,
		           	        							conflicts.measure.getConflicList());		           		
		           	    //resultList.add(bosimplify.getOutPolygon());
		           	    fnew.setGeometry(bosimplify.getOutPolygon());
		           	    
			           	conflicts = new BuildingShortestEdge(bosimplify.getOutPolygon(), 
			           			goals.getShortestEdgeReal(), goals.getShortestEdgeFlexibility());
		           	    if ((conflicts.measure.hasConflicts() == true) ||
		           	    		(bosimplify.isAlreadySimple() == true)){
		           	    	fnew.setAttribute(this.newAttributString, "not solved");
		           	    	problematicEdges.addAll(conflicts.measure.getLineStringList());
		           	    }
		           	    else{
		           	    	fnew.setAttribute(this.newAttributString, "simplified");
		           	    }
		           	    /**
		           	    transaction.setGeometry(count-1,bosimplify.getOutPolygon());
		           	    **/
		           	}
		           	else{
		           	 //====================================   
		           	 // if solution should be done iterative
		           	 //====================================

		           	    BuildingOutlineSimplify bosimplify = null;		           		
		           	    int j = 0;
		           	    boolean tosolve = conflicts.measure.hasConflicts();
		           	    while(tosolve == true){
			           	    bosimplify = new BuildingOutlineSimplify(poly,
	        							conflicts.measure.getConflicList());
			           	    poly = bosimplify.getOutPolygon();
			           	    boolean problems = bosimplify.isProblemsEncountered();
			           	    conflicts = new BuildingShortestEdge(poly, 
				           			    goals.getShortestEdgeReal(), goals.getShortestEdgeFlexibility()); 
			           	    tosolve = conflicts.measure.hasConflicts();
			           	    //--notbremse:
			           	    j = j + 1;
			           	    //-- stop at max iterations 
			           	    if(j == this.iterMax){
			           	        tosolve = false;
			           	        context.getWorkbenchFrame().warnUser("stopped at step: " + j);
			           	    }
			           	    if(bosimplify.isAlreadySimple() == true){
			           	        tosolve = false;
			           	    }		
			           	    //-- stop if only one not solveable conflicts appears 
			           	    //   to avoid unnecessary loop till end   
			           	    if(problems && (conflicts.measure.getConflicList().size() == 1 )){
			           	        tosolve = false;
			           	        context.getWorkbenchFrame().warnUser("stopped at step: " + j);
			           	    }
			           	    if (tosolve == false){
				           	    //--objects which still have problems
			           	    	if(conflicts.measure.hasConflicts()== true){
			           	    		fnew.setAttribute(this.newAttributString, "not solved");
			           	    		/*
				           	    	Feature fnew = (Feature)f.clone();
				           	    	fnew.setGeometry(bosimplify.getOutPolygon());
				           	    	problematicFeatures.add(fnew);
				           	    	*/
				           	    	problematicEdges.addAll(conflicts.measure.getLineStringList());
			           	    	}//--objects with solved problems
			           	    	else{
			           	    		fnew.setAttribute(this.newAttributString, "simplified");
			           	    	}
			           	    	//--store geometry
			           	    	fnew.setGeometry(bosimplify.getOutPolygon());
			           	    }
			           	    //-- visualisation
			           	    /*
			               	List stepList = new ArrayList();
			           	    stepList.add(0,bosimplify.getOutPolygon());
				    	    FeatureCollection myCollD = FeatureDatasetFactory.createFromGeometry(stepList);
				    	    if (myCollD.size() > 0){
				    		    context.addLayer(StandardCategoryNames.WORKING, "stepList", myCollD);
				    		    }	    	  
				    	    */
		           	    }//end while
		           	    /**
		           	    resultList.add(bosimplify.getOutPolygon());
		           	    transaction.setGeometry(count-1,bosimplify.getOutPolygon());
		           	    **/
		           	} //--end iterative solution
		           	// ===== visulisation =====
		           	/**
		    	    FeatureCollection myCollA = FeatureDatasetFactory.createFromGeometry(conflictListA);
		    	    if (myCollA.size() > 0){
		    		    context.addLayer(StandardCategoryNames.WORKING, "point-edge conflicts", myCollA);
		    		    }	           	
	           		FeatureCollection myCollC = FeatureDatasetFactory.createFromGeometry(conflictListB);
	           		if (myCollC.size() > 0){
	           		    context.addLayer(StandardCategoryNames.WORKING, "point-point conflicts", myCollC);
	    		    }
	    		               		           	           	
	           		FeatureCollection myCollD = FeatureDatasetFactory.createFromGeometry(conflictListD);
	           		if (myCollD.size() > 0){
	           		    context.addLayer(StandardCategoryNames.WORKING, "shortest Edges", myCollD);
	    		    } 
	           		
		    	    FeatureCollection myCollB = FeatureDatasetFactory.createFromGeometry(resultList);
		    	    if (myCollB.size() > 0){
		    		    context.addLayer(StandardCategoryNames.WORKING, "result", myCollB);
		    		    }
		    		**/
		        }// ========================       		
	           	else{
	           	    context.getWorkbenchFrame().setStatusMessage("no conflict detected!");
	           	    fnew.setAttribute(this.newAttributString, "no conflict");
	           	}
	       	}
	       	else{
	       	    context.getWorkbenchFrame().warnUser("no polygon selected");
           	    fnew.setAttribute(this.newAttributString, "no polygon");
	       	}
       	    resultFeatures.add(fnew);
		    String mytext = "item: " + count + " / " + noItems + " : simplification finalized";
		    monitor.report(mytext);	       	
      	}//  end loop for selection
      	/**
       	transaction.commit();
       	**/      	
	    if (problematicEdges.size() > 0){
		    FeatureCollection myCollE = FeatureDatasetFactory.createFromGeometry(problematicEdges);
		    context.addLayer(StandardCategoryNames.WORKING, "problematic edges", myCollE);
		    }
	    //context.addLayer(StandardCategoryNames.WORKING, "simplified buildings", resultFeatures);	    
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
