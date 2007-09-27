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
package de.latlon.deejump.plugin.style;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.openjump.core.ui.util.ScreenScale;
import org.w3c.dom.NamedNodeMap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.java2xml.Java2XML;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.Viewport;

import de.latlon.deejump.plugin.wfs.WFSLayer;

/**
 * ...
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
 *  
 */
public class LayerStyle2SLDPlugIn extends AbstractPlugIn {

    private static final String xsltString; 
    
    /**
     * The <code>Transformer</code> object used in the transformation of a task/project/layer xml
     * to sld.
     */
//    protected static Transformer transformer = null;
    private Transformer transformer = null;

    private static final String WMS_LAYER_NAME = "WMS Layer name"; 

    private static final String STYLE_NAME = "Style name"; 

    private static final String STYLE_TITLE = "Style title"; 
    
    private static final String FEATURETYPE_STYLE = "Feature Type Style"; 
    
    private static final String GEOTYPE = "geoType"; 
    
    private static final String UTF_8 = "UTF-8";

    private static final String ISO_8859_1 = "ISO-8859-1";
    
    private static final String GEOM_PROPERTY = "geomProperty";

    private static final String PREFIX = "NamespacePrefix";

    private static final String NAMESPACE = "Namespace"; 

    private static String SCALE_MIN = "minScale"; 

    private static String SCALE_MAX = "maxScale"; 

    
    static{
        try {
            xsltString = readXSLT();
        } catch ( UnsupportedEncodingException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        } catch ( IOException e ) {
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }
    
    private Java2XML java2Xml = new Java2XML();

    private JFileChooser fileChooser;
    
    private MultiInputDialog dialog;

    private String wmsLayerName = WMS_LAYER_NAME;
    
    private String styleName = STYLE_NAME; 
    
    private String styleTitle = STYLE_TITLE;
    
    private String featureTypeStyle = FEATURETYPE_STYLE; 
    
    private double scaleFactor = 1d;
    
    private String geoProperty = "GEOM";

    private String prefix = "_app";

    private String namespace = "http://http://www.deegree.org/app_";
    
    public String getName() {
        return "Transform layer style into sld";
    }

    private static String readXSLT() throws UnsupportedEncodingException, IOException {
        URL xslUrl = LayerStyle2SLDPlugIn.class.getResource( "layerstyle2sld.xsl" );
        
        InputStreamReader isr = new InputStreamReader( xslUrl.openStream(), ISO_8859_1);
        
        BufferedReader br = new BufferedReader( isr );
        StringBuffer sb = new StringBuffer( 50000 );
        String s = null;
        while (( s = br.readLine() ) != null) {
            sb.append( s );
        }
        s = sb.toString();
        br.close();
        
        return s;
    }

    public void install( PlugInContext context ) throws Exception {

        context.getWorkbenchContext().getWorkbench().getFrame().getToolBar().addPlugIn(
            getIcon(),
            this, 
            createEnableCheck(context.getWorkbenchContext()),
            context.getWorkbenchContext()
        );
    }

    public boolean execute( PlugInContext context ) throws Exception {
        reportNothingToUndoYet( context );
        
        Layer layer = context.getSelectedLayer( 0 );
        if ( layer == null ) {
            return false;
        }

        if( layer instanceof WFSLayer ){
            WFSLayer wfsLayer = (WFSLayer)layer;
            QualifiedName qn = wfsLayer.getQualifiedName();
            prefix = qn.getPrefix();
            namespace = qn.getNamespace().toString();
            
        }

        initDialog(context);
        
        dialog.setVisible(true);

        if (!dialog.wasOKPressed()) {
            return false;
        }
        
        
        wmsLayerName = dialog.getText( WMS_LAYER_NAME );
        styleName = dialog.getText( STYLE_NAME );
        styleTitle = dialog.getText( STYLE_TITLE );
        featureTypeStyle = dialog.getText( FEATURETYPE_STYLE );
        geoProperty = dialog.getText( GEOM_PROPERTY );
        prefix = dialog.getText( PREFIX );
        namespace = dialog.getText( NAMESPACE );
    
        
        
        if ( fileChooser == null ) {
            fileChooser = new JFileChooser();
            fileChooser.setApproveButtonText( "Save" );
            fileChooser.setDialogTitle( "Save style as SLD" );
        }
        
        if (JFileChooser.APPROVE_OPTION == 
            	fileChooser.showOpenDialog(context.getWorkbenchFrame())) {
            
            //FIXME no good: saving to file to transform...
            File file = File.createTempFile( "temptask", ".xml" );
            file.deleteOnExit();
            
            activateVertexStyle( layer );
            
            scaleFactor = calcScaleFactor( context.getLayerViewPanel() );
            
            transformXML( layer, file, fileChooser.getSelectedFile(), scaleFactor);
            
        }
        
        return true;
    }

    
    /**
     * When starting up, vertex styles might be deactivated, though they are "shown". So, if 
     * layer if of point type, and basic style is activated, activated vertex style too.
     * @param layer
     */
    private void activateVertexStyle( Layer layer ) {
        Iterator iter = layer.getFeatureCollectionWrapper().getUltimateWrappee()
    	.getFeatures().iterator();
        String type = "";
        if( iter.hasNext() ){
            Feature f = (Feature)iter.next();
            type = f.getGeometry().getGeometryType();
             
        }
        if ( layer.getBasicStyle().isEnabled() ){
            if( "MultiPoint".equals( type ) || "Point".equals( type ) ){
                layer.getVertexStyle().setEnabled( true );
            }
        }
    }

    private void initDialog(PlugInContext context) {
        if( dialog == null ){
            
	        dialog = new MultiInputDialog(context.getWorkbenchFrame(),"SLD Parameters", true);
	        
	        dialog.addSeparator();
	        
            dialog.addTextField( PREFIX, prefix, 25, null, "Input the namespace prefix." );
            dialog.addTextField( NAMESPACE, namespace, 25, null, "Input the name of the geometry property." );
            dialog.addTextField( GEOM_PROPERTY, geoProperty, 25, null, "Input the unqualified name of the geometry property." );
            
            dialog.addSeparator();
            String name = context.getCandidateLayer( 0 ).getName();
            
            dialog.addTextField( WMS_LAYER_NAME, name, 25, null, WMS_LAYER_NAME );
	        dialog.addTextField( STYLE_NAME, name, 25, null, STYLE_NAME );
	        dialog.addTextField( STYLE_TITLE, name, 25, null, STYLE_TITLE );
	        dialog.addTextField( FEATURETYPE_STYLE, name, 25, null, FEATURETYPE_STYLE );
	        GUIUtil.centreOnWindow(dialog);
        }
    }
    
    private void transformXML( Layer layer, File inputXML, File outputXML, double scaleFactor ) throws Exception{

        //TODO don't assume has 1 item!!!
        BasicFeature bf = (BasicFeature)layer.getFeatureCollectionWrapper().getFeatures().get( 0 );
        Geometry geo = bf.getGeometry();
        String geoType = geo.getGeometryType(); 
                
        java2Xml.write( layer,"layer", inputXML );
        
        FileInputStream input = new FileInputStream( inputXML );
        
        //FileWriter fw = new FileWriter( outputXML );
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream( outputXML ), ISO_8859_1);
        
        HashMap map = new HashMap( 10 );
        map.put( WMS_LAYER_NAME, wmsLayerName );
        map.put( FEATURETYPE_STYLE, featureTypeStyle );
        map.put( STYLE_NAME, styleName );
        map.put( STYLE_TITLE, styleTitle );
        map.put( GEOTYPE, geoType );
        map.put( GEOM_PROPERTY, geoProperty );
        map.put( PREFIX, prefix );
        
        
        // ATENTION : note that min and max are swapped in JUMP!!!
        // will swap later, in transformContext
        Double d = layer.getMinScale();
        d = d != null ? d : new Double( 0 ); 

        map.put( SCALE_MIN, toRealWorldScale( scaleFactor, d.doubleValue()) );
        
        // using  Double.MAX_VALUE  is creating a large number - too many 0's
        // make it simple and hardcde a large number
        final double largeNumber = 99999999999d;
        d = layer.getMaxScale();
        d = d != null ? d : new Double( largeNumber );
        
        map.put( SCALE_MAX, toRealWorldScale( scaleFactor, d.doubleValue()) );
        
        
        fw.write( transformContext( input, map ) );
        fw.close();

    }
    
    public Icon getIcon() {
        return new ImageIcon(LayerStyle2SLDPlugIn.class.getResource("sldstyle.png"));
    }

    public void run( TaskMonitor monitor, PlugInContext context ) throws Exception {
        // will need this? extend threaded plug-in
    }

    public String transformContext( InputStream layerXML, HashMap parMap)
        throws TransformerException, UnsupportedEncodingException {

        
        initTransformer();
        
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult( sw );
        
        InputStreamReader isr = new InputStreamReader( layerXML, ISO_8859_1);
        StreamSource streamSource = new StreamSource( isr );

        //if you don't clear the pars, xalan throws a nasty NPE
        transformer.clearParameters();
        
        //TODO ths is getting too long -> iterate over pars and set them
        transformer.setParameter( "wmsLayerName", parMap.get( WMS_LAYER_NAME ) );
        transformer.setParameter( "featureTypeStyle", parMap.get( FEATURETYPE_STYLE ) );
        transformer.setParameter( "styleName", parMap.get( STYLE_NAME ) );
        transformer.setParameter( "styleTitle", parMap.get( STYLE_TITLE ) );
        transformer.setParameter( GEOTYPE, parMap.get( GEOTYPE ) );
        transformer.setParameter( GEOM_PROPERTY, parMap.get( GEOM_PROPERTY ) );
        transformer.setParameter( SCALE_MIN, parMap.get( SCALE_MIN ) );
        transformer.setParameter( SCALE_MAX, parMap.get( SCALE_MAX ) );
        transformer.setParameter( PREFIX, parMap.get( PREFIX ) );
        
        
        transformer.transform( streamSource, sr );

        try {
            sw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return sw.toString();
    }

    private void initTransformer() throws TransformerConfigurationException {

        try {

//            URL xslUrl = LayerStyle2SLDPlugIn.class.getResource( "layerstyle2sld.xsl" );
            
//            InputStreamReader isr = new InputStreamReader( xslUrl.openStream(), ISO_8859_1);
            
            StringBuffer sb = new StringBuffer( xsltString );
            String temp = "StyledLayerDescriptor ";
            int ix = sb.indexOf( temp );
            String newXslt = sb.insert( ix + temp.length(), "xmlns:" + prefix + "=\"" + namespace + "\" ").toString();
            
            InputStreamReader isr = new InputStreamReader( new StringBufferInputStream( newXslt ), ISO_8859_1 );
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            
            StreamSource streamSrc = new StreamSource( isr );
            
            transformer = transformerFactory.newTransformer( streamSrc );

//        } catch (TransformerConfigurationException e) {
//            e.printStackTrace();
//        } catch (TransformerFactoryConfigurationError e) {
//            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory ecf = new EnableCheckFactory(workbenchContext);
        
        MultiEnableCheck mec = new MultiEnableCheck()
        	.add( ecf.createWindowWithLayerNamePanelMustBeActiveCheck())
    		.add( ecf.createExactlyNLayerablesMustBeSelectedCheck( 1, Layer.class) );
        
        return mec;
        
    }
    
    public static final Double toRealWorldScale( double scaleFactor, double jumpScale ){
        
        
        return new Double( jumpScale/scaleFactor );
    }
    
    private double calcScaleFactor( LayerViewPanel panel ){
        double internalScale = 1d / panel.getViewport().getScale();
		double realScale = ScreenScale.getHorizontalMapScale(panel.getViewport());
		return internalScale / realScale;
    }
    
    public static void main(String[] args){
        
        try {

        FileInputStream input = 
            new FileInputStream( new File("f:/temp/input_layer_style2.xml") );

        FileWriter sw = new FileWriter( "f:/temp/sldoutput.xml");

        HashMap map = new HashMap( 4 );
        map.put( WMS_LAYER_NAME, "mrh:sehenswert" );
        map.put( FEATURETYPE_STYLE, "feature_test");
        map.put( STYLE_NAME, "mrh:sehenswert2" );
        map.put( STYLE_TITLE, "mrh:sehenswert2" );
        map.put( GEOTYPE, "Point" );
        
//        StringWriter sw = new StringWriter();
//        sw.write( transformContext( input, map ) );
        sw.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
}