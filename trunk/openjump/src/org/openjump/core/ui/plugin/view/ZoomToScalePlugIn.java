/*****************************************************
 * created:  		04.01.2005
 * last modified:  	
 * 
 * description:
 *   zooms to a given map scale, which is received from an input dialog 
 * 
 *****************************************************/

package org.openjump.core.ui.plugin.view;
import java.awt.Toolkit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.Viewport;


/**
 * @description
 * 	zooms to a given map scale, received from a input dialog
 * 
 * @author sstein
 *
 */
public class ZoomToScalePlugIn extends AbstractPlugIn{    

    private String T1 = "scale:";
    int scale = 0;
    double oldHorizontalScale = 0; // is calculated for panel-width (not heigth!!)
    double modelWidth = 0;
    double panelWidth = 0; 
    double SCREENRES = Toolkit.getDefaultToolkit().getScreenResolution(); //72 dpi or 96 dpi or ..     
    double INCHTOCM = 2.54; //cm


    public void initialize(PlugInContext context) throws Exception {
    	
		this.T1 = I18N.get("org.openjump.core.ui.plugin.view.ZoomToScalePlugIn.scale") + ": ";
	    context.getFeatureInstaller().addMainMenuItem(this,
	        new String[]
			{MenuNames.VIEW},
	        I18N.get("org.openjump.core.ui.plugin.view.ZoomToScalePlugIn.zoom-to-scale")+"{pos:9}", 
			false, 
			null, 
			null);
    }
    
    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        
        return new MultiEnableCheck()
                        .add(checkFactory.createTaskWindowMustBeActiveCheck());
        
    }
    
    
	public boolean execute(PlugInContext context) throws Exception{
	    
        Viewport port = context.getLayerViewPanel().getViewport();
        this.panelWidth = port.getPanel().getWidth(); //pixel
        this.modelWidth = port.getEnvelopeInModelCoordinates().getWidth(); //m
        //-----
        // example:
        // screen resolution: 72 dpi
        // 1 inch = 2.54 cm
        // ratio = 2.54/72 (cm/pix) ~ 0.35mm
        // mapLength[cm] = noPixel * ratio
        // scale = realLength *100 [m=>cm] / mapLength
        //-----                            
        this.oldHorizontalScale = this.modelWidth*100 / (this.INCHTOCM / this.SCREENRES * this.panelWidth);
        
	    MultiInputDialog dialog = new MultiInputDialog(
	            context.getWorkbenchFrame(), 
	            I18N.get("org.openjump.core.ui.plugin.view.ZoomToScalePlugIn.zoom-to-scale"), 
				true);
	        setDialogValues(dialog, context);
	        GUIUtil.centreOnWindow(dialog);
	        dialog.setVisible(true);
	        if (! dialog.wasOKPressed()) { return false; }
	        getDialogValues(dialog);               
        
	    //-- get zoom factor
        double factor = this.scale/this.oldHorizontalScale;

        //--calculating new screen using the envelope of the corner LineString 
        Envelope oldEnvelope = port.getEnvelopeInModelCoordinates();

        double xc = 0.5*(oldEnvelope.getMaxX() + oldEnvelope.getMinX());
        double yc = 0.5*(oldEnvelope.getMaxY() + oldEnvelope.getMinY());
        double xmin = xc - 1/2.0 * factor * oldEnvelope.getWidth();
        double xmax = xc + 1/2.0 * factor * oldEnvelope.getWidth();
        double ymin = yc - 1/2.0 * factor * oldEnvelope.getHeight();
        double ymax = yc + 1/2.0 * factor * oldEnvelope.getHeight();
        Coordinate[] coords = new Coordinate[]{new Coordinate(xmin,ymin), 
                								new Coordinate(xmax,ymax)};
        Geometry g1 = new GeometryFactory().createLineString(coords);       
        port.zoom(g1.getEnvelopeInternal());
        
	    return true;
	}
	
    private void setDialogValues(MultiInputDialog dialog, PlugInContext context)
	  {
        //dialog.addLabel("actual scale in horizontal direction: " + (int)this.oldHorizontalScale);
        dialog.addLabel(I18N.get("org.openjump.core.ui.plugin.view.ZoomToScalePlugIn.actual-scale-in-horizontal-direction") + ": " +(int)this.oldHorizontalScale);
        dialog.addSeparator();
	    //dialog.addLabel("set new scale to zoom:");
	    dialog.addLabel(I18N.get("org.openjump.core.ui.plugin.view.ZoomToScalePlugIn.set-new-scale-to-zoom") + ":");
	    dialog.addIntegerField(T1, 25000, 7,T1);	    
	  }

	private void getDialogValues(MultiInputDialog dialog) {
	    this.scale = dialog.getInteger(T1);    	    
	  }
    
}
