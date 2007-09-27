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
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.deegree.xml.ElementList;
import org.deegree.xml.XMLTools;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class represents a WFService. It handles connection with the server
 * behind the given URL. It also caches Feature Information such as which 
 * propertis belong to a FeatueType 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class WFService {
    
    public static final String WFS_PREFIX = "wfs";
    
    private String wfsURL;
    
    /**
     * Contains a list of feature types (as Strings) offered by the WFService
     */
    private String[] featureTypes;
    
    /**
     * Maps a feature type to a list of its properties. Geometry property is not held here!
     */
    private HashMap featureProperties = new HashMap( 10 );
    
    /**
     * Maps a feature type to a list of its properties. Geometry property is not held here!
     */
    private HashMap geoProperties = new HashMap( 10 );

    public WFService(String wfsURL){
        
        setWfsURL( wfsURL ); 
    }
    
    
    /**
     * Code adapted from deegree viewer */
    private String[] extractFeatureTypes(Document doc) 
    	throws Exception {

        String[] fts = new String[] {"Default Feature Type"};
        NodeList nl = doc.getElementsByTagName( "FeatureType" );

		if ( nl == null || nl.getLength() < 1 ){        
		    throw new Exception( "Error parsing the capabilities.\n" + 
		            "No feature types found.");
		}
		
		fts = new String[ nl.getLength() ];
            for (int i = 0; i < fts.length; i++) {
                Element elem = (Element)nl.item( i );
                Node node = elem.getElementsByTagName( "Name" ).item( 0 );
                fts[i] = node.getFirstChild().getNodeValue();
            }
        
        return fts;
    }

    /**
     * Code stolen from deegree viewer ;-)*/
    private Document getWfsCapabilitiesDoc( String serverUrl )
    	throws Exception{

        Document doc = null;

        HttpClient httpclient = new HttpClient();
		GetMethod httpMethod = new GetMethod( serverUrl );
		httpMethod.setQueryString( "request=GetCapabilities&version=1.0.0&SERVICE=WFS" );
		try {
		    httpclient.executeMethod(httpMethod);
		    doc = XMLTools.parse( httpMethod.getResponseBodyAsStream() );            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        httpMethod.releaseConnection();
        return doc;
    }
   
    /**Gets a document containing a valid description feature type (schema; XSD). Code adapted
     * from deegree viewer
     * @param featureType the feature type whose description should be returned
     * @return an XSD containing the type desciption */
    private Document getDescribeFeatureType(String featureType){
        Document doc = null;
            
	        try {
	            String serverUrl = this.wfsURL;
	            String serverReq = "SERVICE=WFS&REQUEST=DescribeFeatureType&version=1.0.0&OUTPUTFORMAT=XMLSCHEMA&TYPENAME=" + featureType;
	            
	            HttpClient httpclient = new HttpClient();
	            httpclient.setTimeout( 5000 );
	            GetMethod httpMethod = new GetMethod( serverUrl );
	    		httpMethod.setQueryString( serverReq );
	    		httpclient.executeMethod(httpMethod);
	
	    		doc = XMLTools.parse( httpMethod.getResponseBodyAsStream() );
	    		httpMethod.releaseConnection();
	            
	        } catch (org.apache.commons.httpclient.HttpRecoverableException ex) {
	            throw new RuntimeException( "Time out error.\n" + 
	                    "Is the server reachable?\n" +
                		"Are 'DescribeFeatureType' available and properly configured?");
	            
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            
	        }
		
		
        return doc;
    }

    /**Creates an String[] containing the attributes of a given feature type
     * @param featureTypeName the name of the feature type
     * */
    private void createAttributes(String featureTypeName) {

        Document doc = getDescribeFeatureType(featureTypeName);
        if ( doc == null ){
            return;
        }
        try {
            ElementList el = XMLTools.getChildElements(doc.getDocumentElement());
            Element ftElement = null;
            for (int i = 0; i < el.getLength(); i++) {
                Element e = el.item(i);
                Node a = XMLTools.getAttributeNode(e, "name");
                if ( a != null && featureTypeName.equals( ((Attr)a).getValue()) ){
                    ftElement = e;
                }                                        
            }
            
            if( ftElement != null ){
                
                ArrayList al = new ArrayList();
                ArrayList pl = new ArrayList();
                
                addElement2List(ftElement, al, pl);
                String[]  featProperties = (String[])al.toArray( new String[al.size()] );
                String[] propTypes = (String[])pl.toArray( new String[pl.size()] );
                String geoProp = guessGeomPropName(featProperties, propTypes );
//am i doing this there
//                researchDialog.setGeoPropName( geoProp );
//                researchDialog.addGeoPropName( geoProp );

                //remove geo property from list of scalar attributes
                al.remove( geoProp );
                this.featureProperties.put( featureTypeName, al.toArray( new String[al.size()] ));
                this.geoProperties.put( featureTypeName, geoProp );                 
                
                /*this.attributeNames = new String[0];
                 
                JOptionPane.showMessageDialog( 
                        (Component)this,
                        "Could not find element '" + featureTypeName +
                        "' in schema.\n Please reconfigure this schema.",
                        "Fehler",
                        JOptionPane.ERROR_MESSAGE
                      );
                      */
            } else{
                throw new Exception("Could not find element '" + featureTypeName +
                "' in schema.\n Please reconfigure this schema.");
                    
            }

        } catch(Exception e) {
            e.printStackTrace();
        }     
    }
        
    /**
     * guess which property might be "the" geometry property
     * @param propNames
     * @return
     */
    protected String guessGeomPropName(String[] propNames, String[] propTypes){
        String geoPropName = FeatureResearchDialog.GEOMETRY_PROPERTY_NAME; 
        for (int i = 0; i < propTypes.length; i++) {
            if( propTypes[i] != null && (
                    //"gml:GeometryPropertyType".equalsIgnoreCase(propTypes[i]) ||
                    propTypes[i].toUpperCase().matches( ".*GEOM.*" ) ||
                    propTypes[i].toUpperCase().matches( ".*POINT.*" ) ||
                    propTypes[i].toUpperCase().matches( ".*LINE.*" )  ||
                    propTypes[i].toUpperCase().matches( ".*POLY.*" ) 
                    )){
                
                geoPropName = propNames[i];
            }
        }
        
        // or better return null?
        return geoPropName;
    }
   
    /**
     * Looks for xsd:elements in element <code>e</code> and add them to array list
     * <code>al</code>
     * @param e
     * @param al
     */
    private void addElement2List(Element e, ArrayList al, ArrayList pl){
        ElementList list = XMLTools.getChildElements(e);
        for (int i = 0; i < list.getLength(); i++) {
            Element candEle = list.item(i);

            if( "xsd:element".equals(candEle.getNodeName()) ){
                Attr at = candEle.getAttributeNode("name");
                Attr type = candEle.getAttributeNode("type");
                // ignore geometry types
                if ( at != null ) {
                    al.add( at.getValue() );
                }
                if (	type == null || "".equals(type.getValue() ) ){  
                    pl.add( null );
                }else {                    
                    pl.add( type.getValue() );
                }
            }
            addElement2List(candEle, al, pl);            
        }
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

    public String[] getFeatureTypes() 
    	throws Exception{
        if ( featureTypes == null ){
            Document doc = getWfsCapabilitiesDoc( wfsURL );
            featureTypes = extractFeatureTypes( doc ); 
        }
        return featureTypes;
    }

    public String[] getFeatureProperties(String featureType) {
        String[] featProps = (String[])this.featureProperties.get( featureType );
        if(featProps == null ){ // then init
            createAttributes( featureType );
        }
        return (String[])this.featureProperties.get( featureType );
    }

    public String getGeometryProperty(String featureType) {
        return (String)this.geoProperties.get( featureType );
    }
}
