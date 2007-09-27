/*----------------    FILE HEADER  ------------------------------------------

This file is part of deegree.
Copyright (C) 2001 by:
EXSE, Department of Geography, University of Bonn
http://www.giub.uni-bonn.de/exse/
lat/lon Fitzke/Fretter/Poth GbR
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
lat/lon Fitzke/Fretter/Poth GbR
Meckenheimer Allee 176
53115 Bonn
Germany
E-Mail: poth@lat-lon.de

Jens Fitzke
Department of Geography
University of Bonn
Meckenheimer Allee 166
53115 Bonn
Germany
E-Mail: jens.fitzke@uni-bonn.de

                 
 ---------------------------------------------------------------------------*/
package de.latlon.deejump.plugin.ows3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.deegree.xml.XMLTools;
import org.deegree_impl.tools.StringExtend;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Little conveniece class to hold code that nobody wants
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class ClientHelper {

    private static Logger LOG = Logger.getLogger( ClientHelper.class );

    private ClientHelper(){ 
        // prevent initialization 
    }
    
    public static Document createDocFromRequest( HttpClient httpClient, 
                                                 String methodType, 
                                                 String serverURL,
                                                 String request) throws IOException {

		LOG.info( methodType + " " + serverURL + " " + request );

    	Document doc = null;
        
        HttpMethod httpMethod = null; 
        if ( ClickThruPlugIn.HTTP_GET.equals( methodType ) ) {
            httpMethod = new GetMethod( serverURL );
            httpMethod.setQueryString( request );

//            httpMethod.setQueryString( request );
            /*httpClient .executeMethod( httpMethod );
            is = httpMethod.getResponseBodyAsStream();*/
        } else if ( ClickThruPlugIn.HTTP_POST.equals( methodType ) ) {
            httpMethod = new PostMethod( serverURL );

            /*httppost.setRequestBody( "DUMMY" );
            httpClient .executeMethod( httppost );
            is = httppost.getResponseBodyAsStream();*/
            System.out.println( "post it" );
        } else {
            throw new RuntimeException( "Not a valid method type: " + methodType);
        }
        httpClient .executeMethod( httpMethod );
        System.out.println( httpMethod.getResponseBodyAsString() );
        InputStream is = httpMethod.getResponseBodyAsStream();
        
        InputStreamReader ireader = new InputStreamReader( is );
        BufferedReader br = new BufferedReader( ireader );
        StringBuffer sb = new StringBuffer( 10000 );
        String s = null;

        while (( s = br.readLine() ) != null) {
            sb.append( s );
        }
        s = sb.toString();
        br.close();
        try {
/*            if ( s.indexOf( "<Exception>" ) >= 0 ) {
                throw new Exception( "Couldn't get data from WFS:\n"
                    + s );
            }
            */
            StringReader sr = new StringReader( s );
            doc = XMLTools.parse( sr );
            
            return doc;
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }

    
    public static String prettyPrintNode(Node node) {
        
        StringBuffer sb = new StringBuffer(10000);
        
        switch (node.getNodeType ()) {
            case Node.DOCUMENT_NODE: {
                sb.append( "<?xml version=\"1.0\"?>\n");
                Document doc = (Document) node;
                sb.append( prettyPrintNode(doc.getDocumentElement ()) );
                break;
            }
            case Node.ELEMENT_NODE: {
                String name = node.getNodeName ();
                sb.append( "<" + name);
                NamedNodeMap attributes = node.getAttributes ();
                for (int i = 0; i < attributes.getLength (); i++) {
                    Node current = attributes.item (i);
                    String value = current.getNodeValue ();
                    value = StringExtend.replace( value, "&", "&amp;", true);
                    sb.append( " " + current.getNodeName () ).append( "=\"" )
                      .append( value ).append( "\"");
                }
                sb.append( ">");
    
                // Kinder durchgehen
                NodeList children = node.getChildNodes ();
                if (children != null) {
                    for (int i = 0; i < children.getLength (); i++) {
                        sb.append( prettyPrintNode(children.item (i)) );
                    }
                }
    
                sb.append( "</" + name + ">\n");
                break;
            }
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE: {
                String trimmed = node.getNodeValue ().trim ();
                if (!trimmed.equals ("")) 
                    sb.append( trimmed );                
                break;
            }
            case Node.PROCESSING_INSTRUCTION_NODE: {
                break;
            }
            case Node.ENTITY_REFERENCE_NODE: {
                break;
            }
            case Node.DOCUMENT_TYPE_NODE: {
                break;
            }
        }
        return sb.toString();
    }
    
    
    

}
