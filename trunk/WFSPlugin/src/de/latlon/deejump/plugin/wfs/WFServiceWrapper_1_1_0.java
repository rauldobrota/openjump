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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.xml.sax.SAXException;

import de.latlon.deejump.ui.DeeJUMPException;


/**
 * This class represents a WFService. It handles connection with the server
 * behind the given URL. It also caches Feature Information such as which 
 * propertis belong to a FeatueType 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class WFServiceWrapper_1_1_0 extends AbstractWFSWrapper {
    
    private static Logger LOG = Logger.getLogger( WFServiceWrapper_1_1_0.class );    
    
    private WFSCapabilities wfsCapabilities;
    
    /**
     * Contains a list of feature types (as Strings) offered by the WFService
     */
    private String[] featureTypes;
    

    private String getFeatureUrl;

    private String descrFtUrl;
    
    private String capsString;

//    private WFSCapabilitiesDocument wfsCapsDoc;

    public WFServiceWrapper_1_1_0(String wfsURL) throws DeeJUMPException{ 
        super(wfsURL);
        init();
    }
    
    private void init() throws DeeJUMPException{     
//    throws MalformedURLException, IOException, SAXException, InvalidCapabilitiesException{
        
        createHttpClient();
        
        //TODO validate string
        //setWfsURL( wfsURL + "REQUEST=GetCapabilities&VERSION=1.1.0&SERVICE=WFS" );
        
        WFSCapabilitiesDocument wfsCapsDoc = new WFSCapabilitiesDocument();
        try {
            wfsCapsDoc.load( new URL( getCapabilitiesURL() ) );
        } catch ( MalformedURLException e ) {
            LOG.error( "Invalid URL", e );
            throw new DeeJUMPException( e );
        } catch ( IOException e ) {
            LOG.error( "IOException when opening: " + getCapabilitiesURL(), e );
            throw new DeeJUMPException( e );
        } catch ( SAXException e ) {
            throw new DeeJUMPException( e );
        } 
        
        capsString = DOMPrinter.nodeToString( wfsCapsDoc.getRootElement(), "" );

        try {

            wfsCapabilities = (WFSCapabilities) wfsCapsDoc.parseCapabilities();
        } catch ( InvalidCapabilitiesException e ) {
            LOG.error( "Could not initialize WFS capabilities", e );
            throw new DeeJUMPException( e );
        }
    }
    
    
    private String[] extractFeatureTypes() {
        
        String[] fts = null;
        
        WFSFeatureType[] featTypes = wfsCapabilities.getFeatureTypeList().getFeatureTypes();
        ftNameToWfsFT = new HashMap<String,WFSFeatureType>();
        fts = new String[ featTypes.length ];
        for ( int i = 0; i < fts.length; i++ ) {
            QualifiedName qn = featTypes[i].getName();
            fts[i] = qn.getLocalName();
            //well, putting prefix + : + simple name
            // should consider to put simple name only!
            /*ftNameToWfsFT.put( qn.getPrefix() + ":" + qn.getLocalName(), 
                                    featTypes[i] );*/
            ftNameToWfsFT.put( qn.getLocalName(), featTypes[i] );                      
        }
        
        return fts;
    }

    public String[] getFeatureTypes() {
        if ( featureTypes == null ){
//            Document doc = getWfsCapabilitiesDoc( wfsURL );
            featureTypes = extractFeatureTypes(); 
        }
        return featureTypes;
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
    
    public String getCapabilitesAsString(){
        return this.capsString;
    }
    
    String getCrsForFeatureType( QualifiedName featureTypeName ) {
        String crs = null;
        
        FeatureTypeList ftl = this.wfsCapabilities.getFeatureTypeList();
        
        QualifiedName qn = featureTypeName;//getQualiNameByFeatureTypeName( featureType );
        WFSFeatureType ft = ftl.getFeatureType( qn );
        crs = ft.getDefaultSRS().toASCIIString();
        
        return crs;
    }
    
    

    public String getServiceVersion() {
        return "1.1.0";
    }

    protected String createDescribeFTOnlineResource(){
        org.deegree.ogcwebservices.getcapabilities.Operation[] ops = this.wfsCapabilities.getOperationsMetadata().getOperations();
        String descrFtUrl = null;
        for ( int i = 0; i < ops.length && descrFtUrl == null; i++ ) {
            
            if( ops[i].getName().equals( "DescribeFeatureType" ) ){
                DCPType[] dcps = ops[i].getDCPs();
                if( dcps.length > 0 ){
                    descrFtUrl =  ( (HTTP) dcps[0].getProtocol() ).getGetOnlineResources()[0].toString();
                }

                if ( descrFtUrl == null ){
                    descrFtUrl =  ( (HTTP) dcps[0].getProtocol() ).getPostOnlineResources()[0].toString();
                    //isGet = false;
                }
            }
        }
        return descrFtUrl;
    }
    
    protected String createCapabilitiesOnlineResource() {
        throw new UnsupportedOperationException("TODO");
//return null;
    }

    
}
