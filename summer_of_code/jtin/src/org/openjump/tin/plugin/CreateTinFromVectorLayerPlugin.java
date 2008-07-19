/**
 * 
 */
package org.openjump.tin.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComboBox;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
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

import org.openjump.core.graph.delauneySimplexInsert.*;
import org.openjump.tin.ImmutableTin;
import org.openjump.tin.TinLayer;
import org.openjump.tin.TriangulatedIrregularNetwork;
import org.openjump.tin.triangulation.ChewUnconstrainedDelaunayTriangulator;




/**
 * @author paradox
 *
 */
public class CreateTinFromVectorLayerPlugin extends AbstractPlugIn implements ThreadedPlugIn {

    private String sName = "Create TIN From Vector Layer";
    private String CLAYER = "select point layer";
    private String sideBarText = "Creates a Delaunay triangulation and returns a TIN layer.";
    private String msgCreateDG = "create triangulation";
    private String msgCreatePolys = "create TIN";
    private String msgNoPoint = "no point geometry";
    private Layer itemlayer = null;
    
	/**
	 * 
	 */
	public CreateTinFromVectorLayerPlugin() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 */
	public CreateTinFromVectorLayerPlugin(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	
	public void initialize (PlugInContext context) throws Exception {
		context.getFeatureInstaller().addMainMenuItem(this, 
				new String[] {"Tools", "Surface Analysis"} , getName(), false, null, 
				createEnableCheck(context.getWorkbenchContext()));
	}
	
    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

        return new MultiEnableCheck()
                        .add(checkFactory.createAtLeastNLayersMustExistCheck(1));
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
	
   private void setDialogValues(MultiInputDialog dialog, PlugInContext context){
	    dialog.setSideBarDescription(this.sideBarText);	    
    	JComboBox addLayerComboBoxBuild = dialog.addLayerComboBox(this.CLAYER, context.getCandidateLayer(0), null, context.getLayerManager());    	
   }

   private void getDialogValues(MultiInputDialog dialog) {
	   this.itemlayer = dialog.getLayer(this.CLAYER);
   }
	
	
	public String getName() {
        return "Create TIN from vector layer";
    }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{            		
	    this.createTin(context, monitor);
	    System.gc();    		
	}

	private boolean createTin(PlugInContext context, TaskMonitor monitor)  throws Exception{
	    System.gc(); //flush garbage collector
	    // --------------------------	    
	    //-- get selected items
	    final Collection<Feature> features = this.itemlayer.getFeatureCollectionWrapper().getFeatures();
	    ArrayList<Point> points = new ArrayList<Point>();
	    int srid = 0;
	    Envelope envelope = new Envelope();
	    
	    //for (Iterator iter = features.iterator(); iter.hasNext();) {
	    for (Feature feature : features) {
            Geometry g = feature.getGeometry();
            if(g instanceof Point){
                points.add((Point)feature.getGeometry());
                srid = g.getSRID();
                envelope.expandToInclude(g.getCoordinate());
            }
            else{
                context.getWorkbenchFrame().warnUser(this.msgNoPoint);
            }
        }
	    if (points.size() > 0){
		    monitor.report(this.msgCreateDG);
		    ChewUnconstrainedDelaunayTriangulator dt = new ChewUnconstrainedDelaunayTriangulator(points);
		    
		    monitor.report(this.msgCreatePolys);
		    TriangulatedIrregularNetwork tin = dt.getTin(srid, envelope);
		    context.getLayerManager().addLayerable(StandardCategoryNames.WORKING, 
		    		new TinLayer(context.getWorkbenchContext(), this.itemlayer.getName()+" TIN",
		    				context.getLayerManager(), tin, srid));
	    }
	    else{
	    	context.getWorkbenchFrame().warnUser(this.msgNoPoint);
	    }
		return true;        		
	}	  	
}
