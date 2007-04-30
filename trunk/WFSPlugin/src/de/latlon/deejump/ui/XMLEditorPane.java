/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Moataz Elmasry (elmasry@lat-lon.de)
 * @author Ugo Taddei (taddei@lat-lon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */

package de.latlon.deejump.ui;

import java.awt.Dimension;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;

/**
 * This class extends a JEditorPanel in order to show XML text with proper
 * syntax highlighting.
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class XMLEditorPane extends JEditorPane {

    private static XSLTDocument xslt;

    /**
     * @param txt
     */
    public XMLEditorPane( String txt ) {
        super( "text/html", toCleanHtml(txt) );
    }

    /**
     * @param txt The text to be formatted
     * @param formatter the path to the format style file
     * @return
     */
    private static String toHtml( String txt ) {
        
        if( xslt == null ){
            xslt = new XSLTDocument();
        }
        try {
            final String filename = "xml2html.xsl";
            xslt.load( XMLEditorPane.class.getResource( filename ) );
            XMLFragment xml = new XMLFragment();
            xml.load( new StringReader( txt ), 
                      XMLEditorPane.class.getResource( filename ).toString() );

            StringWriter sw = new StringWriter();

            // workaround for error using com.sun parser
            DOMSource xslSource = new DOMSource( xslt.getRootElement().getOwnerDocument() );
            DOMSource xmlSource = new DOMSource( xml.getRootElement().getOwnerDocument() );
            StreamResult result = new StreamResult( sw );
            XSLTDocument.transform( xmlSource, xslSource, result, null, null );
            
            return sw.toString(); 

        } catch ( Exception e ) {
            e.printStackTrace();
            return txt;
        }
    }

    /**
     *  this function is used only onside the class for testing purposes
     * @param txt
     * @param formatter
     */
    private static String toCleanHtml( String txt ) {

        String html = toHtml( txt );
        int openHead = html.indexOf("<html");
        int closeHead = html.indexOf("<body>");
        String result = html.substring(openHead,closeHead); 
        return StringTools.replace(html, result, "<html>", true);
        
    }
    
    public static void main( String[] args ){
        
        String txt = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+
        "<wfs:GetFeature xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:wfs=\"http://www.opengis.net/wfs\" outputFormat=\"GML2\">"+
        "<wfs:Query xmlns:topp=\"http://www.openplans.org/topp\" typeName=\"topp:strassen-joined\">"+
        "<wfs:PropertyName>"+
        "topp:the_geom</wfs:PropertyName>"+
        "<wfs:PropertyName>"+
        "topp:strShapeID</wfs:PropertyName>"+        
        "</wfs:Query>"+
        "</wfs:GetFeature>";
        
        JFrame jf = new JFrame();
        jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        jf.getContentPane().add( new XMLEditorPane(txt) );
        jf.setSize( 600, 500 );
        jf.setVisible( true );
    }
    
}
