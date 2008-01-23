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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.deegree.framework.util.StringTools;
import org.deegree.model.feature.GMLFeatureCollectionDocument;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

import de.latlon.deejump.util.data.JUMPFeatureFactory;

/**
 * 
 * This class is basically copied from FeatureInfoTool to extend functionality so as to provide
 * FeatureInfo on WMSLayers.
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */

public class LayerAndWMSFeatureInfoTool extends SpecifyFeaturesTool {

    private static final Logger LOG = Logger.getLogger( LayerAndWMSFeatureInfoTool.class );

    /**
     * 
     */
    public static final ImageIcon ICON = new ImageIcon( LayerAndWMSFeatureInfoTool.class.getResource( "inform.gif" ) );

    private ColorHelper colorHelper = new ColorHelper();

    /**
     * 
     */
    public LayerAndWMSFeatureInfoTool() {
        setColor( Color.magenta );
    }

    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Cursor getCursor() {
        return createCursor( IconLoader.icon( "InfoCursor.gif" ).getImage() );
    }

    @Override
    protected void gestureFinished()
                            throws Exception {
        reportNothingToUndoYet();

        InfoFrame infoFrame = getTaskFrame().getInfoFrame();
        if ( !wasShiftPressed() ) {
            infoFrame.getModel().clear();
        }
        Map<?, ?> map = layerToSpecifiedFeaturesMap();
        Iterator<?> i = map.keySet().iterator();
        while ( i.hasNext() ) {
            Layer layer = (Layer) i.next();

            if ( layer.getName().equals( FenceLayerFinder.LAYER_NAME ) ) {
                continue;
            }
            Collection<?> features = (Collection) map.get( layer );
            infoFrame.getModel().add( layer, features );

        }

        // do the same, but for wms layers
        map = wmsLayerToSpecifiedFeaturesMap( getPanel().getLayerManager().getLayerables( WMSLayer.class ).iterator() );

        if ( map != null ) {
            i = map.keySet().iterator();

            while ( i.hasNext() ) {

                Layerable layer = (Layerable) i.next();

                FeatureCollection fc = (FeatureCollection) map.get( layer );
                if ( fc != null ) {

                    Layer l = new Layer( "WMS:" + layer.getName(), colorHelper.getNextColor(), fc,
                                         getPanel().getLayerManager() );

                    // here get all features from the feture collec and...
                    Set<Object> intersectingFeatures = new HashSet<Object>();
                    for ( Iterator<?> iter = fc.iterator(); iter.hasNext(); ) {
                        Object o = iter.next();
                        intersectingFeatures.add( o );

                    }

                    // ...pass it on
                    infoFrame.getModel().add( l, intersectingFeatures );

                }

            }

            infoFrame.surface();
        }
    }

    private static String createFeatureInfoRequest( WMSLayer wmsLayer, LayerViewPanel panel, Point2D clickXY )
                            throws MalformedURLException, IOException {
        String url = wmsLayer.createRequest( panel ).getURL().toExternalForm();
        url = url.replace( wmsLayer.getService().getCapabilities().getGetMapURL(),
                           wmsLayer.getService().getCapabilities().getGetFeatureInfoURL() );
        url = url.replace( "REQUEST=GetMap", "REQUEST=GetFeatureInfo" );
        url += "&QUERY_LAYERS=" + StringTools.listToString( wmsLayer.getLayerNames(), ',' );
        url += "&X=" + (int) clickXY.getX();
        url += "&Y=" + (int) clickXY.getY();
        url += "&feature_count=1000";
        return url;
    }

    // [UT] 02.05.2005
    /**
     * @param iter
     * @return unknown map
     */
    public Map<WMSLayer, FeatureCollection> wmsLayerToSpecifiedFeaturesMap( Iterator<?> iter ) {
        Map<WMSLayer, FeatureCollection> map = new HashMap<WMSLayer, FeatureCollection>();

        Point2D sourcePoint = null;

        try {
            Point2D destPoint = getViewDestination();
            sourcePoint = getViewSource();

            // assuming source == dest -> click TODO another way to check this?
            if ( destPoint.getX() != sourcePoint.getX() && destPoint.getY() != sourcePoint.getY() ) {
                // no click, but drag -> cannot handle handle WMS GetInfo
                return null;
            }

        } catch ( NoninvertibleTransformException e ) {
            e.printStackTrace();
            return null;
        }

        for ( Iterator<?> i = iter; i.hasNext(); ) {

            WMSLayer wmsLayer = (WMSLayer) i.next();

            if ( wmsLayer.isVisible() ) {

                try {
                    String ir = createFeatureInfoRequest( wmsLayer, getPanel(), sourcePoint );
                    URL u = new URL( ir );

                    LOG.debug( "GetFeatureInfo request: " + ir );

                    InputStreamReader is = new InputStreamReader( u.openStream() );

                    // create deegree FeatureCollection
                    // FIXME this is throwing NullPointer is layer is not queryable
                    // TODO see if except is thrown, rather than catch and run
                    // or check if layer is queryable
                    try {
                        GMLFeatureCollectionDocument gmlDoc = new GMLFeatureCollectionDocument();
                        gmlDoc.load( is, "http://dummy" );

                        org.deegree.model.feature.FeatureCollection deegFc = gmlDoc.parse();

                        // transform deegeree FC into JUMP FC
                        Point pt = new GeometryFactory().createPoint( getModelSource() );
                        FeatureCollection fc = JUMPFeatureFactory.createFromDeegreeFC( deegFc, pt );

                        map.put( wmsLayer, fc );

                    } catch ( NullPointerException e3 ) {
                        e3.printStackTrace();
                        System.out.println( "NullPointerException when clicking unqueryable layer." );
                    } finally {
                        is.close();
                    }

                } catch ( MalformedURLException e2 ) {
                    e2.printStackTrace();
                    return null;
                } catch ( Exception e ) {
                    e.printStackTrace();
                    return null;
                }
            }
            // put a Feat.Collec because we need to reuse it in Fet.Info Tool

        }
        return map;
    }

    @Override
    public String getName() {
        return "LayerAndWMSFeatureInfoToolPlugIn";
    }

    class ColorHelper {

        /**
         * 
         */
        public final Color latLonOrange = Color.red;// new Color(231,99,40);

        /**
         * 
         */
        public final Color otherColor = Color.GREEN;

        private Color currentColor = otherColor;

        /**
         * @return the next color
         */
        public Color getNextColor() {
            return currentColor = currentColor == latLonOrange ? otherColor : latLonOrange;
        }

        /**
         * @return the current color
         */
        public Color getCurrentColor() {
            return currentColor;
        }

    }
}
