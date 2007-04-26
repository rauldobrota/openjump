/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */

package de.latlon.deejump.plugin.wfs;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.GMLSchemaDocument;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcwebservices.OWSUtils;
import org.xml.sax.SAXException;

import de.latlon.deejump.ui.DeeJUMPException;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class AbstractWFSWrapper {
    
    private static Logger LOG = Logger.getLogger( AbstractWFSWrapper.class );    
    
    protected String baseURL;
    
    //should  use WFSFeatureType
    protected Map featureTypeToQName;
    
    /**
     * Maps a feature type to its schem. Geometry property is not held here!
     */
    private Map featureTypeToSchema;

    //hmmm, this is repating the above, really...
    private Map featureTypeToSchemaXML;

    /**
     * Maps a feature type to its geometry!
     */
    private Map geoProperties;

    private HttpClient httpClient;
    
    public abstract String getServiceVersion();
    
    public abstract String[] getFeatureTypes();
    
//    public abstract String[] getProperties(String featureType);
    
//    public abstract QualifiedName[] getGeometryProperties(String featureType);
    
    public abstract String getGetFeatureURL();
    
//    public abstract QualifiedName getQualiNameByFeatureTypeName( String ftName );

    //abstract protected String createCapabilitiesOnlineResource(); 

    //perhaps use a map for these???
    abstract protected String createDescribeFTOnlineResource(); 

    public String getBaseWfsURL(){
        return this.baseURL;
    }
    
    //not abs
    public abstract String getCapabilitesAsString();
    
    protected AbstractWFSWrapper( String baseUrl ){
        if( baseUrl == null || baseUrl.length() == 0 ){
            throw new IllegalArgumentException("The URL for the WFServer cannot be null or empty.");
        }
        this.baseURL = baseUrl;
        this.featureTypeToSchema = new HashMap( 10 );
        this.featureTypeToSchemaXML = new HashMap( 10 );
        this.geoProperties = new HashMap( 10 );
        createHttpClient();
        
    }
    
    public String getCapabilitiesURL() {
        
        StringBuffer sb = new StringBuffer(OWSUtils.validateHTTPGetBaseURL( this.baseURL ) );
        sb.append( "SERVICE=WFS&REQUEST=GetCapabilities&VERSION=" );
        sb.append( getServiceVersion() );
                
        return sb.toString();
    }
    
    
    public String getDescribeTypeURL( QualifiedName typename){
        
        String url = OWSUtils.validateHTTPGetBaseURL(createDescribeFTOnlineResource()) + "SERVICE=WFS&REQUEST=DescribeFeatureType&version="
            +  getServiceVersion() + "&TYPENAME=" 
            + typename.getPrefix() + ":" + typename.getLocalName()  
            + "&NAMESPACE=xmlns(" + typename.getPrefix()+"="+typename.getNamespace()+")";
        
        return url;
    }

    public GMLSchema getSchemaForFeatureType( String featureType ){
        return (GMLSchema)this.featureTypeToSchema.get( featureType );
    }

    public String getRawSchemaForFeatureType( String featureType ){
        return (String)this.featureTypeToSchemaXML.get( featureType );
    }
    
    /**Gets a document containing a valid description feature type (schema; XSD). Code adapted
     * from deegree viewer
     * @param featureType the feature type whose description should be returned
     * @return an XSD containing the type description 
     * @throws DeeJUMPException 
     * @throws IOException 
     * @throws HttpException 
     * @throws SAXException 
     * @throws XMLException 
     * @throws XMLParsingException 
     * @throws XMLSchemaException 
     * @throws Exception */
    protected GMLSchema loadSchemaForFeatureType(String featureType) throws DeeJUMPException {

        boolean isGet = true;
        
        String descrFtUrl = createDescribeFTOnlineResource();
        
        if( descrFtUrl == null ){
            throw new RuntimeException( "Service does not have a DescribeFeatureType operation accessible by HTTP GET or POST." );
        }
        QualifiedName ft = getQualiNameByFeatureTypeName( featureType );

        String serverReq = getDescribeTypeURL( ft );
        
        String httpProtocolMethod = isGet ? "HTTP_GET" : "HTTP_POST" ;
        
        LOG.debug( "Using " + httpProtocolMethod + " to get feature type description from " + descrFtUrl + serverReq);
        
        HttpMethod httpMethod = createHttpMethod( httpProtocolMethod );//new GetMethod( serverUrl );
        URI uri;
        try {
     
            uri = new URI( getBaseWfsURL(), true );
            httpMethod.setURI( uri );

        } catch ( URIException e ) {
            throw new DeeJUMPException(e);
        } 
        
        //only input here what's after the '?'
        httpMethod.setQueryString( serverReq.split( "\\?" )[1] );
        try {
            httpClient.executeMethod(httpMethod);
            
            GMLSchemaDocument xsdDoc = new GMLSchemaDocument();
            xsdDoc.load( new URL(serverReq ) );//httpMethod.getResponseBodyAsStream(), serverReq );
            
            return xsdDoc.parseGMLSchema();
        } catch ( Exception e ) {
e.printStackTrace();            
            String mesg = "Error fetching FeatureType description";
            LOG.error( mesg + " for " + featureType + " from " 
                       + uri + " using " + descrFtUrl + serverReq);
            throw new DeeJUMPException( mesg,e);
        } 
        
    }
    
    protected String loadSchemaForFeatureType2(String featureType) throws DeeJUMPException {

        boolean isGet = true;
        
        String descrFtUrl = createDescribeFTOnlineResource();
        
        if( descrFtUrl == null ){
            throw new RuntimeException( "Service does not have a DescribeFeatureType operation accessible by HTTP GET or POST." );
        }
        QualifiedName ft = getQualiNameByFeatureTypeName( featureType );

        String serverReq = getDescribeTypeURL( ft );
        
        String httpProtocolMethod = isGet ? "HTTP_GET" : "HTTP_POST" ;
        
        LOG.debug( "Using " + httpProtocolMethod + " to get feature type description from " + descrFtUrl + serverReq);
        
        HttpMethod httpMethod = createHttpMethod( httpProtocolMethod );//new GetMethod( serverUrl );
        URI uri;
        try {
     
            uri = new URI( getBaseWfsURL(), true );
            httpMethod.setURI( uri );

        } catch ( URIException e ) {
            throw new DeeJUMPException(e);
        } 
        
        //only input here what's after the '?'
        httpMethod.setQueryString( serverReq.split( "\\?" )[1] );
        try {
            httpClient.executeMethod(httpMethod);
            GMLSchemaDocument xsdDoc = new GMLSchemaDocument();
            xsdDoc.load( new URL(serverReq ) );//httpMethod.getResponseBodyAsStream(), serverReq );
            
            return DOMPrinter.nodeToString( xsdDoc.getRootElement(), null );
        } catch ( Exception e ) {
e.printStackTrace();            
            String mesg = "Error fetching FeatureType description";
            LOG.error( mesg + " for " + featureType + " from " 
                       + uri + " using " + descrFtUrl + serverReq);
            throw new DeeJUMPException( mesg,e);
        } 
        
    }
    
        
    
    /**Creates an String[] containing the attributes of a given feature type
     * @param featureTypeName the name of the feature type
     * @throws Exception 
     * */
    protected void createSchemaForFeatureType(String featureTypeName){
        
        try {
            //GMLSchema xsd = loadSchemaForFeatureType( featureTypeName );
            String rawXML = loadSchemaForFeatureType2( featureTypeName );
            
            GMLSchemaDocument xsdDoc = new GMLSchemaDocument();
            xsdDoc.load( new StringReader(rawXML), "dummy" );
            
            GMLSchema xsd = xsdDoc.parseGMLSchema(); 
            
            this.featureTypeToSchema.put( featureTypeName, xsd);
            this.featureTypeToSchemaXML.put( featureTypeName, rawXML);
            
            QualifiedName[] geoProp = guessGeomProperty( xsd );
            
            this.geoProperties.put( featureTypeName, geoProp );                 

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public String[] getProperties(String featureType) {
        
        createSchemaForFeatureType( featureType );
        
        List propsList = new ArrayList();
        
        GMLSchema schema = (GMLSchema)this.featureTypeToSchema.get( featureType );
        
        FeatureType[] fts = schema.getFeatureTypes();
        for ( int i = 0; i < fts.length; i++ ) {
            PropertyType[] props = fts[i].getProperties();
            for ( int j = 0; j < props.length; j++ ) {
                if( !(props[j].getType() == Types.GEOMETRY) ){
                    propsList.add(  props[j].getName().getAsString() );
                }
            }
        }
        
        return (String[])propsList.toArray( new String[ propsList.size() ] );
    }

    public QualifiedName getQualiNameByFeatureTypeName( String ftName ){
        return (QualifiedName)featureTypeToQName.get( ftName );
    }
    
    /**
     * guess which property might be "the" geometry property
     * @param propNames
     * @return
     */
    protected QualifiedName[] guessGeomProperty( GMLSchema schema ){

        QualifiedName[] geoPropNames = null;
        List tmpList = new ArrayList( 20 );
        
        
        FeatureType[] fts = schema.getFeatureTypes();
        for ( int i = 0; i < fts.length; i++ ) {
            PropertyType[] props = fts[i].getProperties();
            for ( int j = 0; j < props.length; j++ ) {
                
                if( props[j].getType() == Types.GEOMETRY ){
                    tmpList.add( props[j].getName() );
                    
                }
            }
        }

        geoPropNames = 
            (QualifiedName[])tmpList.toArray( new QualifiedName[ tmpList.size() ] );
        return geoPropNames;
    }
    
    public QualifiedName[] getGeometryProperties(String featureType) {
        return (QualifiedName[])this.geoProperties.get( featureType );
    }
    
    protected void createHttpClient(){
        httpClient = new HttpClient();
        
        HttpClientParams clientPars = new HttpClientParams();
        clientPars.setConnectionManagerTimeout( 60000 );
        
        httpClient.setParams( clientPars );
        
    }
    
    protected HttpMethod createHttpMethod( String methodName ){
        
        HttpMethod httpMethod = null;
        
        if( "HTTP_GET".equals( methodName ) ){
            httpMethod = new GetMethod();
        } else if( "HTTP_POST".equals( methodName ) ){
            httpMethod = new PostMethod();
        } else {
            throw new IllegalArgumentException( "method mame must be either 'HTTP_GET' or 'HTTP_POST'" );
        }
        
        return httpMethod;
    }    
}

/* ********************************************************************
Changes to this class. What the people have been up to:

$Log$
Revision 1.1  2007/04/26 09:19:26  taddei
Added initial working version of classes and complementary files.

********************************************************************** */