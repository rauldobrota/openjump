/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
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
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package de.latlon.deejump.plugin.wms;

import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;

import org.deegree.model.geometry.GM_Envelope;
import org.deegree_impl.model.cs.ConvenienceCSFactory;
import org.deegree_impl.model.ct.GeoTransformer;
import org.deegree_impl.model.geometry.GeometryFactory;
import org.deegree_impl.model.geometry.JTSAdapter;
import org.opengis.cs.CS_CoordinateSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerWizardPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.OneSRSWizardPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel;
import com.vividsolutions.jump.workbench.ui.wizard.WizardDialog;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;
import com.vividsolutions.wms.BoundingBox;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.WMService;


public class DeeAddWMSQueryPlugIn extends AbstractPlugIn {

    private String lastWMSVersion = WMService.WMS_1_1_1;

    private List validURLsList;

    //[UT] 23.05.2005 added for Mainz
    private Envelope bigEnv;

    private String chosenSRS = null;
    
    public DeeAddWMSQueryPlugIn() {
    }
    public MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(
                checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()
                );
    }

    public void install( PlugInContext context ) throws Exception {

        context.getWorkbenchContext().getWorkbench().getFrame().getToolBar().addPlugIn(
            getIcon(),
            this, 
            createEnableCheck( context.getWorkbenchContext() ),
            context.getWorkbenchContext()
        );        	
    }
    
    private List toLayerNames(List mapLayers) throws Exception {
    	
        ArrayList names = new ArrayList();
        
        for (Iterator i = mapLayers.iterator(); i.hasNext();) {
            
        	MapLayer layer = (MapLayer) i.next();
            BoundingBox bb = layer.getBoundingBox();
                         
            Envelope env = new Envelope( new Coordinate(bb.getMinX(),bb.getMinY()),
                    					 new Coordinate(bb.getMaxX(),bb.getMaxY()));            
            String srs = bb.getSRS();

            if ( ! ( "EPSG:4326".equalsIgnoreCase(srs) || "LatLon".equalsIgnoreCase(srs) )) {             	
            	env = transform(bb.getSRS(), "EPSG:4326", env);
           }

            //env = transform("EPSG:31466", "EPSG:4326", env);
            //System.out.println("env = "+env.getMinX()+" "+env.getMinY()+env.getMaxX()+" "+env.getMaxY());

            if( bigEnv == null ){            	
                bigEnv = env;                            
            }
            bigEnv.expandToInclude(env);
            names.add(layer.getName());
        }

        return names;
    }

    public boolean execute(final PlugInContext context)
        throws Exception {
        reportNothingToUndoYet(context);
		initUrlList( context.getWorkbenchContext() );
        WizardDialog d = new WizardDialog(context.getWorkbenchFrame(),
        		I18N.get("ui.plugin.wms.AddWMSQueryPlugIn.connect-to-web-map-server"), context.getErrorHandler());
        d.init(new WizardPanel[] {
                new DeeURLWizardPanel(validURLsList, lastWMSVersion), new MapLayerWizardPanel(),
                new SRSWizardPanel(), new OneSRSWizardPanel()
            });

        //Set size after #init, because #init calls #pack. [Jon Aquino]
        d.setSize(500, 400);
        GUIUtil.centreOnWindow(d);
        d.setVisible(true);
        if (!d.wasFinishPressed()) {
            return false;
        }
        
        chosenSRS = (String) d.getData(SRSWizardPanel.SRS_KEY) ; 
        final WMSLayer layer = new WMSLayer(context.getLayerManager(),
                (WMService) d.getData(URLWizardPanel.SERVICE_KEY),
                (String) d.getData(SRSWizardPanel.SRS_KEY),
                toLayerNames((List) d.getData(MapLayerWizardPanel.LAYERS_KEY)),
                ((String) d.getData(URLWizardPanel.FORMAT_KEY)));
        execute(new UndoableCommand(getName()) {
                public void execute() {
                    Collection selectedCategories = context.getLayerNamePanel()
                                                           .getSelectedCategories();
                    context.getLayerManager().addLayerable(selectedCategories.isEmpty()
                        ? StandardCategoryNames.WORKING
                        : selectedCategories.iterator().next().toString(), layer);
                    if ( chosenSRS != null ){
                        doDisplacement( context, chosenSRS);
                    }
                }

                public void unexecute() {
                    context.getLayerManager().remove(layer);
                }
            }, context);
        if( !validURLsList.contains(d.getData(DeeURLWizardPanel.WMS_URL_LIST))){
            validURLsList.add( d.getData(DeeURLWizardPanel.WMS_URL_LIST));
        }
        lastWMSVersion = (String) d.getData( URLWizardPanel.VERSION_KEY );
        bigEnv = null;
        chosenSRS = null;
        
        return true;
    }
    
    //[UT] 23.05.2005 added for Mainz
    private void doDisplacement(PlugInContext ctxt, String targetCS){
        
        String stdCS = "EPSG:4326";
		
        
        if( !stdCS.equalsIgnoreCase(targetCS )){  //codition: bigEnv is in epsg:4326==> transform it
		    bigEnv = transform( stdCS, targetCS, bigEnv);
        }
        
        
        try {
            ctxt.getLayerViewPanel().getViewport().zoom(bigEnv);
        } catch (NoninvertibleTransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
        
        /*if( !stdCS.equalsIgnoreCase(targetCS )){
		    bigEnv = transform( stdCS, targetCS, bigEnv);
		    
		    /*
		    bigEnv = new Envelope(
		            bigEnv.getMinX(),
		            bigEnv.getMaxX(),
		            bigEnv.getMinY(),
		            bigEnv.getMaxY());
		    */
		    /*
		    try {
	            ctxt.getLayerViewPanel().getViewport().zoom(bigEnv);
	        } catch (NoninvertibleTransformException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }   
		}*/
		
		
    }
    
    //[UT] 23.05.2005 added for Mainz
    private Envelope transform( String sourceCS, String targetCS, Envelope inEnv){
    	
        Envelope env = new Envelope( 
                new Coordinate(inEnv.getMinX(),inEnv.getMinY()),
                new Coordinate(inEnv.getMaxX(),inEnv.getMaxY()));

        try {
            GM_Envelope de = GeometryFactory.createGM_Envelope( 
                    inEnv.getMinX(), inEnv.getMinY(),
                    inEnv.getMaxX(), inEnv.getMaxY()); 

            GeoTransformer gt = new GeoTransformer(targetCS);

            de = gt.transformEnvelope(de, sourceCS); 
            
            CS_CoordinateSystem cs = ConvenienceCSFactory.getInstance().getOGCCSByName(sourceCS);
            Point p1 =  (Point)JTSAdapter.export( GeometryFactory.createGM_Point (de.getMin().getX(),de.getMin().getY(), cs ));
            Point p2 =  (Point)JTSAdapter.export( GeometryFactory.createGM_Point (de.getMax().getX(),de.getMax().getY(), cs ));

            env = new Envelope( 
                    new Coordinate(p1.getX(),p1.getY()),
                    new Coordinate(p2.getX(),p2.getY()));
                    
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // better be on the safe side
            return env;
        }
        
        return env; 
    }
    public ImageIcon getIcon() {
        return new ImageIcon(DeeAddWMSQueryPlugIn.class.getResource("wms.gif"));
    } 
    public String getName(){
        return I18N.get("com.vividsolutions.jump.workbench.ui.plugin.wms.AddWMSQueryPlugIn");
    }
    /**
	 * 
	 */
	private void initUrlList( WorkbenchContext workbenchContext ) {
		 List lastValidURLsList = (List) PersistentBlackboardPlugIn.get(workbenchContext)
                                           .get( DeeURLWizardPanel.WMS_URL_LIST );
		 if(lastValidURLsList == null){
        	validURLsList = new ArrayList();
            validURLsList.add( "http://demo.deegree.org:8080/deegree/wms");
            validURLsList.add( "http://localhost:8080/deegreewms/wms");
            validURLsList.add( "http://www.geoserver.nrw.de/GeoOgcWms1.3/servlet/TK25");
       }
       else{
       	   validURLsList = lastValidURLsList;
       }
	}
}
