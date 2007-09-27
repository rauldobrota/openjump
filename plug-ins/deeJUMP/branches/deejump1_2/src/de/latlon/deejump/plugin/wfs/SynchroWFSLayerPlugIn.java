/*
 * Created on 07.10.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.latlon.deejump.plugin.wfs;

import java.util.Collection;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.spatialschema.EnvelopeImpl;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;

import de.latlon.deejump.ui.Messages;
import de.latlon.deejump.util.data.JUMPFeatureFactory;

/**
 * @author sncho
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SynchroWFSLayerPlugIn extends ThreadedBasePlugIn {
	 
	private static Logger LOG = Logger.getLogger( SynchroWFSLayerPlugIn.class );
	private String wfsUrl;
	private WFSLayer layer ;
	   
	public SynchroWFSLayerPlugIn(){ }
	    	
	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn#run(com.vividsolutions.jump.task.TaskMonitor, com.vividsolutions.jump.workbench.plugin.PlugInContext)
	 */
	public void run(TaskMonitor monitor, PlugInContext context)
			throws Exception {
	
		monitor.report("SynchroWFSLayerPlugIn.name");
	    reloadLayer(monitor, context, layer);	
	}
	
	public void install( PlugInContext context ) throws Exception {

        context.getWorkbenchContext().getWorkbench().getFrame().getToolBar().addPlugIn(
            getIcon(),
            this, 
            createEnableCheck(context.getWorkbenchContext()),
            context.getWorkbenchContext()
        );        	
    }
	 
	public ImageIcon getIcon() {
		return new ImageIcon(SynchroWFSLayerPlugIn.class.getResource("refresh.png"));
	} 
	 
    public String getNameWithMnemonic() {
        return StringUtil.replace(getName(), "S", "&S", false);
    }	 
	
    public MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
    	
    	EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
	    return new MultiEnableCheck()
			.add( checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck())
			.add( UpdateWFSLayerPlugIn.createExactlyNWfsLayersMustBeSelectedCheck( workbenchContext, 1) );
	}

	public boolean execute(PlugInContext context) throws Exception {

		Collection collec =  context.getLayerManager().getLayersWithModifiedFeatureCollections();
        Layer candidatelayer = context.getSelectedLayer( 0 );
        
        if ( candidatelayer instanceof WFSLayer ) {
            layer = (WFSLayer)candidatelayer;
        } else {
        	layer = null;
            return false;
        }
        this.wfsUrl = layer.getServerURL();        	  		
		return true;
    }    
	  
	  
	private void reloadLayer(TaskMonitor monitor, PlugInContext context, WFSLayer layer)
	  		        throws Exception {
	  
		monitor.report(Messages.getString("SynchroWFSLayerPlugIn.name"));	  		       
		Envelope jEnv = context.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();	  		  
	  	org.deegree.model.spatialschema.Envelope env = 
	  		GeometryFactory.createEnvelope( 
              org.deegree.model.spatialschema.GeometryFactory.createPosition( jEnv.getMinX() ,jEnv.getMinY() ),
			  org.deegree.model.spatialschema.GeometryFactory.createPosition(jEnv.getMaxX(), jEnv.getMaxY()),null);
	  		       		  		
        QualifiedName qName = layer.getQualifiedName();
        String crs = layer.getCrs();
        
	  	GetFeature gfr = 
	  		JUMPFeatureFactory.createFeatureRequest("1.0.0", qName, env);
	  		
	  	org.deegree.model.feature.FeatureCollection dfc = 
	  		JUMPFeatureFactory.createDeegreeFCfromWFS( wfsUrl, gfr );
	  		     
	  	FeatureCollection dataset = JUMPFeatureFactory.createFromDeegreeFC( dfc, null );
	  		       
	  	Feature f = (Feature)dataset.getFeatures().iterator().next();
	  	Geometry g = f.getGeometry();
	  	Class geoClass = g.getClass();
	  	
	  	if( g instanceof GeometryCollection ){
	  		// then choose the first geometry as THE geo type
	  		geoClass = ((GeometryCollection)g).getGeometryN( 0 ).getClass();           
	  	}
	  		       
	  	if (dataset != null) {	  		          
	  		        	
	  		LayerManager layerManager = context.getLayerManager();
	  		String displayName = layer.getName();
	  		           
	  		WFSLayerListener layerListener = new WFSLayerListener( displayName );
	  		//layerManager.addLayerListener( layerListener );
	  		String cat = layerManager.getCategory( layer ).getName();
	  		//layerManager.addOrReplaceLayer( cat, layer.getName(), dataset);
	  		layerManager.remove((Layerable)layer);
	  		layer = new WFSLayer(displayName,
	  		            		layerManager.generateLayerFillColor(),
	  		            		dataset,
								layerManager,
								((WFSLayer)layer).getQualifiedName(),
								((WFSLayer)layer).getGeoPropertyName(),
                                crs);
	  		           	  		
	  		layer.setServerURL( this.wfsUrl );
	  		layerManager.addLayer( cat, layer).setDataSourceQuery(null).setFeatureCollectionModified(false);
	  		layerManager.addLayerListener( layerListener );
	  	}
	} 
}	       
	  

