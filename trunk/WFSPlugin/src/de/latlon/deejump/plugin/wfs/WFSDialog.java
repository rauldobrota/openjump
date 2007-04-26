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
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DebugGraphics;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryImpl;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;

import com.vividsolutions.jts.geom.Envelope;

import de.latlon.deejump.ui.DeeJUMPException;
import de.latlon.deejump.ui.ExtensibleComboBox;
import de.latlon.deejump.ui.Messages;
import de.latlon.deejump.util.data.JUMPFeatureFactory;

/**
 * This dialog presents a graphical user interface to OGC Filter operations. It
 * encapsulates two panels, one for attribute-based feature search and the other
 * for geometry-based search. Both search methods can be combined. The dialog
 * generates a GetFeature request as an XML string. This can be used to query a
 * WFS.
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
 *  
 */
public class WFSDialog extends JDialog {

    public static final String WFS_URL_LIST = "WFS_URL_LIST";

    private static List servers = new ArrayList();

    // Constants for spatial search criteria type
    // also used by child panels
    /** Search uses no spatial criteria */
    public static final String NONE = "NONE";

    /** Search uses bounding box as spatial criteria */
    public static final String BBOX = "BBOX";

    /** Search uses a selected (GML) geometry as spatial criteria */
    public static final String SELECTED_GEOM = "SELECTED_GEOM";

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

    /**
     * Whether the dialog has enough info to produce a search or it makes sense
     * to carry on. For example, when the user closed (cancelled) the dialog.
     */
    private boolean canSearch = false;

    private JTextArea requestTextArea;

    private JTextArea responseTextArea;

    private JComboBox serverCombo;

    private JButton okButton;

    private JButton cancelButton;

    private JTabbedPane tabs;

    private JComboBox featureTypeCombo;

    private JPanel mainPanel;

    private Box box;

    private JButton extrasButton;

    // TODO remove dependency on JUMP/JTS use deegree Envelope
    /** The envelope of the current bounding box */
    private Envelope envelope = new Envelope( -1d, -1d, 1d, 1d );

    //private GMLGeometry gmlBbox;
    private Geometry selectedGeom;

    private String srs = "EPSG:4326";

    private PropertySelectionPanel propertiesPanel;
    
    private JButton capabilitiesButton;

    protected String wfsVersion;
    

    /**
     * Creates a dialog from an owner, with a title and a WFS server address.
     * 
     * @param owner
     *            the parent window
     * @param title
     *            the name to appear on the window bar
     * @param wfsServer
     *            the address of the server. This is something like
     *            http://my.domain.com/deegreewfs/wfs
     * @throws java.awt.HeadlessException
     */
    public WFSDialog( Frame owner, String title, List<String> urlList ) throws Exception {

        super( owner, title, true );
        setLocation( 0, 50 );
        //        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent we ) {
                canSearch = false;
                dispose();
            }
        } );

        setWFSList( urlList );
        initGUI();

    }

    public WFSDialog( ArrayList urls ) {
        setWFSList( urls );
    }

    /** Initializes the FeatureType combo box of the AttributeResearchPanel */

    private void refreshGUIs() {

        String[] featTypes = null;

        try {
            featTypes = wfService.getFeatureTypes();
            featureTypeCombo.setModel( new javax.swing.DefaultComboBoxModel( featTypes ) );

            okButton.setEnabled( true );
            capabilitiesButton.setEnabled( true );
            tabs.setEnabledAt( 1, true );

            featureTypeCombo.setEnabled( true );
            featureTypeCombo.setVisible( true );
            attributeResPanel.setFeatureTypeComboEnabled( true );

        } catch ( Exception e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog( this, "Could not connect to WFS server at '"
                                                 + wfService.getBaseWfsURL() + "'\n" + e.getMessage(),
                                           "Error", JOptionPane.ERROR_MESSAGE );

            featureTypeCombo.setModel( new javax.swing.DefaultComboBoxModel( new String[] {} ) );
            attributeResPanel.setFeatureTypeComboEnabled( false );
            tabs.setEnabledAt( 1, false );
            setCanSearch( false );
            capabilitiesButton.setEnabled( false );

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

    /** Initialize main GUI and its children */
    private void initGUI() {

        setSize( 450, 300 );
        setResizable( true );

        // convenience panel
        mainPanel = new JPanel();
        setContentPane( mainPanel );
        LayoutManager lm = new BoxLayout( mainPanel, BoxLayout.Y_AXIS );
        mainPanel.setLayout( lm );

        //combo box for WFS URLs
        serverCombo = createServerCombo();
        serverCombo.setPreferredSize( new Dimension( 260, 21 ) );
        String txt = Messages.getString( "FeatureResearchDialog.wfsService" );
        serverCombo.setBorder( BorderFactory.createTitledBorder( txt ) ); 
        txt = Messages.getString( "FeatureResearchDialog.wfsServiceToolTip" );
        serverCombo.setToolTipText( txt );
        
        mainPanel.add(serverCombo);
        
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
                createXMLFrame( WFSDialog.this, wfService.getCapabilitesAsString());
            }
        } );
        
        JPanel p = new JPanel();
        p.setLayout( new BoxLayout(p, BoxLayout.Y_AXIS ) );
        p.setPreferredSize( this.getSize() );
        
        // version buttons
        p.add( createVersionButtons( new String[]{ "1.0.0", "1.1.0" } ) );
        
        
        JPanel innerPanel = new JPanel();
        innerPanel.add( connecButton );
        innerPanel.add( capabilitiesButton );
        p.add( innerPanel );
        
        featureTypeCombo = createFeatureTypeCombo();
        featureTypeCombo.setVisible( false );
        p.add( featureTypeCombo );

        final Dimension dim = new Dimension( 400, 670 );
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
        okButton = new JButton( Messages.getString( "OK" ) );
        //okButton.setAlignmentX(0.5f);
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                System.out.println("in jump modus it is set invisible....");
                //setVisible( false );
                setCanSearch( true );
                
            }
        });
        
        okButton.setEnabled( false );
        okButton.setFocusable( true );

        cancelButton = new JButton( Messages.getString( "CANCEL" ) );

        cancelButton.setAlignmentX( 0.5f );
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                setVisible( false );
                setCanSearch( false );

            }
        } );

        mainPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        mainPanel.add( p );
//        mainPanel.add( Box.createVerticalStrut( 10 ) );

        //TODO externalize
        final String showAdvanced = "Advanced";
        final String hideAdvanced = "Hide Advanced Settings";

        extrasButton = new JButton( showAdvanced );
        extrasButton.setBounds( 260, 20, 80, 20 );
        extrasButton.setAlignmentX( 0.5f );
        extrasButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                String actComm = e.getActionCommand();
                JButton b = (JButton) e.getSource();
                if ( showAdvanced.equals( actComm ) ) {
                    mainPanel.remove( box );
                    //hmm, size is hard coded :-(
                    setSize( 450, 800 );
                    //pack(); //is not looking very nice
                    mainPanel.add( tabs );
                    mainPanel.add( box );
                    b.setText( hideAdvanced );
                    b.setActionCommand( hideAdvanced );
                } else {
                    mainPanel.remove( box );
                    mainPanel.remove( tabs );
                    setSize( 450, 300 );
                    //pack();
                    mainPanel.add( box );
                    b.setText( showAdvanced );
                    b.setActionCommand( showAdvanced );

                }
            }
        } );

        box.add( extrasButton );
        box.add( new JLabel( "         " ) );//Box.createHorizontalStrut(20));
        box.add( okButton );
        box.add( new JLabel( "         " ) );//Box.createHorizontalStrut(20));
        box.add( cancelButton );
        mainPanel.add( Box.createVerticalStrut( 50 ) );
        mainPanel.add( box );
    }

    private Component createVersionButtons( String[] versions ) {
        JPanel p = new JPanel();
        
        p.add( new JLabel( Messages.getString( "FeatureResearchDialog.version" ) ) );
        
        
        ButtonGroup bg = new ButtonGroup();
        for ( int i = 0; i < versions.length; i++ ) {
            final JRadioButton b = new JRadioButton( versions[i] );
            b.addActionListener( new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    WFSDialog.this.wfsVersion = b.getText(); 
                }
            } );
            bg.add( b );
            if( i == versions.length - 1 ){//last but not least is clicked 
                b.doClick();
            }
            p.add( b );
        }
        return p;
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

    private void reinitService( String url ) {
        try {
            wfService = "1.1.0".equals( this.wfsVersion ) ? 
                            new WFServiceWrapper_1_1_0( url ) :
                            new WFServiceWrapper_1_0_0( url );    
            refreshGUIs();
            String ft = (String) featureTypeCombo.getSelectedItem();
        } catch ( Exception e ) {

            JOptionPane.showMessageDialog( this, "Could not connect to WFS server at '" + url
                                                 + "'\n" + e.getMessage(), "Error",
                                           JOptionPane.ERROR_MESSAGE );

            e.printStackTrace();

        }

    }

    //GH 29.11.05
    public void setWFSList( List serverURLS ) {
        servers = serverURLS;
    }

    public List getWfsList() {
        return servers;
    }

    private JComboBox createFeatureTypeCombo() {
        String[] start = { "            " };
        JComboBox tmpFeatureTypeCombo = new JComboBox( start );
        Border border = 
            BorderFactory
                .createTitledBorder( Messages.getString( "FeatureResearchDialog.featureType" ) );
        
        tmpFeatureTypeCombo.setBorder( border );
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
                    JOptionPane.showMessageDialog( WFSDialog.this,
                                                   "Error loading schema: " + e.getMessage() );
                }
            }
        } );

        return tmpFeatureTypeCombo;
    }

    public String getRequestString(){
        return this.requestTextArea.getText();
    }
    
    public String getResponseString(){
        return this.responseTextArea.getText();
    }
    
    /** Creates a GetFeature request by concatenation of xlm elements */
    private StringBuffer createRequest() {

        StringBuffer sb = new StringBuffer( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" );

        final String outputFormat = "GML2";
        //"text/xml; subtype=gml/3.1.1
        
        System.out.println("output format is hard-coded...");
        System.out.println("FT namespace is missing in the xml");
        
        sb.append( "<wfs:GetFeature xmlns:ogc=\"http://www.opengis.net/ogc\" " )
            .append("xmlns:gml=\"http://www.opengis.net/gml\" " )
            .append("xmlns:wfs=\"http://www.opengis.net/wfs\" " )
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
            sb.append( WFSDialog.createStartStopTags( "And" )[0] );
        }

        if ( BBOX.equals( spatCrit ) ) {
            sb.append( createBboxGml() );
        } else if ( SELECTED_GEOM.equals( spatCrit ) ) {
            sb.append( spatialResPanel.getXmlElement() );
        }

        sb.append( attributeResPanel.getXmlElement() );
        if ( includesSpatialClause && listSize > 0 ) {
            sb.append( WFSDialog.createStartStopTags( "And" )[1] );
        }

        sb.append( filterTags[1] );
        sb.append( "</wfs:Query></wfs:GetFeature>" );

        return sb;
    }

    private void setRequestText( String text ) {
        requestTextArea.setText( text.replaceAll( ">", ">\n" ) );

    }

    /**
     * Returns the complete GetFeature request as XML
     */
    public String getWfsRequest() {
        String t = createRequest().toString();
        setRequestText( t );
        return t;
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

    /**
     * Returns the address of the WFS server
     * 
     * @return the address of the WFS server
     */
    public String getWfsServer() {
        return (String) serverCombo.getSelectedItem();
    }

    public AbstractWFSWrapper getWfService() {
        return this.wfService;
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

    public String getCurrentCrs() {
        return ((WFServiceWrapper_1_1_0) this.wfService).getCrsForFeatureType( getFeatureType() );
    }

    /**
     * Sets the GML Geometry that is used for spatial comparison operations
     * 
     * @param geom
     *            the comparison geometry. If null, the GUI will disable spatial
     *            comparison
     */
    public void setSelectedGMLGeometry( Geometry geom ) {
        //this.spatialResPanel.setGMLGeometry( geom );
        this.selectedGeom = geom;
        this.attributeResPanel.setSelGeoButtonEnabled( geom != null );
        //tabs.setEnabledAt(1, geom != null && attributeNames.length > 0);
    }

    public void setGMLGeometrySRS( CoordinateSystem cs ) {
        //FIXME is this needed?
        //		this.srs = cs;
        if ( this.selectedGeom != null ) {
            ( (GeometryImpl) this.selectedGeom ).setCoordinateSystem( cs );
        }
    }

    public String getGMLGeometrySRS() {
        return this.srs;
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

    /**
     * Sets the envelope inside of which the GetFeature request will be
     * performed.
     * 
     * @param env
     *            the envelope defining the current visible bounding box
     * 
     * FIXME this method should be removed in order to avoid unnecessary
     * dependence on JUMP (use deegree Envelope or simply some double[][]
     */
    public void setEnvelope( Envelope env ) {
        if ( env != null ) {
            this.envelope = env;
        }
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


        JButton validateReq = new JButton( "validate i18n");//
//                                        Messages.getString( "FeatureResearchDialog.createWFSRequest" ) );

        validateReq.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                try {
                    XMLFragment xf = new XMLFragment();
                    xf.load( new StringReader(requestTextArea.getText()), "dummy" );
                    GetFeature.create( null, xf.getRootElement() );
                } catch ( Exception ex ) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog( WFSDialog.this,
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
    
    /**
     * Whether it makes sense to ask for a GetFeature request. This is generally
     * true, but clicking on the Cancel or clisong the dialog will return
     * <code>false</code>, meaning that the user changed his mind and no
     * requst should be sent.
     * 
     * @return a boolean value hinting whether to carry on or not
     */
    public boolean canSearch() {
        return this.canSearch;
    }

    public void setCanSearch( boolean canSearch ) {
        this.canSearch = canSearch;
    }

    /*public void setGeoPropName(String name) {
     if (name != null) {
     this.geoPropName = name;
     }
     }*/

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

    public String[] getPropertiesNames() {
        return this.attributeNames;
    }

    public boolean isEditable() {
        return this.attributeResPanel.isEditable();
    }

    
    private static List<String> createInitialServerList( String[] serverURLs )
        throws MalformedURLException {
        List<String> servers = new ArrayList<String>();
        
        for ( int i = 0; i < serverURLs.length; i++ ) {
            URL tmpUrl = new URL( serverURLs[i] );
            if( !"http".equals( tmpUrl.getProtocol() ) ){
                throw new IllegalArgumentException("Protocol must be http: " + servers );
            }
            // create URLs to check if input string are valid URLs
            servers.add( tmpUrl.toString() );
        }
        
        return servers;
    }

    static void createXMLFrame( Component parent, String txt ) {

        JTextArea ta = new JTextArea( txt, 20, 80 );
        ta.setLineWrap( true );
        JScrollPane sp = new JScrollPane( ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        JOptionPane.showMessageDialog( parent, sp ); 
    
    }
    
    /** Sets the look and feel to be that of Windows (the default by JUMP) */
    private static void setWinLaF() {
        try {
            UIManager.setLookAndFeel( "com.sun.java.swing.plaf.windows.WindowsLookAndFeel" );
        } catch ( Exception e1 ) {
            e1.printStackTrace();
        }
    }
    
    /** Used for GUI layout testing */
    public static void main( String[] args ) {

        if ( args.length < 1 ) {
            System.out.println( "Usage TODO" );
        } else {
            
            try {
                
            //      setWinLaF();
            List<String> servers = createInitialServerList( args );
            
            JFrame jf = new JFrame();
            
            final WFSDialog rd = new WFSDialog( jf, "WFS Dialog", servers );
            /* ignore other behaviours */
            WindowListener[] wListener = rd.getWindowListeners();
            for ( int i = 0; i < wListener.length; i++ ) {
                rd.removeWindowListener( wListener[i] );
            }
            
            rd.setJMenuBar( new MiniMenu( rd ) );
            
            rd.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            rd.addWindowListener( new WindowAdapter() {
                public void windowClosing( WindowEvent we ) {
                    System.exit( 0 );
                }
            } );

            rd.okButton.addActionListener( new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                       
                    String resp  = null;
                    try {
                        resp = JUMPFeatureFactory.createResponsefromWFS( rd.getWfService().getGetFeatureURL() , rd.createRequest().toString() );
                    } catch ( DeeJUMPException e1 ) {
                        e1.printStackTrace();
                        resp = e1.getMessage();
                    }
                    rd.responseTextArea.setText( resp );
                }
            });

            rd.cancelButton.addActionListener( new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    System.exit( 0 );
                }
            });

            rd.setVisible( true );
            
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            
            
            
        }
    }

}