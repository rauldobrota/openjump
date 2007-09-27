/*----------------    FILE HEADER  ------------------------------------------

Copyright (C) 2001-2005 by:
lat/lon GmbH
http://www.lat-lon.de

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Contact:

Andreas Poth
lat/lon GmbH
Aennchenstraﬂe 19
53177 Bonn
Germany


 ---------------------------------------------------------------------------*/
package de.latlon.deejump.plugin.wfs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.GeometryImpl;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.WorkbenchException;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

import de.latlon.deejump.ui.Messages;
import de.latlon.deejump.util.data.JUMPFeatureFactory;

/**
 * JUMP plug-in providing a GUI for complex filter operations. Whole process is
 * controlled by a FeatureResearchDialog. This contains two panel, one for
 * attribute-based (re-)search and the other allowing the user to choose the 
 * spatial operation to be performed (when he has selected a geometry on the map
 * view). 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class WFSResearchPlugIn extends ThreadedBasePlugIn {

    private static Logger LOG = Logger.getLogger( WFSResearchPlugIn.class );

    private de.latlon.deejump.plugin.wfs.FeatureResearchDialog rd;
    private String wfsUrl;
    
    
    
    private EditingPlugIn editingPlugIn;

    public WFSResearchPlugIn(EditingPlugIn editingPlugIn){
    	this.editingPlugIn = editingPlugIn;
	}

    public void install( PlugInContext context ) throws Exception {

        context.getWorkbenchContext().getWorkbench().getFrame().getToolBar().addPlugIn(
            getIcon(),
            this, 
            createEnableCheck(context.getWorkbenchContext()),
            context.getWorkbenchContext()
        );        	
    }
    
    public boolean execute(PlugInContext context) throws Exception {
    	
        if ( rd == null ){
            rd = new FeatureResearchDialog(
        	        context.getWorkbenchFrame(),
        	        Messages.getString("WFSResearchPlugIn.mainDialogTitle"),
        	        createUrlList( context.getWorkbenchContext() ) );
        }
    
		// get the srs of the current view
        String srs = rd.getGMLGeometrySRS();
        LayerViewPanel lvPanel = context.getLayerViewPanel();
        // get selected geometry(ies)        
        Collection geoCollec = lvPanel.getSelectionManager().getFeatureSelection().getSelectedItems();    
        // then make GML out of it
        org.deegree.model.spatialschema.Geometry gmlGeom;
        
        try {
            gmlGeom = getSelectedGeoAsGML( geoCollec, srs );
        } catch ( WorkbenchException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(context.getWorkbenchFrame(),
		            e.getMessage(),
		            "Error",JOptionPane.ERROR_MESSAGE
		    );
		    return false;
        }
        
		// get the view envelope to perform BBOX operations
        Envelope env = context.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
		rd.setEnvelope( env );
						
		// sets set selected geometry
		// this geometry is used for spatial filter operations
		rd.setSelectedGMLGeometry( gmlGeom );
		rd.setVisible(true);
		wfsUrl = rd.getWfsServer();
		
		if ( !rd.canSearch() ){
		    return false;
		}

		return true;
    }    
      
    /** 
     * @see com.vividsolutions.jump.workbench
     *                   .plugin.ThreadedPlugIn#run(com.vividsolutions.jump.task
     *                       .TaskMonitor, com.vividsolutions.jump.workbench.plugin.PlugInContext)
     */
    public void run(TaskMonitor monitor, PlugInContext context)
            throws Exception {
    	
        monitor.report( Messages.getString( "WFSSearch.searching") );
        
        String request = rd.getWfsRequest();
        
        String crs = rd.getCurrentCrs();
        
        org.deegree.model.feature.FeatureCollection dfc = 
            JUMPFeatureFactory.createDeegreeFCfromWFS( rd.getWfService().getGetFeatureURL() , request );
        
        monitor.report( "Parsing feature collection (size = " + dfc.size() +")"  );
        FeatureCollection dataset = JUMPFeatureFactory.createFromDeegreeFC( dfc );
        
        monitor.report( "Adding Layer"  );
        
		if (dfc != null) {
            
	        LayerManager layerManager = context.getLayerManager();
            
            QualifiedName ftName = rd.getFeatureType();
            QualifiedName geoQN = rd.getChosenGeoProperty();
            
            if( geoQN == null ){
                geoQN = new QualifiedName( "GEOMETRY" );
                LOG.warn( "Could not determine the qualified name of the geometry property. Setting it to 'GEOMETRY'.");
            }
            geoQN = new QualifiedName( ftName.getPrefix(), geoQN.getLocalName(), ftName.getNamespace() );
            
            // need a prefix to infor user it's not all layer
	        String displayName = WFService.WFS_PREFIX + ":" + ftName.getLocalName();	       
	        WFSLayer layer = new WFSLayer(	displayName, 
		            						layerManager.generateLayerFillColor(),
		            						dataset,
		            						layerManager,
                                            ftName,
                                            geoQN, 
                                            crs);
	        
	        //TODO this should be mandatory
	        //FIXME throwing null pointers
	        layer.setServerURL( this.wfsUrl );
	        WFSLayerListener layerListener = new WFSLayerListener( displayName );
	        layerManager.addLayerListener( layerListener );
	        layer.setLayerListener( layerListener );
	        
            DataSource ds = new WFSDataSource();
            
            Map map = new HashMap(2);
            map.put( "SERVER_URL", this.wfsUrl );
            map.put( "REQUEST",  "<![CDATA["+ request +"]]>" );
            ds.setProperties( map );
            
            //TODO fix this "query to come...", "dataSourceQuery"
            DataSourceQuery wfsDSQuery = new DataSourceQuery( ds, "query to come...",
                                                              "dataSourceQuery");
           
            layerManager.addLayer(  StandardCategoryNames.SYSTEM, layer)
                                         .setDataSourceQuery(wfsDSQuery)
                                                  .setFeatureCollectionModified(false);
           	        
	        //TODO set editable
	        boolean editable = rd.isEditable();
	        //FIXME editing Plugin should always be available
	        if ( editingPlugIn != null ){
	        	
		        if ( !editingPlugIn.getToolbox(context.getWorkbenchContext()).isVisible()) {
	                editingPlugIn.execute(context);
	            }
	        }
	        layer.setEditable( editable );
	        
	        if( dataset.size() == JUMPFeatureFactory.getMaxFeatures() ){
	            context.getWorkbenchFrame().warnUser( "Maximale Anzahl Geoobjekte erreicht: " + JUMPFeatureFactory.getMaxFeatures() );
	        }
	        
		} else {
		    
            JOptionPane.showMessageDialog(context.getWorkbenchFrame(),
                    "No data found!",
                    "Info", JOptionPane.WARNING_MESSAGE
                    );
            
		}		
    }
    
    /**Make a GMLGeometry out of geometries inside a collection
     * 
     * @param geoCollec the Collection containing geometries
     * @param srs the spatial reference system of the GML 
     * @return the geometries encoded as GML
     * @throws Exception if something went wrong when building or wrapping the geometries
     */
    private org.deegree.model.spatialschema.Geometry getSelectedGeoAsGML(Collection geoCollec, String srs)
    	throws Exception {

        if ( geoCollec.size() == 0){
            return null;
        }
            
        
	    GeometryFactory gf = new GeometryFactory();
	    Geometry geo =  gf.buildGeometry( (List)geoCollec );
	    if( geo instanceof GeometryCollection ){
		    throw new WorkbenchException(Messages.getString("WFSResearchPlugIn.invalideGeomType"));
		}
	    org.deegree.model.spatialschema.Geometry geoObj = org.deegree.model.spatialschema.JTSAdapter.wrap( geo );
//	    GMLGeometry gg = GMLFactory.createGMLGeometry( geoObj );
        //TODO coord sys
        CoordinateSystem cs = null;

	    ( (GeometryImpl) geoObj ).setCoordinateSystem( cs );
    	return geoObj;
    }
    
    
    /* not used anymore 
    public static final org.deegree.model.spatialschema.Geometry createGMLfromEnvelope(Envelope env, String srs)
	throws Exception {

	    Coordinate[] coords = new Coordinate[5];
	    coords[0] = new Coordinate(env.getMinX(), env.getMinY());
	    coords[1] = new Coordinate(env.getMinX(), env.getMaxY());
	    coords[2] = new Coordinate(env.getMaxX(), env.getMaxY());
	    coords[3] = new Coordinate(env.getMaxX(), env.getMinY());    
	    coords[4] = coords[0];
	    
	    GeometryFactory gf = new GeometryFactory();
	    LinearRing shell =  gf.createLinearRing( coords );
	    Polygon poly = gf.createPolygon( shell, null );
	    GM_Object geoObj = JTSAdapter.wrap( poly );
	    
	    GMLGeometry gg = GMLFactory.createGMLGeometry( geoObj );
	    gg.setSrs(srs);
    	return gg;
    }
        */
    public String getName(){ return "WFS Dialog";}
    
    public ImageIcon getIcon() {
        return new ImageIcon(WFSResearchPlugIn.class.getResource("search.gif"));
    } 
    
    public MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        
        
        MultiEnableCheck mec = new MultiEnableCheck()
            .add( checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck() )
            .add(new EnableCheck() {
            public String check(JComponent component) {
                component.setToolTipText( getName() );
                
                return null;
            }}); 
        
        return mec; 
    }
    
	private List createUrlList( WorkbenchContext workbenchContext ) {
		List urlList = (List) PersistentBlackboardPlugIn.get(workbenchContext)
                                          .get( FeatureResearchDialog.WFS_URL_LIST );
		
		if(urlList == null){
			urlList = new ArrayList(3);
            urlList.add( "http://localhost:8080/deegree2wpvs/ogcwebservice" );
            urlList.add( "http://localhost:8080/deegree2demo/ogcwebservice" );
            urlList.add( "http://10.19.1.167:8088/deegree2/ogcwebservice" );
		} 
		
		return urlList; 
	}
    
}
