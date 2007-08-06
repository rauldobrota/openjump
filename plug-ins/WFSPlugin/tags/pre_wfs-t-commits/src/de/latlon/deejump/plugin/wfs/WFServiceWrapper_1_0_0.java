/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */

package de.latlon.deejump.plugin.wfs;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFServiceWrapper_1_0_0 extends AbstractWFSWrapper {

    
    private XMLFragment capsDoc;
    
    private String[] featureTypes;
    
    public WFServiceWrapper_1_0_0( String baseUrl ){
        super( baseUrl );
        init();
    }
    
    private void init() {
        
        capsDoc = new XMLFragment();
        
        try {
            capsDoc.load( new URL( getCapabilitiesURL() ) );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        
    }

    @Override
    public String getCapabilitesAsString() {
        return DOMPrinter.nodeToString( capsDoc.getRootElement(), " " );
    }

    public String[] getFeatureTypes() {
        if ( featureTypes == null ) {
            featureTypes = extractFeatureTypes();
        }
        return featureTypes;
    }

    private String[] extractFeatureTypes() {
        
        String[] fts = null;
        
        ftNameToWfsFT = new HashMap<String, WFSFeatureType>();
        
        Element root = this.capsDoc.getRootElement();
        
        try {
            List nodes = XMLTools.getNodes( root, 
                                           "wfs:FeatureTypeList/wfs:FeatureType", 
                                           CommonNamespaces.getNamespaceContext() );
            
            List<String> ftList = new ArrayList<String>( nodes.size() );
            for ( Object n : nodes ) {
               String name = 
                   XMLTools.getRequiredNodeAsString( (Node) n,
                                                     "wfs:Name",
                                                     CommonNamespaces.getNamespaceContext() );

               Node node = XMLTools.getNode( (Node) n,
                                                     "wfs:Name/text()",
                                                     CommonNamespaces.getNamespaceContext() );

               QualifiedName qualiName = XMLFragment.parseQualifiedName( node );
               
               ftList.add( qualiName.getLocalName() );
               
               URI uri = XMLTools.getNodeAsURI( (Node) n, 
                                                "wfs:SRS/text()", 
                                                CommonNamespaces.getNamespaceContext(), null );
               WFSFeatureType wfsFt = 
                   new WFSFeatureType(qualiName,null, null, null, uri,null,null,null,null,null);
               
               //putting the name as key, only
               //ftNameToWfsFT.put( qualiName.getPrefix() + ":" + qualiName.getLocalName(), wfsFt );
               ftNameToWfsFT.put( qualiName.getLocalName(), wfsFt );
               
            }
            fts = ftList.toArray( new String[ ftList.size() ] );
        } catch ( XMLParsingException e ) {
            e.printStackTrace();
        }
        return fts;
        
    }

   
    private String createOnlineResourceForOperation( String operationName, String httpMeth ) {
        String value = null;
        Element root = this.capsDoc.getRootElement();
        
        try {
            value = XMLTools.getRequiredNodeAsString( root, "wfs:Capability/wfs:Request/wfs:"+ operationName +"/wfs:DCPType/wfs:HTTP/wfs:"+ httpMeth +"/@onlineResource", CommonNamespaces.getNamespaceContext() );
            
        } catch ( XMLParsingException e ) {
            e.printStackTrace();
        }
        return value;
        
    }
    
    public String createDescribeFTOnlineResource() {
        return createOnlineResourceForOperation( "DescribeFeatureType", "Get" );
    }

    public String[] _getProperties( String featureType ) {
        // TODO Auto-generated method stub
        return null;
    }

    public QualifiedName _getQualiNameByFeatureTypeName( String ftName ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServiceVersion() {
        return "1.0.0";
    }

    protected String _createCapabilitiesOnlineResource() {
        throw new UnsupportedOperationException("TODO");
//return null;
    }

    public String getGetFeatureURL() {
        return createOnlineResourceForOperation( "GetFeature", "Post" );
    }
    
    public static void main( String[] args ){
        
        if( args.length == 21 ){
            System.out.println("Usage: todo...");
        } else {
            String url = "http://www.refractions.net:8080/geoserver/wfs/GetCapabilities";
            //url = "file:///home/taddei/Desktop/test_caps.xml";
//            url = "http://demo.intevation.de/geoserver/wfs";
            
            AbstractWFSWrapper wfs = new WFServiceWrapper_1_0_0( url );
            
//            System.out.println( wfs.createCapabilitiesOnlineResource() );
//            System.out.println( wfs.getCapabilitiesURL() );
            
            System.out.println( wfs.createDescribeFTOnlineResource() );
            
            QualifiedName qn = null;
            try {
                qn = new QualifiedName("blah", "Blih", new URI("http://host") );    
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            
            System.out.println( wfs.getDescribeTypeURL( qn ) );
            System.out.println( wfs.getGetFeatureURL() );
            
            String[] featTypes = wfs.getFeatureTypes();
            for ( int i = 0; i < featTypes.length; i++ ) {
                System.out.println( featTypes[i] );
            }            
            
        }
    }
    
}

/* ********************************************************************
Changes to this class. What the people have been up to:

$Log$
Revision 1.3  2007/05/14 08:50:53  taddei
Fix for the problems of null prefixes and false namespaces of misbehaving WFS

Revision 1.2  2007/05/02 13:27:11  taddei
Use now WFSFeatureType instead of QualifiedName.

Revision 1.1  2007/04/26 09:19:26  taddei
Added initial working version of classes and complementary files.

********************************************************************** */