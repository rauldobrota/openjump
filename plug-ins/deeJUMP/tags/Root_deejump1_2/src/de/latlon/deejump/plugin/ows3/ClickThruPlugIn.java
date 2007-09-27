/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI for visualizing and
 * manipulating spatial features with geometry and attributes.
 * 
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * 
 * For more information, contact:
 * 
 * Vivid Solutions Suite #1A 2328 Government Street Victoria BC V8T 5G5 Canada
 * 
 * (250)385-6040 www.vividsolutions.com
 */
package de.latlon.deejump.plugin.ows3;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.deegree.gml.GMLDocument;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.xml.DOMPrinter;
import org.deegree.xml.XMLTools;
import org.deegree_impl.gml.GMLDocument_Impl;
import org.deegree_impl.model.feature.FeatureFactory;
import org.deegree_impl.tools.CharsetUtility;
import org.w3c.dom.Document;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbenchContext;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.wms.MapLayerWizardPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.OneSRSWizardPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.SRSWizardPanel;
import com.vividsolutions.jump.workbench.ui.plugin.wms.URLWizardPanel;
import com.vividsolutions.jump.workbench.ui.wizard.WizardDialog;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.WMService;

import de.latlon.deejump.DeeJUMPWorkbench;
import de.latlon.deejump.plugin.wfs.GUIMessages;
import de.latlon.deejump.plugin.wfs.WFSLayer;
import de.latlon.deejump.plugin.wfs.WFSLayerListener;
import de.latlon.deejump.ui.Messages;
import de.latlon.deejump.util.data.JUMPFeatureFactory;

public class ClickThruPlugIn extends ThreadedBasePlugIn {

    // Common keys used in click-thru wizard

    static final String HTTP_CLIENT = "HTTP_CLIENT";

    static final String HTTP_METHOD = "HTTP_METHOD";

    static final String HTTP_GET = "HTTP GET";

    static final String HTTP_POST = "HTTP POST";

    static final String WFS_URL = "WFS_URL";

    static final String CAPABILITIES = "CAPABILITIES";

    static final String FEATURE_TYPE = "FEATURE_TYPE";

    static final String LICENCE = "LICENCE";

    static final String LICENCE_ID = "LICENCE_ID";

    private static Logger LOG = Logger.getLogger( ClickThruPlugIn.class );

    private HttpClient httpClient;

    private String featureType;

    private String serverURL;

    public boolean execute( final PlugInContext context ) throws Exception {
        //        reportNothingToUndoYet(context);

        if ( httpClient == null ) {
            httpClient = new HttpClient();
        }

        JFrame f = new JFrame();
        f.setLocation( 400, 400 );

        WizardDialog d = new WizardDialog( f, //TODO fill in with context.getWorkb frame
            "Click-Through WFS (GeoDRM-enabled WFS)", null );//TODO get error handler

        d.setData( HTTP_CLIENT, httpClient );

        d.init( new WizardPanel[] { new WFSURLWizardPanel(), new FeatureListPanel(),
                                   new LicencePanel() } );

        //Set size after #init, because #init calls #pack. [Jon Aquino]
        d.setSize( 500, 400 );
        GUIUtil.centreOnWindow( d );
        d.setVisible( true );
        if ( !d.wasFinishPressed() ) {
            return false;
        }

        featureType = (String) d.getData( FEATURE_TYPE );
        serverURL = (String) d.getData( ClickThruPlugIn.WFS_URL );

        return true;
    }

    public void run( TaskMonitor monitor, PlugInContext context ) throws Exception {
        monitor.report( Messages.getString( "WFSSearch.searching" ) );

        String query = createGetFeatureRequest();

        LOG.info( "WFS GetFeature: "
            + serverURL + " -> " + query );

        org.deegree.model.feature.FeatureCollection dfc = createDeegreeFCfromWFS( serverURL, query );

        if ( dfc != null ) {
            LayerManager layerManager = context.getLayerManager();

            // need a prefix to infor user it's not all layer
            String displayName = "WFS:"
                + featureType + " (r)";
            
            com.vividsolutions.jump.feature.FeatureCollection fc = JUMPFeatureFactory.createFromDeegreeFC( dfc );
            
            WFSLayer layer = JUMPFeatureFactory
            	.createWFSLayer( fc, displayName, featureType,
               "GEOMETRY", layerManager);

            layer.setServerURL( serverURL );

            WFSLayerListener layerListener = new WFSLayerListener( displayName );
            layerManager.addLayerListener( layerListener );

            layer.setLayerListener( layerListener );

            layerManager.addLayer( StandardCategoryNames.SYSTEM, layer ).setDataSourceQuery( null )
                .setFeatureCollectionModified( false );

        } else {
            JOptionPane.showMessageDialog( context.getWorkbenchFrame(), "No data found!", "Info",
                JOptionPane.WARNING_MESSAGE );

        }

    }

    public void install( PlugInContext context ) throws Exception {

        context.getWorkbenchContext().getWorkbench().getFrame().getToolBar()
            .addPlugIn( getIcon(), this, createEnableCheck( context.getWorkbenchContext() ),
                context.getWorkbenchContext() );
    }

    public MultiEnableCheck createEnableCheck( final WorkbenchContext workbenchContext ) {
        EnableCheckFactory checkFactory = new EnableCheckFactory( workbenchContext );
        return new MultiEnableCheck().add( checkFactory
            .createWindowWithLayerViewPanelMustBeActiveCheck() );
    }

    protected String createGetFeatureRequest() {
        StringBuffer req = new StringBuffer();
        req.append( "<?xml version='1.0' encoding='ISO-8859-1'?>" ).append(
            "<wfs:GetFeature xmlns:ogc='http://www.opengis.net/ogc' " ).append(
            "xmlns:gml='http://www.opengis.net/gml' " ).append(
            "xmlns:wfs='http://www.opengis.net/wfs' outputFormat='GML2'> " ).append(
            "<wfs:Query typeName='" ).append( featureType ).append( "'>" ).append(
            "</wfs:Query></wfs:GetFeature>" );

        return req.toString();
    }

    public ImageIcon getIcon() {
        return new ImageIcon( ClickThruPlugIn.class.getResource( "ows3.png" ) );
    }

    //TODO use ClientHelper...
    private FeatureCollection createDeegreeFCfromWFS( String serverUrl, String sreq )
        throws Exception {

        PostMethod httppost = new PostMethod( serverUrl );
        httppost.setRequestBody( sreq );
        httpClient.executeMethod( httppost );

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

        if ( s.indexOf( "<Exception>" ) >= 0
            || s.indexOf( "<ExceptionReport" ) >= 0 ) {
            throw new Exception( "Couldn't get data from WFS:\n"
                + s );
        }
        StringReader sr = new StringReader( s );
        doc = XMLTools.parse( sr );

        //		DOMPrinter.printNode( System.out, doc);

        //doc = XMLTools.parse( is );

        // transform data and store as shape file
        GMLDocument gmlDoc = new GMLDocument_Impl( doc );
        FeatureCollection newFeatCollec = FeatureFactory.createFeatureCollection( gmlDoc.getRoot() );

        return newFeatCollec;
    }

    public static void main( String[] args ) {
        try {

            /*
             * JFrame frame = new JFrame("Test Click-Thru"); frame.setDefaultCloseOperation(
             * JFrame.EXIT_ON_CLOSE );
             */
            ClickThruPlugIn clickThru = new ClickThruPlugIn();
            clickThru.execute( new PlugInContext( new JUMPWorkbenchContext( null ), null, null,
                null, null ) );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}