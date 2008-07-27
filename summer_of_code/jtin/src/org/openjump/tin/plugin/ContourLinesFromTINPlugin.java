package org.openjump.tin.plugin;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComboBox;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;


import org.openjump.tin.TinLayer;
import org.openjump.tin.TriangulatedIrregularNetwork;
import org.openjump.tin.triangulation.ChewUnconstrainedDelaunayTriangulator;
import org.openjump.tin.ui.MultiInputDialog;
import org.openjump.tin.i18n.I18NPlug;

public class ContourLinesFromTINPlugin extends AbstractPlugIn implements
		ThreadedPlugIn {

	    
	private String menuName = "Countour Lines From TIN";
    private String chooseLayer = "Select TIN Layer";
    private String sideBarText = "Creates a layer of contour lines spaced at the given Z interval over the given TIN";
    private String deltaHeightText = "Height Change Between Contour Lines";
    private String startingHeightText = "Starting Height";
    
    //
    private String msgCreateDG = "Triangulating";
    private String msgCreatePolys = "Building TIN";
    private String msgNoPoint = "no point geometry";
    private String fileNameBox = "File name for created TIN";
    //
    
    private Layerable itemlayer = null;
    
    private static String pluginname = "contourlinesfromtinplugin";
    
    private double deltaHeight = 100.0;
    private double startingHeight = 0.0;


	/**
	 * @param name
	 */
    public ContourLinesFromTINPlugin() { };
	public ContourLinesFromTINPlugin(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	
	public void initialize (PlugInContext context) throws Exception {
        I18NPlug.setPlugInRessource(pluginname, "org.openjump.tin.i18n.resources.contourlinesfromtinplugin");
        
		//-- initialize country specific strings 
		if (I18NPlug.jumpi18n == true) {
			this.menuName = I18NPlug.get(pluginname, "ContourLinesFromTINPlugin.ContourLinesFromTIN");
			this.chooseLayer = I18NPlug.get(pluginname, "ContourLinesFromTINPlugin.SelectTINLayer");
			this.sideBarText = I18NPlug.get(pluginname, "ContourLinesFromTINPlugin.SideBarText");
			this.deltaHeightText = I18NPlug.get(pluginname, "ContourLinesFromTINPlugin.HeightChangeBetweenContourLines");
			this.startingHeightText = I18NPlug.get(pluginname, "ContourLinesFromTINPlugin.StartingHeight");
		}
		
		context.getFeatureInstaller().addMainMenuItem(this, 
				new String[] {MenuNames.TOOLS, MenuNames.TOOLS_GENERATE}, 
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
	    List<TinLayer> tinLayers = (List<TinLayer>)context.getLayerManager().getLayerables(TinLayer.class);	    
	    dialog.addLayerableComboBox(this.chooseLayer, tinLayers.get(0), null, tinLayers);
	    dialog.addPositiveDoubleField(this.deltaHeightText, this.deltaHeight, 6);
    	dialog.addDoubleField(this.startingHeightText, this.startingHeight, 6);
   }

   private void getDialogValues(MultiInputDialog dialog) {
	   this.itemlayer = dialog.getLayerable(this.chooseLayer);
	   this.deltaHeight = dialog.getDouble(this.deltaHeightText);
	   this.startingHeight = dialog.getDouble(this.startingHeightText);
   }
	
	
	public String getName() {
        return this.menuName;
    }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{            		
	    this.extractContourLines(context, monitor);
	    System.gc();    		
	}

	private boolean extractContourLines(PlugInContext context, TaskMonitor monitor) {
	    System.gc(); //flush garbage collector
	    
	    if (!(this.itemlayer instanceof TinLayer))
	    	return false;
	    TriangulatedIrregularNetwork tin = ((TinLayer)this.itemlayer).getTin();
	    
	    
	    FeatureSchema schema = new FeatureSchema();
        schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
	    FeatureDataset features = new FeatureDataset(schema); 
	    
	    for (double i = this.startingHeight; i < tin.getMaxBoundingCoordinate().z; i=i+this.deltaHeight) {
	    	if (i < tin.getMinBoundingCoordinate().z) 
	    		continue;
	    	features.add(FeatureUtil.toFeature(tin.getContourLinesAtHeight(i), schema));
	    }
	    
	    String newLayerName = this.itemlayer.getName() + " contours";
	    
	    context.getLayerManager().addLayer(StandardCategoryNames.WORKING, newLayerName, features);

		return true;
	}

}



