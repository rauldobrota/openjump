
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

import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

import de.latlon.deejump.util.data.JUMPFeatureFactory;

/**
 * 
 * This class is basically copied from FeatureInfoTool to extend functionality
 * so as to provide FeatureInfo on WMSLayers. 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 *
 */

public class LayerAndWMSFeatureInfoTool extends SpecifyFeaturesTool {

    public static final ImageIcon ICON =  
    	
        new ImageIcon(LayerAndWMSFeatureInfoTool.class.getResource("inform.gif"));
    
    private ColorHelper colorHelper = new ColorHelper();
    
    public LayerAndWMSFeatureInfoTool() {
        setColor(Color.magenta);
    }

    public Icon getIcon() {
        return ICON;
    }

    public Cursor getCursor() {
        return createCursor(IconLoader.icon("InfoCursor.gif").getImage());
    }
    


    
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();

        InfoFrame infoFrame = getTaskFrame().getInfoFrame();
        if (!wasShiftPressed()) {
            infoFrame.getModel().clear();
        }
        Map map = layerToSpecifiedFeaturesMap();
        Iterator i = map.keySet().iterator();
        while(i.hasNext()){
            Layer layer = (Layer) i.next();

            if (layer.getName().equals(FenceLayerFinder.LAYER_NAME)) {
                continue;
            }
            Collection features = (Collection) map.get(layer);
            infoFrame.getModel().add(layer, features);
            
        }

        //do the same, but for wms layers
        map = wmsLayerToSpecifiedFeaturesMap( 
                getPanel().getLayerManager().getLayerables( WMSLayer.class ).iterator(),
                getBoxInModelCoordinates());

        if ( map != null){
            i = map.keySet().iterator();

            while(i.hasNext()){

                Layerable layer = (Layerable) i.next();
                
//                Collection features = (Collection) map.get(layer);
                FeatureCollection fc = (FeatureCollection)map.get(layer);
                if( fc != null ){
                    
                    Layer l = new Layer( "WMS:" + layer.getName(), 
                        colorHelper.getNextColor(), 
                        fc, getPanel().getLayerManager());
    	            
    	            // here get all features from the feture collec and...
    	            Set intersectingFeatures = new HashSet();				    
    			    for (Iterator iter = fc.iterator(); iter.hasNext();) {
    			        Object o = iter.next();
    				    intersectingFeatures.add( o );

                    }
    			    
    	            // ...pass it on
    	            infoFrame.getModel().add(l, intersectingFeatures);
                    
                }
                
            }
            
            infoFrame.surface();
        }
    }    
    
    //[UT] 02.05.2005 
    public Map wmsLayerToSpecifiedFeaturesMap(Iterator iter, Envelope envelope){
        Map map = new HashMap();

        Point2D sourcePoint = null;
        
        try {
            Point2D destPoint = getViewDestination();
            sourcePoint = getViewSource();
            
            // assuming source == dest -> click TODO another way to check this?
            if( destPoint.getX() != sourcePoint.getX() && 
                    destPoint.getY() != sourcePoint.getY()     ){
                // no click, but drag -> cannot handle handle WMS GetInfo
                return null;
            }
            
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
            return null;
        }
        
        for (Iterator i = iter; i.hasNext();) {

//            Layerable layer = (Layerable) i.next();
            WMSLayer wmsLayer = (WMSLayer)i.next();

            if ( wmsLayer.isVisible() ){
		        Envelope boxInModelCoordinates = null;
		        
		        try {
		            boxInModelCoordinates = getBoxInModelCoordinates();
		        } catch (NoninvertibleTransformException e1) {
		            e1.printStackTrace();
		            return null;
		        }
		        
		        InfoRequest ir = InfoRequest.createFeatureInfoRequest(wmsLayer, getPanel().getWidth(),getPanel().getHeight(),
				        sourcePoint,
				        new Point2D.Float ((float)boxInModelCoordinates.getMinX(), (float)boxInModelCoordinates.getMinY()),
				        new Point2D.Float((float)boxInModelCoordinates.getMaxX(), (float)boxInModelCoordinates.getMaxY()));
		        
		    	try {
		            URL u = ir.getURL();

					InputStreamReader is = new InputStreamReader(u.openStream());

					// create deegree FeatureCollection
					//FIXME this is throwing NullPointer is layer is not queryable
					//  TODO see if except is thrown, rather than catch and run
                    // or check if layer is queryable
					try {
                        GMLFeatureCollectionDocument gmlDoc = 
                            new GMLFeatureCollectionDocument();
                        gmlDoc.load( is, "http://dummy" );
                        
                        
                        org.deegree.model.feature.FeatureCollection deegFc = gmlDoc.parse();
                        
                        
                        // this is buggy, prevents other sys outs them from showing
//GMLFeatureAdapter.export( deegFc, System.out);
                        // transform deegeree FC into JUMP FC
                        FeatureCollection fc = JUMPFeatureFactory
                        	.createFromDeegreeFC( deegFc, 
                        	        new GeometryFactory().createPoint( getModelSource() ));

//                        Collection feats = createFeatures( u, boxInModelCoordinates );
                        map.put( wmsLayer, fc );

                    } catch (NullPointerException e3) {
                        e3.printStackTrace();
                        System.out.println("NullPointerException when clicking unqueryble layer.");
                    } finally {
                        is.close();
                    }
		            
		        } catch (MalformedURLException e2) {
		            e2.printStackTrace();
		            return null;
		        } catch (Exception e) {
		            e.printStackTrace();
		            return null;
		        }
            }
	//        	 put a Feat.Collec because we need to reuse it in Fet.Info Tool
	        
        }
        return map;
    }
        
        private Collection createFeatures(URL getInfoUrl, Envelope boxInModelCoordinates){
            
            Collection features = null;
            InputStreamReader is = null; 
		    // create deegree FeatureCollection
		    
		    try {
            is = new InputStreamReader( getInfoUrl.openStream() );
            GMLFeatureCollectionDocument gmlDoc = new GMLFeatureCollectionDocument();
            gmlDoc.load( is, null );
            org.deegree.model.feature.FeatureCollection deegFc = gmlDoc.parse();
            // System.out.println("deegree FC " + deegFc);
            } catch ( IOException e ) {
                e.printStackTrace();
                return null;
            }
            // transform deegeree FC nto JUMP FC
            /*
             * FeatureCollection fc = JUMPFeatureFactory .createFromDeegreeFC( deegFc,
             * EnvelopeUtil.toGeometry(boxInModelCoordinates));
             */catch ( XMLParsingException e ) {
                e.printStackTrace();
            } catch ( SAXException e ) {
                e.printStackTrace();
            }
    		    
// Set intersectingFeatures = new HashSet();
		    
		    
		    return features;
        }
        
        public String getName(){
            return "LayerAndWMSFeatureInfoToolPlugIn";
        }
        
        class ColorHelper {

            public final Color latLonOrange = Color.red;//new Color(231,99,40);

            public final Color otherColor = Color.GREEN;
            
            private Color currentColor = otherColor; 
            /**
             * 
             */
            private ColorHelper() {
            }
            
            public Color getNextColor(){
                return currentColor = currentColor == latLonOrange ? otherColor : latLonOrange; 
            }

            public Color getCurrentColor(){
                return currentColor;
            }
            
        }
}
