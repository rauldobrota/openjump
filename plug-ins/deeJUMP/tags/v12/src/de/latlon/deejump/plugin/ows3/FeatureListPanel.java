package de.latlon.deejump.plugin.ows3;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.httpclient.HttpClient;
import org.deegree.xml.XMLParsingException;
import org.deegree.xml.XMLTools;
import org.deegree_impl.tools.StringExtend;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jump.workbench.WorkbenchException;
import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;

public class FeatureListPanel extends JPanel implements WizardPanel {

    private InputChangedFirer inputChangedFirer = new InputChangedFirer();

    private Map dataMap;

    private JTextArea licenceInfoArea;

    private JTextArea capabiltiesInfoArea;

    private String nextID = LicencePanel.class.getName();

    private JComboBox featTypeComboBox;

    public FeatureListPanel() {
        try {
            initGUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add( InputChangedListener listener ) {
        inputChangedFirer.add( listener );
    }

    public void remove( InputChangedListener listener ) {
        inputChangedFirer.remove( listener );
    }

    private void initGUI() throws Exception {
        BoxLayout layout = new BoxLayout( this, BoxLayout.Y_AXIS );
        setLayout( layout );

        JTabbedPane tabs = new JTabbedPane();

        //Box firstBox = Box.createHorizontalBox();
        JPanel firstBox = new JPanel( new GridLayout(1,1));

        String[] ftypeList = {};// "os_free_poi", "os_poi" };
        featTypeComboBox = new JComboBox( ftypeList );
        featTypeComboBox.setBorder( BorderFactory.createTitledBorder( "Feature Types" ) );
        featTypeComboBox.addActionListener( new ActionListener() {

            public void actionPerformed( ActionEvent e ) {
                JComboBox comboBox = (JComboBox) e.getSource();
                try {
                    Object selection = comboBox.getSelectedItem();
                    doGetLicences( (String) selection );
                    //also make available in dataMap
                    dataMap.put( ClickThruPlugIn.FEATURE_TYPE, selection);
                    inputChangedFirer.fire();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog( FeatureListPanel.this,
                        "Error connecting to WFS\n"
                            + StringExtend.stackTraceToString( ex ), "Error",
                        JOptionPane.ERROR_MESSAGE );
                }
            }

        } );

        JPanel dummyPanel = new JPanel();
        dummyPanel.add(  Box.createVerticalStrut( 30 ) );
        dummyPanel.add( featTypeComboBox );
        firstBox.add( dummyPanel );

        licenceInfoArea = new JTextArea( 15, 5 );
        licenceInfoArea.setText( "licence info will appear here" );
        licenceInfoArea.setWrapStyleWord( false );
        licenceInfoArea.setLineWrap( true );
        
        JScrollPane pane = new JScrollPane( licenceInfoArea );
        pane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        pane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
        
        firstBox.add( pane );
        
        tabs.add( firstBox, "Features Types" );
/*
        JTextArea licenceTextArea = new JTextArea();
        licenceTextArea.setText( "licence TXT will appear here" );

        tabs.add( licenceTextArea, "Licence Text" );
*/
        capabiltiesInfoArea  = new JTextArea( "" );
        capabiltiesInfoArea.setWrapStyleWord( false );
        capabiltiesInfoArea.setLineWrap( true );
        
        pane = new JScrollPane( capabiltiesInfoArea );
        pane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        pane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
        tabs.add( pane, "Capabilities" );

        
        add( tabs );
    }

    private void doGetLicences( String featureType ) throws Exception {
        
        HttpClient httpClient = (HttpClient)dataMap.get( ClickThruPlugIn.HTTP_CLIENT );
        String methType = (String)dataMap.get( ClickThruPlugIn.HTTP_METHOD );
        String serverURL = (String)dataMap.get( ClickThruPlugIn.WFS_URL );
        String query = "version=1.1.0&service=WFS&request=GetLicenses&owsrequest=GetFeature&entities="
            + featureType;
        Document doc = ClientHelper.createDocFromRequest( httpClient, methType, serverURL, query);
        licenceInfoArea.setText( ClientHelper.prettyPrintNode( doc ) );
        extractLicenceInfo( doc );
    }

    /**
     * @param doc
     */
    private void extractLicenceInfo( Document doc ) {
        
        String licenceID = null;
        Element licenceElement = null;
        try {
            licenceElement = XMLTools.getChildByName( "License", "http://www.deegree.org/security/license", doc.getDocumentElement());
            if ( licenceElement != null ){
                licenceID = XMLTools.getAttrValue( "id", licenceElement );
                nextID = LicencePanel.class.getName();
            } else {
               nextID = null;
            }
            
        } catch (XMLParsingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        this.dataMap.put( ClickThruPlugIn.LICENCE_ID, licenceID); 
        this.dataMap.put( ClickThruPlugIn.LICENCE, licenceElement); 
        
        
    }

    public String getInstructions() {
        return "Select a FeatureType to download";
    }

    public void exitingToRight() throws IOException, WorkbenchException {
        // nothing to do, because licence info is already extracted on combo box event
    }

    public void enteredFromLeft( Map dataMap ) {

        this.dataMap = dataMap;

        Document doc = (Document) ( dataMap.get( "CAPABILITIES" ) );
        capabiltiesInfoArea.setText( ClientHelper.prettyPrintNode( doc ) );
        capabiltiesInfoArea.setCaretPosition( 0 );

        try {
            extractFeatureTypes( doc );
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private void extractFeatureTypes( Document doc ) throws Exception{

/*String ns = "xmlns:wfs=http://www.opengis.net/wfs";
        Element featTypeList = XMLTools
    		.getChildByName( "wfs:FeatureTypeList", ns, doc.getDocumentElement());
//System.out.println( featTypeList.getNodeName() );
        ElementList list = XMLTools
        	.getChildElementsByName( "wfs:FeatureType", ns, doc.getDocumentElement());
  */
        NodeList nl = doc.getElementsByTagName( "wfs:FeatureType" );
    
        String[] featureTypes = new String[ nl.getLength()  ];
        
        for (int i = 0; i < featureTypes.length; i++) {
            Element elem = (Element)nl.item( i );
            Node node = elem.getElementsByTagName( "wfs:Name" ).item( 0 );//UGLY!
            featureTypes[i] = node.getFirstChild().getNodeValue();
        }
        featTypeComboBox.setModel( new DefaultComboBoxModel( featureTypes ));
        if ( featureTypes.length > 0 ){
            this.dataMap.put( ClickThruPlugIn.FEATURE_TYPE, featureTypes[0]);
            doGetLicences( featureTypes[0] );
        }
    }

    public String getTitle() {
        return "Select the data";
    }

    public String getID() {
        return getClass().getName();
    }

    public boolean isInputValid() {
        return true;
    }

    public String getNextID() {
        return nextID;
    }
    //[UT] 10.01.2005

}