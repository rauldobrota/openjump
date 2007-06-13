/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.latlon.deejump.util.data;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.xml.Marshallable;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.feature.schema.AbstractPropertyType;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.GMLSchemaDocument;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

import de.latlon.deejump.plugin.wfs.AbstractWFSWrapper;
import de.latlon.deejump.ui.DeeJUMPException;

/**
 * Utility functions to create different kinds of FeatureDatasets. <br/>Further methods provided for
 * JUMPlon implemented by UT
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
 */
public class JUMPFeatureFactory {
    
    private static Logger LOG = Logger.getLogger( JUMPFeatureFactory.class );    
    
    private static int maxFeatures = 100;
        //Integer.parseInt( DeeJUMPProperties.getString("maxFeatures") );
    
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
     * @param localName
     *            the feature (type) name
     * @param envelope
     *            the box inside of which data has been requested
     * @param prefix 
     * @return a wfs GetFeature request
     */
    public static GetFeature createFeatureRequest( String version, QualifiedName qualName,
                                                            org.deegree.model.spatialschema.Envelope envelope ) throws Exception {

        // FIXME srs is null!!!!
        String srs = null;
        //TODO
        CoordinateSystem cs = null;
        
        org.deegree.model.spatialschema.Geometry boxGeom = null;
        try {
            boxGeom = GeometryFactory.createSurface( envelope, cs );
        } catch ( GeometryException e ) {
            e.printStackTrace();
            throw new RuntimeException( "Cannot create surface from bbox." + e.getMessage() );
        } 
        
        
        Operation op = new SpatialOperation( OperationDefines.BBOX, new PropertyName( new QualifiedName( "GEOM" ) ), boxGeom ); //$NON-NLS-1$

        Filter filter = new ComplexFilter( op );
        
        //TODO get maxFeatures...
        Query query = Query.create( null, null, null, null, version, new QualifiedName[]{qualName}, srs, filter, maxFeatures, 0, RESULT_TYPE.RESULTS );
        IDGenerator idg = IDGenerator.getInstance();
        // create WFS GetFeature request
        
        //FIXME TODO what the heck???
        int maxDepth = 100;
        int traverseExpiry = -999;
        GetFeature gfr = GetFeature.create( version, 
                                            "" + idg.generateUniqueID(), 
                                            RESULT_TYPE.RESULTS, 
                                            "GML3",
                                            null,
                                            maxFeatures, 
                                            0,
                                            maxDepth,
                                            traverseExpiry, 
                                            new Query[] { query });

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
     * @throws Exception throwing ALL exceptions
     */
    public static org.deegree.model.feature.FeatureCollection createDeegreeFCfromWFS(
                                                                                     AbstractWFSWrapper serverUrl,
                                                                                     GetFeature request )
        throws Exception {

        return createDeegreeFCfromWFS( serverUrl, ( (Marshallable) request ).exportAsXML(), null );
    }


    
    /**
     * Creates a deegree <code>FeatureCollection</code> from a given GetFeature request to a
     * server.
     * 
     * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
     * @param server
     *            the URL of the WFS server
     * @param request
     *            the GetFeature request
     * @param featureType if non null, a DescribeFeatureType will be performed to get the correct schema
     * @return a deegree FeatureCollection
     * @throws DeeJUMPException 
     */
    public static org.deegree.model.feature.FeatureCollection createDeegreeFCfromWFS( AbstractWFSWrapper server, String request, QualifiedName featureType ) throws DeeJUMPException {
        
        String s = WFSClientHelper.createResponsefromWFS( server.getGetFeatureURL(), request );
        
        if ( s.indexOf( "<Exception>" ) >= 0 || s.indexOf( "<ServiceExceptionReport" ) >= 0 ) { //$NON-NLS-1$ //$NON-NLS-2$
            RuntimeException re = new RuntimeException( "Couldn't get data from WFS:\n" //$NON-NLS-1$
                                                        + s ); 
            LOG.debug( "Couldn't get data from WFS.", re ); 
            throw re;
        }
      
        LOG.debug( "WFS FC: " + s ); //$NON-NLS-1$ //$NON-NLS-2$

        StringReader sr = new StringReader( s );
        
        GMLFeatureCollectionDocument gfDoc = new GMLFeatureCollectionDocument();

        // get schema from server
        if(featureType != null){
            Map<URI, GMLSchema> schemaMap = new HashMap<URI, GMLSchema>();
            String dft = server.getDescribeTypeURL(featureType);
            GMLSchemaDocument doc = new GMLSchemaDocument();
            try {
                doc.load(new URL(dft));
                GMLSchema schema = doc.parseGMLSchema();
                schemaMap.put(featureType.getNamespace(), schema);
                gfDoc.setSchemas(schemaMap);
            } catch ( XMLSchemaException e ) {
                LOG.debug("DescribeFeatureType did not work.");
            } catch ( UnknownCRSException e ) {
                LOG.debug("DescribeFeatureType did not work.");
            } catch ( XMLParsingException e ) {
                LOG.debug("DescribeFeatureType did not work.");
            } catch ( MalformedURLException e ) {
                LOG.debug("DescribeFeatureType did not work.");
            } catch ( IOException e ) {
                LOG.debug("DescribeFeatureType did not work.");
            } catch ( SAXException e ) {
                LOG.debug("DescribeFeatureType did not work.");
            }
        }
        
        org.deegree.model.feature.FeatureCollection newFeatCollec = null;
        try {
            gfDoc.load( sr, "http://dummySysId" );
            
            newFeatCollec = gfDoc.parse();
        } catch ( SAXException e ) {
            String mesg = "Error parsing response."; 
            LOG.error( mesg, e );
            throw new DeeJUMPException( mesg, e  );
        } catch ( IOException e ) {
            String mesg = "Error parsing response."; 
            LOG.error( mesg, e );
            throw new DeeJUMPException( mesg, e  );
        } catch ( XMLParsingException e ) {
            String mesg = "Error parsing response."; 
            LOG.error( mesg, e );
            throw new DeeJUMPException( mesg, e  );
        }
        
       

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
    public static FeatureCollection createFromDeegreeFC(org.deegree.model.feature.FeatureCollection deegreeFeatCollec,
                                                        Geometry defaultGeometry )

        throws Exception {

        FeatureSchema fs = new FeatureSchema();

        com.vividsolutions.jump.feature.FeatureCollection jumpFC = new FeatureDataset( fs );

        org.deegree.model.feature.Feature[] feats = deegreeFeatCollec.toArray();

        if ( feats == null || feats.length < 1 ) {
            throw new Exception( "No data found" ); //$NON-NLS-1$
        } 
        
        //assuming one at least
        FeatureType ft = feats[0].getFeatureType();
        
        AbstractPropertyType[] geoTypeProps = ft.getGeometryProperties();

        String geoProName = null;

        if ( geoTypeProps.length > 1 ) {
            throw new RuntimeException( "FeatureType has more than one GeometryProperty.\n" //$NON-NLS-1$
                                 + "This is currently not supported." ); //$NON-NLS-1$
        } else if ( geoTypeProps == null || geoTypeProps.length == 0 ) {
            geoProName = "GEOMETRY"; //$NON-NLS-1$
        } else {
            geoProName = geoTypeProps[0].getName().getLocalName();
        }
        
        PropertyType[] featTypeProps = ft.getProperties();
        Object[] properties = feats[0].getProperties();

        // populate JUMP schema
        for ( int j = 0; j < featTypeProps.length; j++ ) {
            String name =  featTypeProps[j].getName().getLocalName();

            if ( !geoProName.equals( name ) ) {
                // TODO get schema to define correct type
                //fs.addAttribute( name, AttributeType.STRING );
                fs.addAttribute( name, findType( featTypeProps[j].getType() )  );
            } else {
                fs.addAttribute( "GEOMETRY", AttributeType.GEOMETRY ); //$NON-NLS-1$

            }
        }
        
        if( defaultGeometry == null && fs.getGeometryIndex() == -1 ){
            throw new RuntimeException( "No geometry property found!" );
        } else if ( defaultGeometry != null && fs.getGeometryIndex() == -1 ){
            fs.addAttribute( "GEOMETRY", AttributeType.GEOMETRY ); //$NON-NLS-1$
        }
        
        
        // populate FC with data
        for ( int i = 0; i < feats.length; i++ ) {

            com.vividsolutions.jump.feature.Feature jf = new BasicFeature( fs );
            org.deegree.model.spatialschema.Geometry geoObject = feats[i].getDefaultGeometryPropertyValue();

            if ( defaultGeometry == null && geoObject != null ) {
                try {
                    Geometry geom = JTSAdapter.export( geoObject );
                    jf.setGeometry( geom );

                } catch ( Exception e ) {
                    throw new RuntimeException( e );
                }
            } else {
                jf.setGeometry( defaultGeometry );
            }

            int geoIndex = jf.getSchema().getGeometryIndex();

            for ( int j = 0; j < jf.getSchema().getAttributeCount(); j++ ) {
                if ( j != geoIndex ) {
                    QualifiedName qn = new QualifiedName( fs.getAttributeName( j ),
                                                          featTypeProps[j].getName().getNamespace() );
                    
                    FeatureProperty fp = feats[i].getDefaultProperty( qn );  
                    Object value = null;
                    if ( fp != null ){
                        value = fp.getValue();
                    }
                    jf.setAttribute( j, value );
                }
            }
            jumpFC.add( jf );
        }

        return jumpFC;
    }

    /**
     * @param type an SQL type code as in deegree Types class
     * @return the JUMP type
     */
    private static AttributeType findType( int type ) {
        // assumes integer for SQL's NUMERIC
        String xsd = Types.getXSDTypeForSQLType(type, 0);

        if(xsd.equals("dateTime")){
            return AttributeType.DATE;
        }

        if(xsd.equals("gml:GeometryPropertyType")){
            return AttributeType.GEOMETRY;
        }

        if(xsd.equals("integer")){
            return AttributeType.INTEGER;
        }
        
        if(xsd.equals("double") || xsd.equals("decimal") || xsd.equals("float")){
            return AttributeType.DOUBLE;
        }

        if(xsd.equals("gml:FeaturePropertyType")){
            return AttributeType.OBJECT; // unknown what happens in this case
        }
        
        // default is string, should work for booleans as well
        return AttributeType.STRING;
    }


    /* TODO re-ignite!!!
    
    public static WFSLayer createWFSLayer(
                                          com.vividsolutions.jump.feature.FeatureCollection featCollec,
                                          String displayName, QualifiedName ftName, QualifiedName geoPropName, String crs,
                                          LayerManager layerManager ) throws Exception {

        return new WFSLayer( displayName, layerManager.generateLayerFillColor(), featCollec,
            layerManager, ftName, geoPropName, crs );
    }
*/
    public static org.deegree.model.feature.FeatureCollection createFromJUMPFeatureCollection(
            com.vividsolutions.jump.feature.FeatureCollection jumpFeatureCollection) throws UnknownTypeException, GeometryException
             {

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
        
        final URI GMLNS = CommonNamespaces.GMLNS;
        final URI XSNS = CommonNamespaces.XSNS;
        
        for (Iterator iter = jumpFeatureCollection.iterator(); iter.hasNext();) {
            com.vividsolutions.jump.feature.Feature feature = 
                (com.vividsolutions.jump.feature.Feature) iter.next();
            
//            FeatureProperty[] ftp = new FeatureProperty[ schema.getAttributeCount() ];
            PropertyType[] propType = new PropertyType[ schema.getAttributeCount() ];
            
            int geoIx = schema.getGeometryIndex();
            
            for (int i = 0; i < schema.getAttributeCount(); i++) {
            	          
                if( i != geoIx ){
                    String type = toXSDName( schema.getAttributeType(i) );

                    propType[i] = FeatureFactory
                        .createPropertyType( new QualifiedName( schema.getAttributeName(i) ),
                                         new QualifiedName( type, XSNS ), true );                        

                    /*ftp[i] = FeatureFactory
                        .createFeatureProperty( propType[i].getName(),
                                            feature.getAttribute( schema.getAttributeName(i) ) );
                    */

                } else {
                    
                    String type = "org.deegree.model.geometry.Geometry";
                    propType[i] = FeatureFactory
                    .createPropertyType( new QualifiedName( schema.getAttributeName(geoIx) ),
                                         new QualifiedName( type, GMLNS ), true );
                
                /*    ftp[i] = FeatureFactory
                        .createFeatureProperty( propType[0].getName() , feature.getAttribute( schema.getAttributeName(i) ));
                  */  
                }
            }

            //2.
            FeatureType ft = FeatureFactory
                .createFeatureType( new QualifiedName("featuretypename"), false, propType);
            //3.
            FeatureProperty[] fp = new FeatureProperty[schema
                    .getAttributeCount()];
            
            
            for (int i = 0; i < schema.getAttributeCount(); i++) {
                if( i != geoIx ){
                    fp[i] = FeatureFactory
                                .createFeatureProperty(schema.getAttributeName(i), 
                                                       feature.getAttribute(i));
                } else {
                    fp[i] = FeatureFactory
                    .createFeatureProperty( schema.getAttributeName( geoIx ), 
                                            JTSAdapter.wrap(feature.getGeometry()));

                }
            }
            //4.
            org.deegree.model.feature.Feature fe = 
                FeatureFactory.createFeature("fid_" + count++, ft, fp);
            
            fc.add(fe);
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
    
    public static String toXSDName( AttributeType type ){
        String t = null;
        if( type == AttributeType.DATE ){
            t = "date";
        } else if ( type == AttributeType.INTEGER ){
            t = "integer";
        } else if ( type == AttributeType.STRING ){
            t = "string";
        } else if ( type == AttributeType.DOUBLE ){
            t = "double";
        } else if ( type == AttributeType.OBJECT ){
            t = "object";
        } else {
            throw new RuntimeException( "no xsd type found for: " + type);
        }
        
        return t;
    }
    
}
