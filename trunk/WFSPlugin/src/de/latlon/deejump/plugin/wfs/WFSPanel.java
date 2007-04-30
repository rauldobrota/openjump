/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.latlon.deejump.plugin.wfs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryImpl;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;

import com.vividsolutions.jts.geom.Envelope;

import de.latlon.deejump.ui.ExtensibleComboBox;
import de.latlon.deejump.ui.Messages;
import de.latlon.deejump.ui.XMLEditorPane;

/**
 * This is a panel which contains other basic GUIs for accessing Features of
 * a WFS.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSPanel extends JPanel {

    //TODO put a props
    static final String releaseVersion = "0.1.0";
    
    // Constants for spatial search criteria type
    // also used by child panels
    /** Search uses no spatial criteria */
    public static final String NONE = "NONE";

    /** Search uses bounding box as spatial criteria */
    public static final String BBOX = "BBOX";

    /** Search uses a selected (GML) geometry as spatial criteria */
    public static final String SELECTED_GEOM = "SELECTED_GEOM";
    
    private static List servers = new ArrayList();

    /**
     * The standard geometry type name (used when getting schemata and creating
     * filters with spatial clauses
     */
    public static final String GEOMETRY_PROPERTY_NAME = "GEOM";

    /** The panel containing the interface for attribute-based search */
    private PropertyCriteriaPanel attributeResPanel;

    /** The panel containing the interface for geometry-based search */
    private SpatialCriteriaPanel spatialResPanel;

    String[] attributeNames = new String[] {};

    private QualifiedName[] geoProperties;

    private QualifiedName geoProperty;

    AbstractWFSWrapper wfService;

    private JTextArea requestTextArea;

    private JTextArea responseTextArea;

    private JComboBox serverCombo;

//    private JButton okButton;

    private JTabbedPane tabs;

    private JComboBox featureTypeCombo;

//    private JPanel mainPanel;

    private Box box;

    // TODO remove dependency on JUMP/JTS use deegree Envelope
    /** The envelope of the current bounding box */
    private Envelope envelope = new Envelope( -1d, -1d, 1d, 1d );

    //private GMLGeometry gmlBbox;
    private Geometry selectedGeom;

    private String srs = "EPSG:4326";

    private PropertySelectionPanel propertiesPanel;
    
    private JButton capabilitiesButton;

    protected String wfsVersion;

    private WFSOptions options;

    WFSPanelButtons controlButtons;

    public WFSPanel(List<String> urlList){
        super();
        setWFSList( urlList );
        initGUI();
        this.options = new WFSOptions();
    }

    private void initGUI() {

        LayoutManager lm = new BoxLayout( this, BoxLayout.Y_AXIS );
        setLayout( lm );

        //combo box for WFS URLs
        serverCombo = createServerCombo();
        Dimension d = new Dimension( 400, 45 );
        serverCombo.setPreferredSize( d );
        serverCombo.setMaximumSize( d );
        String txt = Messages.getString( "FeatureResearchDialog.wfsService" );
        serverCombo.setBorder( BorderFactory.createTitledBorder( txt ) ); 
        txt = Messages.getString( "FeatureResearchDialog.wfsServiceToolTip" );
        serverCombo.setToolTipText( txt );
        
        add(serverCombo);
        
        // connect and capabilities button
        JButton connecButton = new JButton( Messages.getString( "FeatureResearchDialog.connect" ) );
        connecButton.setAlignmentX( 0.5f );
        connecButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                reinitService( (String) serverCombo.getSelectedItem() );
            }
        } );

        capabilitiesButton = new JButton( "Capabilities..." );
        capabilitiesButton.setEnabled( false );
        capabilitiesButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                createXMLFrame( WFSPanel.this, wfService.getCapabilitesAsString());
            }
        } );
        
        JPanel p = new JPanel();
        p.setLayout( new BoxLayout(p, BoxLayout.Y_AXIS ) );
        
        // version buttons
        p.add( createVersionButtons( new String[]{ "1.0.0", "1.1.0" } ) );
        
        
        JPanel innerPanel = new JPanel();
        innerPanel.add( connecButton );
        innerPanel.add( capabilitiesButton );
        p.add( innerPanel );
        
        featureTypeCombo = createFeatureTypeCombo();
        //featureTypeCombo.setVisible( false );
        featureTypeCombo.setEnabled( false );
        p.add( featureTypeCombo );

        //FIXME what's this???
        setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        add( p );
        
        final Dimension dim = new Dimension( 400, 570 );
        final Dimension minDim = new Dimension( 400, 500 );

        tabs = new JTabbedPane() {
            public Dimension getPreferredSize() {
                return dim;
            }

            public Dimension getMinimumSize() {
                return minDim;
            }
        };

        attributeResPanel = new PropertyCriteriaPanel( this, featureTypeCombo );
        attributeResPanel.setEnabled( false );
        tabs.add( Messages.getString( "FeatureResearchDialog.attributeSearch" ), attributeResPanel );

        propertiesPanel = new PropertySelectionPanel( this );
        tabs.add( "Properties", propertiesPanel );

        spatialResPanel = new SpatialCriteriaPanel( this );
        tabs.add( Messages.getString( "FeatureResearchDialog.spatialSearch" ), spatialResPanel );

        tabs.add( Messages.getString( "FeatureResearchDialog.request" ), createRequestTextArea() );

        //TODO i18n
        tabs.add( Messages.getString( "FeatureResearchDialog.response" ), createrResponseTextArea() );

        box = Box.createHorizontalBox();
        box.setBorder( BorderFactory.createEmptyBorder( 20, 5, 10, 5 ));
        
        
        add( tabs );
        tabs.setVisible( false );
        
                
//        setMinimumSize( new Dimension( 400, 300 ) );
//        setPreferredSize( new Dimension( 400, 600 ) );
    }
    
    // Gh 15.11.05
    private JComboBox createServerCombo() {
        // 
        if ( wfService != null ) {
            servers.add( 0, wfService.getCapabilitiesURL() );
        }
        String[] server = (String[]) servers.toArray( new String[servers.size()] );
        final ExtensibleComboBox extensibleComboBox = new ExtensibleComboBox( server );
        extensibleComboBox.setSelectedIndex( 0 );
        extensibleComboBox.addItemListener( new ItemListener() {
            public void itemStateChanged( ItemEvent e ) {
                if ( e.getStateChange() == ItemEvent.SELECTED ) {
                    /*String selected = extensibleComboBox.getSelectedItem().toString();
                    reinitService( selected );
                     */
                }
            }
        } );
        return extensibleComboBox;
    }    

    static void createXMLFrame( Component parent, String txt ) {
        
        //FIXME: this is still too slow...
        
        //JTextArea ta = new JTextArea( txt, 20, 80 );
        XMLEditorPane xe = new XMLEditorPane(txt);
//        ta.setLineWrap( true );
        JScrollPane sp = new JScrollPane( xe, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        sp.setMaximumSize( new Dimension(600,400) );
        sp.setPreferredSize( new Dimension(800,400) );
        JOptionPane.showMessageDialog( parent, sp ); 
    
    }

    private void reinitService( String url ) {
        try {
            wfService = "1.1.0".equals( this.wfsVersion ) ? 
                            new WFServiceWrapper_1_1_0( url ) :
                            new WFServiceWrapper_1_0_0( url );    
            refreshGUIs();
        } catch ( Exception e ) {

            JOptionPane.showMessageDialog( this, "Could not connect to WFS server at '" + url
                                                 + "'\n" + e.getMessage(), "Error",
                                           JOptionPane.ERROR_MESSAGE );

            this.controlButtons.okButton.setEnabled( false );
            e.printStackTrace();
        }

    }

    private Component createVersionButtons( String[] versions ) {
        JPanel p = new JPanel();
        
        p.add( new JLabel( Messages.getString( "FeatureResearchDialog.version" ) ) );
        ButtonGroup bg = new ButtonGroup();
        for ( int i = 0; i < versions.length; i++ ) {
            final JRadioButton b = new JRadioButton( versions[i] );
            b.addActionListener( new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    WFSPanel.this.wfsVersion = b.getText(); 
                }
            } );
            bg.add( b );
            if( i == 0 ){//first is clicked 
                b.doClick();
            }
            p.add( b );
        }
        return p;
    }

    private JComboBox createFeatureTypeCombo() {
        String[] start = { "            " };
        JComboBox tmpFeatureTypeCombo = new JComboBox( start );
        Dimension d = new Dimension( 300, 60 );
        tmpFeatureTypeCombo.setPreferredSize( d );
        tmpFeatureTypeCombo.setMaximumSize( d );
        
        
        Border border = 
            BorderFactory
                .createTitledBorder( Messages.getString( "FeatureResearchDialog.featureType" ) );
        
        Border border2 = BorderFactory.createEmptyBorder( 5, 2, 10, 2 );
        
        border2 = BorderFactory.createCompoundBorder( border2, border );
        tmpFeatureTypeCombo.setBorder( border2 );
        tmpFeatureTypeCombo.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( java.awt.event.ActionEvent evt ) {

                JComboBox combo = (JComboBox) evt.getSource();

                try {
                    attributeNames = wfService.getProperties( (String) combo.getSelectedItem() );

                    attributeResPanel.refreshPanel();
                    geoProperties = getGeoProperties();
                    propertiesPanel.setProperties( attributeNames, geoProperties );
                    spatialResPanel.resetGeoCombo( geoProperties );

                } catch ( Exception e ) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog( WFSPanel.this,
                                                   "Error loading schema: " + e.getMessage() );
                }
            }
        } );

        return tmpFeatureTypeCombo;
    }
    
    private JComponent createRequestTextArea() {

        JPanel p = new JPanel();
        p.setLayout( new BoxLayout( p, BoxLayout.Y_AXIS ) );

        requestTextArea = new JTextArea();
        requestTextArea.setLineWrap( true );
        requestTextArea.setWrapStyleWord( true );
        requestTextArea.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        JScrollPane jsp = new JScrollPane( requestTextArea );

        p.add( jsp );

        JButton createReq = new JButton(
                                         Messages.getString( "FeatureResearchDialog.createWFSRequest" ) );
        createReq.setBounds( 260, 20, 80, 20 );
        createReq.setAlignmentX( 0.5f );
        createReq.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                setRequestText( createRequest().toString() );
                tabs.setSelectedIndex( 3 );
            }
        } );

//TODO i18n
        JButton validateReq = new JButton( "Validate request");//
//                                        Messages.getString( "FeatureResearchDialog.createWFSRequest" ) );

        validateReq.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                try {
                    XMLFragment xf = new XMLFragment();
                    xf.load( new StringReader(requestTextArea.getText()), "dummy" );
                    GetFeature.create( null, xf.getRootElement() );
                } catch ( Exception ex ) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog( WFSPanel.this,
                                                   ex.getMessage(), 
                                                   "Error",
                                                   JOptionPane.ERROR_MESSAGE );
                }
            }
        } );
        
        JPanel innerPanel = new JPanel();
        innerPanel.add( createReq );
        innerPanel.add( validateReq );
        
        p.add( innerPanel );
        
        return p;
    }

    private JComponent createrResponseTextArea() {

        JPanel p = new JPanel();
        p.setLayout( new BoxLayout( p, BoxLayout.Y_AXIS ) );

        responseTextArea = new JTextArea();
        responseTextArea.setLineWrap( true );
        responseTextArea.setWrapStyleWord( true );
        responseTextArea.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        JScrollPane jsp = new JScrollPane( responseTextArea );

        p.add( jsp );

        return p;
    }    

    /** Initializes the FeatureType combo box of the AttributeResearchPanel */

    private void refreshGUIs() {

        String[] featTypes = null;

        try {
            featTypes = wfService.getFeatureTypes();
            featureTypeCombo.setModel( new javax.swing.DefaultComboBoxModel( featTypes ) );

            controlButtons.okButton.setEnabled( true );
            capabilitiesButton.setEnabled( true );
            tabs.setEnabledAt( 1, true );

            featureTypeCombo.setEnabled( true );
//            featureTypeCombo.setVisible( true );
            attributeResPanel.setFeatureTypeComboEnabled( true );

        } catch ( Exception e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog( this, "Could not connect to WFS server at '"
                                                 + wfService.getBaseWfsURL() + "'\n" + e.getMessage(),
                                           "Error", JOptionPane.ERROR_MESSAGE );

            featureTypeCombo.setModel( new javax.swing.DefaultComboBoxModel( new String[] {} ) );
            attributeResPanel.setFeatureTypeComboEnabled( false );
            tabs.setEnabledAt( 1, false );
            capabilitiesButton.setEnabled( false );
            controlButtons.okButton.setEnabled( false );
        }

        if ( featTypes != null && featTypes.length > 0 ) {
            try {
                attributeNames = wfService.getProperties( featTypes[0] );
                attributeResPanel.setEnabled( true );
                geoProperties = wfService.getGeometryProperties( featTypes[0] );

                propertiesPanel.setProperties( attributeNames, geoProperties );
                propertiesPanel.setEnabled( true );
                spatialResPanel.resetGeoCombo( geoProperties );

            } catch ( Exception e ) {

                e.printStackTrace();

                attributeResPanel.setEnabled( false );
                propertiesPanel.setEnabled( false );

                JOptionPane.showMessageDialog( this, "Could not get DescribeFeatureType for '"
                                                     + featTypes[0] + "' from WFS server at '"
                                                     + wfService.getBaseWfsURL() + "'\n"
                                                     + e.getMessage(), "Error",
                                               JOptionPane.ERROR_MESSAGE );
            }
        }

    }
    
    /** Creates a GetFeature request by concatenation of xlm elements */
    private StringBuffer createRequest() {
        
        StringBuffer sb = new StringBuffer();
        if( wfService == null ){//not inited yet
            return sb;
        }
        
        sb.append( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" );

        final String outputFormat = options.getSelectedOutputFormat();
        
        System.out.println("FT namespace is missing in the xml??");
        
        sb.append( "<wfs:GetFeature xmlns:ogc=\"http://www.opengis.net/ogc\" " )
            .append("xmlns:gml=\"http://www.opengis.net/gml\" " )
            .append("xmlns:wfs=\"http://www.opengis.net/wfs\" service=\"WFS\" " )
            .append("version=\"").append( wfService.getServiceVersion() ).append( "\" " )
            .append("outputFormat=\"")
            .append( outputFormat )
            .append( "\">" )
            .append( "<wfs:Query " );

        String ftName = (String) featureTypeCombo.getSelectedItem();
        QualifiedName ft = wfService.getQualiNameByFeatureTypeName( ftName );

        sb.append( "xmlns:" ).append( ft.getPrefix() ).append( "=\"" )
        .append( ft.getNamespace() ).append("\" " ).append("typeName=\"" ).append( ftName ).append("\">" );

        sb.append( propertiesPanel.getXmlElement() );

        String spatCrit = attributeResPanel.getSpatialCriteria();

        int listSize = attributeResPanel.getListSize();

        String[] filterTags = new String[] { "", "" };

        if ( listSize > 0 || !NONE.equals( spatCrit ) ) {
            filterTags = createStartStopTags( "Filter" );
        }
        sb.append( filterTags[0] );

        boolean includesSpatialClause = !NONE.equals( spatCrit );
        if ( includesSpatialClause && listSize > 0 ) {
            sb.append( WFSPanel.createStartStopTags( "And" )[0] );
        }

        if ( BBOX.equals( spatCrit ) ) {
            sb.append( createBboxGml() );
        } else if ( SELECTED_GEOM.equals( spatCrit ) ) {
            sb.append( spatialResPanel.getXmlElement() );
        }

        sb.append( attributeResPanel.getXmlElement() );
        if ( includesSpatialClause && listSize > 0 ) {
            sb.append( WFSPanel.createStartStopTags( "And" )[1] );
        }

        sb.append( filterTags[1] );
        sb.append( "</wfs:Query></wfs:GetFeature>" );

        return sb;
    }

    /**
     * Creates the XML fragment containing a bounding box filter
     * 
     * @return the XML fragment containing a bounding box filter
     */
    private StringBuffer createBboxGml() {

        StringBuffer sb = new StringBuffer( 500 );

        QualifiedName ft = getFeatureType();
        QualifiedName qn = getChosenGeoProperty();

        if ( envelope != null ) {
            sb.append( "<ogc:BBOX>" ).append( "<ogc:PropertyName>" )
                .append( ft.getPrefix() ).append(":" ).append(qn.getLocalName() )
                .append("</ogc:PropertyName>" ).append("<gml:Box><gml:coord>" )
                .append("<gml:X>" ).append(envelope.getMinX() ).append("</gml:X>" )
                .append("<gml:Y>" ).append(envelope.getMinY() ).append("</gml:Y>" )
                .append("</gml:coord><gml:coord>" ).append("<gml:X>" )
                .append(envelope.getMaxX() ).append("</gml:X>" ).append("<gml:Y>" )
                .append(envelope.getMaxY() ).append("</gml:Y>" )
                .append("</gml:coord></gml:Box></ogc:BBOX>" );
        }

        return sb;
    }
    private void setRequestText( String text ) {
        requestTextArea.setText( text.replaceAll( ">", ">\n" ) );
    }

    /**
     * Convenience method to create XML tags mit "ogc" namespace. For example an
     * input like MyTag will return <code>{"<ogc:MyTag>", "</ogc:MyTag>"}</code>
     * 
     * @param tagName
     *            the tag name
     * @return a String[] with start and stop tags
     */
    public static final String[] createStartStopTags( String tagName ) {
        String[] tags = new String[] { "<ogc:" + tagName + ">", "</ogc:" + tagName + ">" };
        return tags;
    }
    
    public QualifiedName getChosenGeoProperty() {
        return geoProperty;
        /*QualifiedName[] qns = wfService.getGeometryProperties(this.getFeatureType().getAsString());
         QualifiedName qn = null;
         if ( qns.length > 1 ){
         qn = qns[0];
         }
         return qn;*/
    }    
    
    public void setGeoProperty( QualifiedName geoProp ) {
        this.geoProperty = geoProp;
    }

    public QualifiedName[] getGeoProperties() {
        return this.wfService.getGeometryProperties( (String) featureTypeCombo.getSelectedItem() );
    }
    
    /**
     * Returns the currently chosen feature type
     * 
     * @return the name of the currently chosen feature type
     */
    public QualifiedName getFeatureType() {
        String s = (String) featureTypeCombo.getSelectedItem();
        return wfService.getQualiNameByFeatureTypeName( s );
    }    

    public AbstractWFSWrapper getWfService() {
        return this.wfService;
    }

    /**
     * Returns the currently selected geometry that serves as basis for spatial
     * operation operations
     * 
     * @return the currently selected geometry
     */
    public Geometry getSelectedGeometry() {
        return this.selectedGeom;
    }
    
    public void setGMLGeometrySRS( CoordinateSystem cs ) {
        //FIXME is this needed?
        //      this.srs = cs;
        if ( this.selectedGeom != null ) {
            ( (GeometryImpl) this.selectedGeom ).setCoordinateSystem( cs );
        }
    }
    
    //GH 29.11.05
    public void setWFSList( List serverURLS ) {
        servers = serverURLS;
    }
    
    public void setResposeText( String txt ){
        responseTextArea.setText( txt );
    }
    
    public String getRequest(){
        String t = requestTextArea.getText();
        if( t == null || t.length() == 0 ){
            t = createRequest().toString();
        }
        return t;
    }
    
    public void setTabsVisible( boolean visible ){
        tabs.setVisible( visible );
    }

    public WFSOptions getOptions() {
        return this.options;
    }

    public String getResponse() {
        return this.responseTextArea.getText();
    }

    public String getGMLGeometrySRS() {
        return this.srs;
    }

    public void setEnvelope( Envelope env ) {
        this.envelope = env;
    }

    public void setSelectedGMLGeometry( Geometry gmlGeom ) {
        // TODO Auto-generated method stub
        
    }
    
}
