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

package de.latlon.deejump.util.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.deegree.gml.GMLDocument;
import org.deegree.gml.GMLGeometry;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.FeatureType;
import org.deegree.model.feature.FeatureTypeProperty;
import org.deegree.model.geometry.GM_Envelope;
import org.deegree.model.geometry.GM_Exception;
import org.deegree.model.geometry.GM_Object;
import org.deegree.services.wfs.filterencoding.Filter;
import org.deegree.services.wfs.filterencoding.Operation;
import org.deegree.services.wfs.protocol.WFSGetFeatureRequest;
import org.deegree.services.wfs.protocol.WFSQuery;
import org.deegree.xml.Marshallable;
import org.deegree.xml.XMLTools;
import org.deegree_impl.gml.GMLBox_Impl;
import org.deegree_impl.gml.GMLDocument_Impl;
import org.deegree_impl.model.feature.FeatureFactory;
import org.deegree_impl.model.geometry.GMLAdapter;
import org.deegree_impl.model.geometry.JTSAdapter;
import org.deegree_impl.services.wfs.filterencoding.ComplexFilter;
import org.deegree_impl.services.wfs.filterencoding.OperationDefines;
import org.deegree_impl.services.wfs.filterencoding.PropertyName;
import org.deegree_impl.services.wfs.filterencoding.SpatialOperation;
import org.deegree_impl.services.wfs.protocol.WFSProtocolFactory;
import org.deegree_impl.tools.IDGenerator;
import org.w3c.dom.Document;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.LayerManager;

import de.latlon.deejump.DeeJUMPProperties;
import de.latlon.deejump.plugin.wfs.WFSLayer;

/**
 * Utility functions to create different kinds of FeatureDatasets. <br/>Further methods provided for
 * JUMPlon implemented by UT
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
 */
public class JUMPFeatureFactory {
    
    private static Logger LOG = Logger.getLogger( JUMPFeatureFactory.class );    
    
    private static int maxFeatures = 
        Integer.parseInt( DeeJUMPProperties.getString("maxFeatures") );
    
    /**
     * Creates a JUMP FeatureCollection from a deegree FeatureCollection [UT]
     * 
     * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
     * @param deegreeFeatCollec
     *            the deegree FeatureCollection
     * @return the new JUMP FeatureCollection
     */
    public static FeatureCollection createFromDeegreeFC( org.deegree.model.feature.FeatureCollection deegreeFeatCollec )
        throws Exception {

        return createFromDeegreeFC( deegreeFeatCollec, null );
    }


    /**
     * Creates a deegree <code>WFSGetFeatureRequest</code> based on the WFS version, the feature
     * name (<code>typeName</code>) and the <code>envelope</code>. <br/>This method was
     * adapted from <code>DownloadListener</code>.
     * 
     * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
     * @param version
     *            the WFS version
     * @param typeName
     *            the feature (type) name
     * @param envelope
     *            the box inside of which data has been requested
     * @return a wfs GetFeature request
     */
    public static WFSGetFeatureRequest createFeatureRequest( String version, String typeName,
                                                            GM_Envelope envelope ) throws Exception {

        StringReader reader = new StringReader( GMLAdapter.export( envelope ) );
        GMLGeometry gml = new GMLBox_Impl( XMLTools.parse( reader ).getDocumentElement() );
        Operation op = new SpatialOperation( OperationDefines.BBOX, new PropertyName( "GEOM" ), gml ); //$NON-NLS-1$

        Filter filter = new ComplexFilter( op );
        // create query using the ID of the FeatureTemplate as typename
        WFSQuery query = WFSProtocolFactory.createQuery( null, null, version, typeName, null );
        IDGenerator idg = IDGenerator.getInstance();
        // create WFS GetFeature request
        WFSGetFeatureRequest gfr = WFSProtocolFactory.createWFSGetFeatureRequest( version, "" //$NON-NLS-1$
            + idg.generateUniqueID(), null, null, "GML2", null, null, maxFeatures, 0, //$NON-NLS-1$
            new WFSQuery[] { query } );

        return gfr;
    }

    /**
     * Creates a deegree <code>FeatureCollection</code> from a given GetFeature request to a
     * server.
     * 
     * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
     * @param serverUrl
     *            the URL of the WFS server
     * @param request
     *            the GetFeature request
     * @return a deegree FeatureCollection
     */
    public static org.deegree.model.feature.FeatureCollection createDeegreeFCfromWFS( String serverUrl,
                                                           WFSGetFeatureRequest request )
        throws Exception {

        return createDeegreeFCfromWFS( serverUrl, ( (Marshallable) request ).exportAsXML() );
    }

    /**
     * Creates a deegree <code>FeatureCollection</code> from a given GetFeature request to a
     * server.
     * 
     * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
     * @param serverUrl
     *            the URL of the WFS server
     * @param request
     *            the GetFeature request
     * @return a deegree FeatureCollection
     */
    public static org.deegree.model.feature.FeatureCollection createDeegreeFCfromWFS( String serverUrl, String request )
        throws Exception {

        LOG.info( "WFS GetFeature: " + serverUrl +  " -> " + request ); //$NON-NLS-1$ //$NON-NLS-2$
        
        HttpClient httpclient = new HttpClient();
        PostMethod httppost = new PostMethod( serverUrl );
        httppost.setRequestBody( request );
        httpclient.executeMethod( httppost );

        InputStream is = httppost.getResponseBodyAsStream();
        Document doc = null;

        InputStreamReader ireader = new InputStreamReader( is );
        BufferedReader br = new BufferedReader( ireader );
        StringBuffer sb = new StringBuffer( 50000 );
        String s = null;
        while (( s = br.readLine() ) != null) {
            sb.append( s );
        }
        s = sb.toString();
        br.close();

        if ( s.indexOf( "<Exception>" ) >= 0 || s.indexOf( "<ExceptionReport" ) >= 0 ) { //$NON-NLS-1$ //$NON-NLS-2$
            throw new Exception( "Couldn't get data from WFS:\n" //$NON-NLS-1$
                + s );
        }
        StringReader sr = new StringReader( s );
        
//System.out.println( s );
        doc = XMLTools.parse( sr );

        //		DOMPrinter.printNode( System.out, doc);


        // transform data and store as shape file
        GMLDocument gmlDoc = new GMLDocument_Impl( doc );
        org.deegree.model.feature.FeatureCollection newFeatCollec = FeatureFactory.createFeatureCollection( gmlDoc.getRoot() );

        return newFeatCollec;
    }

    
    /**
     * Creates a JUMP FeatureCollection from a deegree FeatureCollection [UT] and a specified
     * JUMP/JTS Geometry object. The new JUMP FeatureCollection returned will have the
     * <code>defaultGeometry</code> as its <code>GEOM</code> attribute
     * 
     * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
     * @param deegreeFeatCollec
     *            the deegree FeatureCollection
     * @param defaultGeometry
     *            the geometry of the returned FeatureCollection
     * @return the new JUMP FeatureCollection
     */
    public static FeatureCollection createFromDeegreeFC(
                                                     org.deegree.model.feature.FeatureCollection deegreeFeatCollec,
                                                     Geometry defaultGeometry)

        throws Exception {

        FeatureSchema fs = new FeatureSchema();

        com.vividsolutions.jump.feature.FeatureCollection jumpFC = new FeatureDataset( fs );

        org.deegree.model.feature.Feature[] feats = deegreeFeatCollec.getAllFeatures();

        if ( feats == null || feats.length < 1 ) {
            throw new Exception( "No data found" ); //$NON-NLS-1$
        } /*else if ( feats.length < 1 ) {
            return null;
        }*/

        // deegree ftps[j].getType() - > JUMP ttributeType.
        FeatureType ft = feats[0].getFeatureType();

        FeatureTypeProperty[] geoTypeProps = ft.getGeometryProperties();

        String geoProName = null;
        
        if ( geoTypeProps.length > 1 ) {
            throw new Exception( "FeatureType has more than one GeometryProperty.\n" //$NON-NLS-1$
                + "This is not supported." ); //$NON-NLS-1$
        } else if ( geoTypeProps == null || geoTypeProps.length == 0 ) {
            geoProName = "GEOMETRY"; //$NON-NLS-1$
        } else {
            geoProName = geoTypeProps[0].getName();
        }


        FeatureTypeProperty[] featTypeProps = ft.getProperties();
        Object[] properties = feats[0].getProperties();
        fs.addAttribute( "GEOMETRY", AttributeType.GEOMETRY ); //$NON-NLS-1$

        for (int j = 0; j < properties.length; j++) {
//            System.out.println( ftps[j].getName() );
            if ( !geoProName.equalsIgnoreCase( featTypeProps[j].getName() ) ) //&&
            // !ftps[j].getName().toUpperCase().endsWith(
            // geoProName.toUpperCase() )
            {
                fs.addAttribute( featTypeProps[j].getName(), AttributeType.STRING );

            }
        }
        for (int i = 0; i < feats.length; i++) {

            com.vividsolutions.jump.feature.Feature jf = new BasicFeature( fs );
            GM_Object geoObject = feats[i].getDefaultGeometryProperty();
            if ( defaultGeometry == null && geoObject != null ){
                try {
                    jf.setGeometry( JTSAdapter.export( geoObject ) );

                } catch (GM_Exception e) {
                    e.printStackTrace();
                }
            } else {
                jf.setGeometry( defaultGeometry );

            }
            
            for (int j = 0; j < jf.getSchema().getAttributeCount(); j++) {
                if ( j != jf.getSchema().getGeometryIndex() ) {
                    jf.setAttribute( j, feats[i].getProperty( fs.getAttributeName( j ) ) );
                }
            }
            jumpFC.add( jf );
        }
        
        return jumpFC;//createWFSLayer( jfc, displayName, origName, geoProName, layerManager );

    }

    
    
    public static WFSLayer createWFSLayer(
                                          com.vividsolutions.jump.feature.FeatureCollection featCollec,
                                          String displayName, String origName, String geoProName,
                                          LayerManager layerManager ) throws Exception {

        return new WFSLayer( displayName, layerManager.generateLayerFillColor(), featCollec,
            layerManager, origName, geoProName );
    }

    public static org.deegree.model.feature.FeatureCollection createFromJUMPFeatureCollection(
            com.vividsolutions.jump.feature.FeatureCollection jumpFeatureCollection)
            throws GM_Exception {

        if (jumpFeatureCollection.size() == 0 || jumpFeatureCollection == null) {
            throw new IllegalArgumentException(
                    "FeatureCollection cannot be null and must have at least one feature");
        }
        org.deegree.model.feature.FeatureCollection fc = FeatureFactory
                .createFeatureCollection("id", jumpFeatureCollection.size());

        FeatureSchema schema = jumpFeatureCollection.getFeatureSchema();
        //      for (int i = 0; i < schema.getAttributeCount(); i++) {
        //          System.out.println(schema.getAttributeName(i) + " "
        //                  + schema.getAttributeType(i));
        //      }
        int count = 0;

        for (Iterator iter = jumpFeatureCollection.iterator(); iter.hasNext();) {
            com.vividsolutions.jump.feature.Feature feature = (com.vividsolutions.jump.feature.Feature) iter
                    .next();
            //1.
            FeatureTypeProperty[] ftp = new FeatureTypeProperty[schema
                    .getAttributeCount()];
            ftp[0] = FeatureFactory.createFeatureTypeProperty(schema
                    .getAttributeName(0),
                    "org.deegree.model.geometry.GM_Object", true);
            for (int i = 1; i < schema.getAttributeCount(); i++) {
            	            	
                ftp[i] = FeatureFactory.createFeatureTypeProperty(schema
                        .getAttributeName(i), feature.getAttribute(
                        schema.getAttributeName(i)).getClass().getName(), true);
            }

            //2.
            FeatureType ft = FeatureFactory.createFeatureType(null, null,
                    "featuretypename", ftp);
            //3.
            FeatureProperty[] fp = new FeatureProperty[schema
                    .getAttributeCount()];
            fp[0] = FeatureFactory.createFeatureProperty(schema
                    .getAttributeName(0), JTSAdapter
                    .wrap(feature.getGeometry()));

            for (int i = 1; i < schema.getAttributeCount(); i++) {
                fp[i] = FeatureFactory.createFeatureProperty(schema
                        .getAttributeName(i), feature.getAttribute(schema
                        .getAttributeName(i)));
            }
            //4.
            org.deegree.model.feature.Feature fe = FeatureFactory
                    .createFeature("-" + count++, ft, fp);
            fc.appendFeature(fe);
        }
        return fc;
    }    
    
    /**
     * @param i max number of features. Values below 1 are ignored
     */
    public static void setMaxFeatures( int i ) {
        if( i > 0 ){
            maxFeatures = i;
        }
    }
         
    public static int getMaxFeatures() {
        return maxFeatures;
    }
    
}