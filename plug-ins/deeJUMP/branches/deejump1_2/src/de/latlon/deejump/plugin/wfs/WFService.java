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
Aennchenstra�e 19
53177 Bonn
Germany


 ---------------------------------------------------------------------------*/

package de.latlon.deejump.plugin.wfs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.latlon.deejump.ui.DeeJUMPException;
import de.latlon.deejump.util.data.JUMPFeatureFactory;


/**
 * This class represents a WFService. It handles connection with the server
 * behind the given URL. It also caches Feature Information such as which 
 * propertis belong to a FeatueType 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class WFService {
    
    private static Logger LOG = Logger.getLogger( WFService.class );    
    
    public static final String WFS_PREFIX = "wfs";
    
    private String wfsURL;
    
    private WFSCapabilities wfsCapabilities;
    
    private Map featureTypeToQName;
    
    /**
     * Contains a list of feature types (as Strings) offered by the WFService
     */
    private String[] featureTypes;
    
    /**
     * Maps a feature type to its schem. Geometry property is not held here!
     */
    private Map ftToSchema;
    
    /**
     * Maps a feature type to its geometry!
     */
    private Map geoProperties;

    private HttpClient httpClient;

    private String getFeatureUrl;

    private String descrFtUrl;
    

//    private WFSCapabilitiesDocument wfsCapsDoc;

    public WFService(String wfsURL) throws DeeJUMPException{ 
//    throws MalformedURLException, IOException, SAXException, InvalidCapabilitiesException{
        
        ftToSchema = new HashMap( 10 );
        geoProperties = new HashMap( 10 );
        
        createHttpClient();
        
        //TODO validate string
        wfsURL = OWSUtils.validateHTTPGetBaseURL( wfsURL );
        setWfsURL( wfsURL + "REQUEST=GetCapabilities&VERSION=1.1.0&SERVICE=WFS" );
        WFSCapabilitiesDocument wfsCapsDoc = new WFSCapabilitiesDocument();
        try {
            wfsCapsDoc.load( new URL( this.wfsURL ) );
        } catch ( MalformedURLException e ) {
            LOG.error( "Invalid URL", e );
            throw new DeeJUMPException( e );
        } catch ( IOException e ) {
            LOG.error( "IOException when opening: " + this.wfsURL, e );
            throw new DeeJUMPException( e );
        } catch ( SAXException e ) {
            throw new DeeJUMPException( e );
        } 
        try {

            wfsCapabilities = (WFSCapabilities) wfsCapsDoc.parseCapabilities();
        } catch ( InvalidCapabilitiesException e ) {
            LOG.error( "Could not initalize WFS capabilities", e );
            throw new DeeJUMPException( e );
        }
    }
    
    
    private String[] extractFeatureTypes() throws Exception {
        
        WFSFeatureType[] featTypes = wfsCapabilities.getFeatureTypeList().getFeatureTypes();
        featureTypeToQName = new HashMap();
        String[] fts = new String[ featTypes.length ];
        for ( int i = 0; i < fts.length; i++ ) {
            
            QualifiedName qn = featTypes[i].getName();
            String prefixedName = qn.getAsString();
            fts[i] = prefixedName;
            
            featureTypeToQName.put( prefixedName, qn );
        }
        
        return fts;
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
    private GMLSchema loadForFeatureType(String featureType) throws DeeJUMPException {

        org.deegree.ogcwebservices.getcapabilities.Operation[] ops = this.wfsCapabilities.getOperationsMetadata().getOperations();
        
        boolean isGet = true;
        
        if ( descrFtUrl == null ){
            for ( int i = 0; i < ops.length && descrFtUrl == null; i++ ) {
    
                if( ops[i].getName().equals( "DescribeFeatureType" ) ){
                    DCPType[] dcps = ops[i].getDCPs();
                    if( dcps.length > 0 ){
                        descrFtUrl =  ( (HTTP) dcps[0].getProtocol() ).getGetOnlineResources()[0].toString();
                    }

                    if ( descrFtUrl == null ){
                        descrFtUrl =  ( (HTTP) dcps[0].getProtocol() ).getPostOnlineResources()[0].toString();
                        isGet = false;
                    }
                }
            }
       
        }
        if( descrFtUrl == null ){
            throw new RuntimeException( "Service does not have a DescribeFeatureType operation accessible by HTTP GET or POST." );
        }
        QualifiedName ft = getQualiNameByFeatureTypeName( featureType );

        
        
        String format = null;
        /*try {
            format = URLEncoder.encode( "text/xml; subtype=gml/3.1.1", "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
            throw new DeeJUMPException(e);
        } */
        format = "";//"text/xml; subtype=gml/3.1.1";
                                                                                        //OUTPUTFORMAT=" + format + "&
        String serverReq = "SERVICE=WFS&REQUEST=DescribeFeatureType&version=1.1.0&TYPENAME=" + featureType 
            + "&NAMESPACE=xmlns(" + ft.getPrefix()+"="+ft.getNamespace()+")";
        
        String httpProtocolMethod = isGet ? "HTTP_GET" : "HTTP_POST" ;
        
        LOG.debug( "Using " + httpProtocolMethod + " to get feature type description from " + descrFtUrl + serverReq);

        
        HttpMethod httpMethod = createHttpMethod( httpProtocolMethod );//new GetMethod( serverUrl );
        
        URI uri;
        try {
            descrFtUrl = OWSUtils.validateHTTPGetBaseURL( descrFtUrl );
     
            uri = new URI( descrFtUrl, true );
            httpMethod.setURI( uri );

        } catch ( URIException e ) {
            throw new DeeJUMPException(e);
        } 
        
        httpMethod.setQueryString( serverReq );
        
        try {
            httpClient.executeMethod(httpMethod);
            
            GMLSchemaDocument xsdDoc = new GMLSchemaDocument();
            this.wfsURL = OWSUtils.validateHTTPGetBaseURL( this.wfsURL );
            xsdDoc.load( httpMethod.getResponseBodyAsStream(), this.wfsURL );
//            LOG.debug( "Schema for " + featureType + ": \n" + DOMPrinter.nodeToString( xsdDoc.getRootElement(), "UTF-8" ) );
            

            return xsdDoc.parseGMLSchema();
        } catch ( Exception e ) {
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
    private void createSchemaForFeatureType(String featureTypeName) throws Exception {

        GMLSchema xsd = loadForFeatureType( featureTypeName );
        
        if ( xsd == null ){
            return;
        }

        this.ftToSchema.put( featureTypeName, xsd);
        
        QualifiedName[] geoProp = guessGeomProperty( xsd );
        
        this.geoProperties.put( featureTypeName, geoProp );                 
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
   

    public void setWfsURL(String wfsURL) {
        if( wfsURL == null || "".equals( wfsURL )){
            throw new IllegalArgumentException("The URL for the WFServer cannot be null or empty.");
        }
        this.wfsURL = wfsURL;
    }

    public String getWfsURL() {
        return this.wfsURL;
    }

    public String[] getFeatureTypes() throws Exception{
        if ( featureTypes == null ){
//            Document doc = getWfsCapabilitiesDoc( wfsURL );
            featureTypes = extractFeatureTypes(); 
        }
        return featureTypes;
    }

    public String[] getFeatureProperties(String featureType) throws Exception {
        
        createSchemaForFeatureType( featureType );
        
        List propsList = new ArrayList();
        
        GMLSchema schema = (GMLSchema)this.ftToSchema.get( featureType );
        
        
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

    public GMLSchema getSchemaForFeatureType( String featureType ){
        
        return (GMLSchema)this.ftToSchema.get( featureType );
    }
    
    public QualifiedName[] getGeometryProperties(String featureType) {
        return (QualifiedName[])this.geoProperties.get( featureType );
    }
    
    public QualifiedName getQualiNameByFeatureTypeName( String ftName ){
        return (QualifiedName)featureTypeToQName.get( ftName );
    }


    public String getGetFeatureURL() {
        
        org.deegree.ogcwebservices.getcapabilities.Operation[] ops = this.wfsCapabilities.getOperationsMetadata().getOperations();
        getFeatureUrl = null;

        for ( int i = 0; i < ops.length && getFeatureUrl == null; i++ ) {

            if( ops[i].getName().equals( "GetFeature" ) ){
                DCPType[] dcps = ops[i].getDCPs();
                if( dcps.length > 0 ){
                    getFeatureUrl =  ( (HTTP) dcps[0].getProtocol() ).getPostOnlineResources()[0].toString();
                }
                
            }
        }
        
        if( getFeatureUrl == null ){
            throw new RuntimeException( "Service does not have a GetFeature operation accessible by HTTP POST." );
        }
       
        return getFeatureUrl;
    }
    
    String getCrsForFeatureType( QualifiedName featureTypeName ) {
        String crs = null;
        
        FeatureTypeList ftl = this.wfsCapabilities.getFeatureTypeList();
        
        QualifiedName qn = featureTypeName;//getQualiNameByFeatureTypeName( featureType );
        WFSFeatureType ft = ftl.getFeatureType( qn );
        crs = ft.getDefaultSRS().toASCIIString();
        
        return crs;
    }
    
    private void createHttpClient(){
        httpClient = new HttpClient();
        
        HttpClientParams clientPars = new HttpClientParams();
        clientPars.setConnectionManagerTimeout( 60000 );
        
        httpClient.setParams( clientPars );
        
    }
    
    private HttpMethod createHttpMethod( String methodName ){
        
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
