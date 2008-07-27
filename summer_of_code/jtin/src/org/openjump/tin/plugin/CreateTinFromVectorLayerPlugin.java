/**
 * 
 */
package org.openjump.tin.plugin;

import java.awt.Component;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRootPane;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.Assert;
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
//import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.MenuNames;

import org.openjump.tin.TinLayer;
import org.openjump.tin.TriangulatedIrregularNetwork;
import org.openjump.tin.triangulation.ChewUnconstrainedDelaunayTriangulator;
import org.openjump.tin.ui.MultiInputDialog;
import org.openjump.tin.ui.SelectFilePanel;
import org.openjump.tin.i18n.I18NPlug;
import org.openjump.tin.io.JTFLayout;
import org.openjump.tin.io.JTFWriter;




/**
 * @author paradox
 *
 */
public class CreateTinFromVectorLayerPlugin extends AbstractPlugIn implements ThreadedPlugIn {

    private String menuName = "Create TIN From Vector Layer";
    private String chooseLayer = "select point layer";
    private String sideBarText = "Creates a TIN Layer from a Delaunay Triangulation of the selected Point Layer";
    private String msgCreateDG = "Triangulating";
    private String msgSaveTIN = "Saving TIN to disk";
    private String msgCreatePolys = "Building TIN";
    private String msgNoPoint = "no point geometry";
	private String msgErrorSavingTIN = "Error saving TIN";
   private String fileNameBox = "File name for created TIN";
    private String tinFileDescription = "OpenJUMP TIN file";
    private Layer itemlayer = null;
    private String fileName;
    
    private static String pluginname = "createtinfromvectorlayerplugin";

    
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
        I18NPlug.setPlugInRessource(pluginname, "org.openjump.tin.i18n.resources.createtinfromvectorlayerplugin");
        
		//-- initialize country specific strings 
		if (I18NPlug.jumpi18n == true) {
			this.menuName = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.CreateTinFromVectorLayer");
			this.chooseLayer = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.SelectPointLayer");
			this.sideBarText = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.CreatesATINLayerFromADelaunayTriangulationOfSelectedPoints");
			this.msgCreateDG = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.Triangulating");
			this.msgSaveTIN = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.SavingTINToDisk");
			this.msgErrorSavingTIN = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.ErrorSavingTINToDisk");
			this.msgCreatePolys = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.BuildingTIN");
			this.msgNoPoint = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.NoPointGeometry");
			this.fileNameBox = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.FileNameForTIN");
			this.tinFileDescription = I18NPlug.get(pluginname, "CreateTinFromVectorLayerPlugin.OpenJUMPTINFile");
		}
		
		context.getFeatureInstaller().addMainMenuItem(this, 
				new String[] {MenuNames.LAYER}, 
				getName(), false, null, 
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
    	dialog.addLayerComboBox(this.chooseLayer, context.getCandidateLayer(0), null, context.getLayerManager());

    	dialog.addRow(this.fileNameBox, new JLabel(this.fileNameBox), 
    			new SelectFilePanel(this.tinFileDescription, new String[] {JTFLayout.FILE_NAME_EXTENSION}, false),
    			null, null);
   }

   private void getDialogValues(MultiInputDialog dialog) {
	   this.itemlayer = dialog.getLayer(this.chooseLayer);

	   SelectFilePanel sfp = (SelectFilePanel)dialog.getComponent(this.fileNameBox);
	   this.fileName = sfp.getSelectedPath();
   }
	
	
	public String getName() {
        return this.menuName;
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
		    
	    	monitor.report(this.msgSaveTIN);
	    	try {
	    		FileOutputStream fout = new FileOutputStream(this.fileName);
	    		JTFWriter.write(tin, fout);
	    	}
	    	catch (Exception e) {
	    		context.getWorkbenchFrame().warnUser(this.msgErrorSavingTIN+": "+e.toString());
	    	}	    			    
		    
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
