/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.latlon.deejump.plugin.wfs;

import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.util.StringTools;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.GeometryImpl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.FeatureCollection;
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

import de.latlon.deejump.ui.I18N;
import de.latlon.deejump.util.data.JUMPFeatureFactory;

/**
 * JUMP plug-in providing a GUI for complex filter operations. Whole process is controlled by a
 * FeatureResearchDialog. This contains two panel, one for attribute-based (re-)search and the other
 * allowing the user to choose the spatial operation to be performed (when he has selected a
 * geometry on the map view).
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class WFSPlugIn extends ThreadedBasePlugIn {

    private static Logger LOG = Logger.getLogger( WFSPlugIn.class );

    private WFSDialog rd;

    private String wfsUrl;

    private EditingPlugIn editingPlugIn;

    /**
     * @param editingPlugIn
     */
    public WFSPlugIn( EditingPlugIn editingPlugIn ) {
        this.editingPlugIn = editingPlugIn;
    }

    /**
     * @param context
     * @throws Exception
     */
    public void install( PlugInContext context )
                            throws Exception {

        // only active if there's a map panel
        MultiEnableCheck mec = createEnableCheck( context.getWorkbenchContext() );

        // crete toolbar button
        context.getWorkbenchContext().getWorkbench().getFrame().getToolBar().addPlugIn( getIcon(), this, mec,
                                                                                        context.getWorkbenchContext() );

        // also create menu item
        /*
         * Disabled due to incompatibility to Vivid solutions JUMP1.2 SH, 2007-05-08
         * context.getFeatureInstaller().addMainMenuItem(this, new String[] { MenuNames.LAYER },
         * getName(), false, getIcon(), mec);
         */
    }

    @Override
    public boolean execute( PlugInContext context )
                            throws Exception {

        if ( rd == null ) {
            rd = new WFSDialog( context.getWorkbenchContext(), context.getWorkbenchFrame(),
                                I18N.getString( "WFSResearchPlugIn.mainDialogTitle" ),
                                createUrlList( context.getWorkbenchContext() ) );
        }

        // get the srs of the current view
        String srs = rd.getWFSPanel().getGMLGeometrySRS();
        LayerViewPanel lvPanel = context.getLayerViewPanel();
        // get selected geometry(ies)
        Collection geoCollec = lvPanel.getSelectionManager().getFeatureSelection().getSelectedItems();
        // then make GML out of it
        org.deegree.model.spatialschema.Geometry selectedGeom;

        try {
            selectedGeom = getSelectedGeoAsGML( geoCollec, srs );
        } catch ( WorkbenchException e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog( context.getWorkbenchFrame(), e.getMessage(), "Error",
                                           JOptionPane.ERROR_MESSAGE );
            return false;
        }

        // get the view envelope to perform BBOX operations
        Envelope env = context.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
        rd.getWFSPanel().setEnvelope( env );

        // sets set selected geometry
        // this geometry is used for spatial filter operations
        rd.getWFSPanel().setComparisonGeometry( selectedGeom );
        rd.setVisible( true );
        if ( !rd.canSearch() ) {
            return false;
        }
        wfsUrl = rd.getWFSPanel().getWfService().getGetFeatureURL();

        return true;
    }

    public void run( TaskMonitor monitor, PlugInContext context )
                            throws Exception {

        monitor.report( I18N.getString( "WFSSearch.searching" ) );

        String request = rd.getWFSPanel().getRequest();

        String crs = rd.getWFSPanel().getGMLGeometrySRS();

        org.deegree.model.feature.FeatureCollection dfc = JUMPFeatureFactory.createDeegreeFCfromWFS(
                                                                                                     rd.getWFSPanel().getWfService(),
                                                                                                     request,
                                                                                                     rd.getWFSPanel().getFeatureType() );

        monitor.report( "Parsing feature collection (size = " + dfc.size() + ")" );
        QualifiedName ftName = rd.getWFSPanel().getFeatureType();
        AbstractWFSWrapper wfs = rd.getWFSPanel().getWfService();

        FeatureCollection dataset;

        if ( wfs.getGeometryProperties( ftName.getLocalName() ).length == 0 ) {
            Point point = new GeometryFactory().createPoint( new Coordinate( 0, 0 ) );
            dataset = JUMPFeatureFactory.createFromDeegreeFC( dfc, point, wfs, ftName );
        } else {
            dataset = JUMPFeatureFactory.createFromDeegreeFC( dfc, null, wfs, ftName );
        }

        monitor.report( "Adding Layer" );

        if ( dfc != null ) {

            LayerManager layerManager = context.getLayerManager();

            QualifiedName geoQN = rd.getWFSPanel().getChosenGeoProperty();

            if ( geoQN == null ) {
                geoQN = new QualifiedName( "GEOMETRY" );
                LOG.warn( "Could not determine the qualified name of the geometry property. Setting it to 'GEOMETRY'." );
            }
            geoQN = new QualifiedName( ftName.getPrefix(), geoQN.getLocalName(), ftName.getNamespace() );

            String displayName = AbstractWFSWrapper.WFS_PREFIX + ":" + ftName.getLocalName();
            WFSLayer layer = new WFSLayer( displayName, layerManager.generateLayerFillColor(), dataset, layerManager,
                                           ftName, geoQN, crs, wfs );

            WFSLayerListener listener = new WFSLayerListener( displayName );
            layer.setLayerListener( listener );
            layerManager.addLayerListener( listener );
            // TODO this should be mandatory
            layer.setServerURL( this.wfsUrl );
            /*
             * WFSLayerListener layerListener = new WFSLayerListener( displayName );
             * layerManager.addLayerListener( layerListener ); layer.setLayerListener( layerListener );
             */
            /*
             * DataSource ds = new WFSDataSource();
             * 
             * Map map = new HashMap(2); map.put( "SERVER_URL", this.wfsUrl ); map.put( "REQUEST", "<![CDATA["+
             * request +"]]>" ); ds.setProperties( map );
             * 
             * //TODO fix this "query to come...", "dataSourceQuery" DataSourceQuery wfsDSQuery =
             * new DataSourceQuery( ds, "query to come...", "dataSourceQuery");
             */
            // fix for threading problem:
            // don't allow event fireing
            /*
             * Disabled due to some rather strange behaviour in OJ1.2b when adding a WFS-Layer with
             * opened system-tab inside the layerlist.
             * 
             * layerManager.setFiringEvents( false );
             */
            // silently add layer
            layerManager.addLayer( StandardCategoryNames.SYSTEM, layer );
            // .setDataSourceQuery(wfsDSQuery)
            // .setFeatureCollectionModified(false);

            /*
             * Disabled due to some rather strange behaviour in OJ1.2b when adding a WFS-Layer with
             * opened system-tab inside the layerlist.
             * 
             * //fire at will layerManager.setFiringEvents( true );
             * 
             * //fire! layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
             */

            // TODO set editable
            boolean editable = true;// rd.isEditable();
            // FIXME editing Plugin should always be available
            if ( editingPlugIn != null ) {

                if ( !editingPlugIn.getToolbox( context.getWorkbenchContext() ).isVisible() ) {
                    editingPlugIn.execute( context );
                }
            }
            layer.setEditable( editable );

            if ( dataset.size() == JUMPFeatureFactory.getMaxFeatures() ) {
                context.getWorkbenchFrame().warnUser(
                                                      I18N.getString( "WFSPlugin.maxnumber" ) + " "
                                                                              + JUMPFeatureFactory.getMaxFeatures() );
            }

        } else {

            JOptionPane.showMessageDialog( context.getWorkbenchFrame(), I18N.getString( "WFSPlugin.nodata" ), "Info",
                                           JOptionPane.WARNING_MESSAGE );

        }

        String[] urls = rd.getWFSPanel().getWFSList().toArray( new String[] {} );
        PersistentBlackboardPlugIn.get( context.getWorkbenchContext() ).put( WFSDialog.WFS_URL_LIST,
                                                                             StringTools.arrayToString( urls, ',' ) );
    }

    /**
     * Make a GMLGeometry out of geometries inside a collection
     * 
     * @param geoCollec
     *            the Collection containing geometries
     * @param srs
     *            the spatial reference system of the GML
     * @return the geometries encoded as GML
     * @throws Exception
     *             if something went wrong when building or wrapping the geometries
     */
    private org.deegree.model.spatialschema.Geometry getSelectedGeoAsGML( Collection geoCollec, String srs )
                            throws Exception {

        if ( geoCollec.size() == 0 ) {
            return null;
        }

        GeometryFactory gf = new GeometryFactory();
        Geometry geo = gf.buildGeometry( (List) geoCollec );
        if ( geo instanceof GeometryCollection ) {
            throw new WorkbenchException( I18N.getString( "WFSResearchPlugIn.invalideGeomType" ) );
        }
        org.deegree.model.spatialschema.Geometry geoObj = org.deegree.model.spatialschema.JTSAdapter.wrap( geo );
        // GMLGeometry gg = GMLFactory.createGMLGeometry( geoObj );
        // TODO coord sys
        CoordinateSystem cs = null;

        ( (GeometryImpl) geoObj ).setCoordinateSystem( cs );
        return geoObj;
    }

    /*
     * not used anymore public static final org.deegree.model.spatialschema.Geometry
     * createGMLfromEnvelope(Envelope env, String srs) throws Exception {
     * 
     * Coordinate[] coords = new Coordinate[5]; coords[0] = new Coordinate(env.getMinX(),
     * env.getMinY()); coords[1] = new Coordinate(env.getMinX(), env.getMaxY()); coords[2] = new
     * Coordinate(env.getMaxX(), env.getMaxY()); coords[3] = new Coordinate(env.getMaxX(),
     * env.getMinY()); coords[4] = coords[0];
     * 
     * GeometryFactory gf = new GeometryFactory(); LinearRing shell = gf.createLinearRing( coords );
     * Polygon poly = gf.createPolygon( shell, null ); GM_Object geoObj = JTSAdapter.wrap( poly );
     * 
     * GMLGeometry gg = GMLFactory.createGMLGeometry( geoObj ); gg.setSrs(srs); return gg; }
     */
    public String getName() {
        return "WFS Dialog";
    }

    public ImageIcon getIcon() {
        return new ImageIcon( WFSPlugIn.class.getResource( "wfs.png" ) );
    }

    public MultiEnableCheck createEnableCheck( final WorkbenchContext workbenchContext ) {
        EnableCheckFactory checkFactory = new EnableCheckFactory( workbenchContext );

        MultiEnableCheck mec = new MultiEnableCheck().add(
                                                           checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck() ).add(
                                                                                                                                 new EnableCheck() {
                                                                                                                                     public String check(
                                                                                                                                                          JComponent component ) {
                                                                                                                                         component.setToolTipText( getName() );

                                                                                                                                         return null;
                                                                                                                                     }
                                                                                                                                 } );

        return mec;
    }

    private String[] createUrlList( WorkbenchContext workbenchContext ) {

        String urls = (String) PersistentBlackboardPlugIn.get( workbenchContext ).get( WFSDialog.WFS_URL_LIST );
        String[] urlList = ( urls == null ) ? null : urls.split( "," );

        if ( urlList == null ) {
            urlList = new String[4];

            urlList[1] = "http://demo.intevation.de/geoserver/wfs";
            urlList[0] = "http://localhost:8080/deegree/services";
            urlList[2] = "http://www.refractions.net:8080/geoserver/wfs/GetCapabilities";
            urlList[3] = "http://localhost:1502?";

        }

        return urlList;
    }

}
