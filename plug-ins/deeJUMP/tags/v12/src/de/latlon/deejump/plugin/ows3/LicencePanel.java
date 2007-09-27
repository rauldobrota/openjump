package de.latlon.deejump.plugin.ows3;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.httpclient.HttpClient;
import org.deegree.xml.DOMPrinter;
import org.deegree_impl.tools.StringExtend;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jump.workbench.WorkbenchException;
import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;

public class LicencePanel extends JPanel implements WizardPanel {

    private InputChangedFirer inputChangedFirer = new InputChangedFirer();

    private Map dataMap;

    private boolean hasAccepted = false;
    
    private boolean hasChoosen = false;
    
    private JTextArea licenceTextArea;
    
    public LicencePanel() {
        try {
            initGUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add( InputChangedListener listener ) {
        inputChangedFirer.add(listener);
    }

    public void remove( InputChangedListener listener ) {
        inputChangedFirer.remove(listener);
    }
    
    private void initGUI() throws Exception {

        Box box = Box.createVerticalBox();

        licenceTextArea = new JTextArea( 10, 60 );
        licenceTextArea.setText( "" );
        licenceTextArea.setWrapStyleWord( true );
        licenceTextArea.setLineWrap( true );
        
        JScrollPane pane = new JScrollPane( licenceTextArea );
        pane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        pane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
        pane.setBorder( BorderFactory.createTitledBorder( "Licence Text" ) );

        box.add( pane );
        box.add( Box.createVerticalStrut( 20 ) );
        box.add( createAcceptRejectButtons());

        add( box );

    }

    public String getInstructions() {
        return "accept or reject";
    }

    public void exitingToRight() throws IOException, WorkbenchException {
        try {
            
            String licence = (String)this.dataMap.get( ClickThruPlugIn.LICENCE_ID );
            if ( hasAccepted ){
                doNegotiateTerms( licence );
            }
            // see if it works, for testing sake
//            touchGetFeature();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog( 
                LicencePanel.this, 
                "Error connection to WFS\n" + StringExtend.stackTraceToString(ex), 
                "Error", JOptionPane.ERROR_MESSAGE );
            ex.printStackTrace();
        }
    }

    public void enteredFromLeft( Map dataMap ) {
        this.dataMap = dataMap;
        Element e = (Element)this.dataMap.get( ClickThruPlugIn.LICENCE );
        if ( e != null ){ // is null if feature type has no licence
            licenceTextArea.setText( ClientHelper.prettyPrintNode( e ) );
            licenceTextArea.setCaretPosition( 0 );
        }
        
    }

    public String getTitle() {
        return "Agree with terms";
    }

    public String getID() {
        return getClass().getName();
    }

    public boolean isInputValid() {
        return hasChoosen;
    }

    public String getNextID() {
        return null;
    }

    private void doNegotiateTerms( String licenceID ) throws Exception {

        HttpClient httpClient = (HttpClient)dataMap.get( ClickThruPlugIn.HTTP_CLIENT );
        String methType = (String)dataMap.get( ClickThruPlugIn.HTTP_METHOD );
        String serverURL = (String)dataMap.get( ClickThruPlugIn.WFS_URL );
        String query = "version=1.1.0&service=WFS&request=NegotiateTerms&licenseIDs=" + licenceID;
        Document doc = ClientHelper.createDocFromRequest( httpClient, methType, serverURL, query);
        //DOMPrinter.printNode( doc, " ");        
    }

    
    private void touchGetFeature() throws Exception{

        String featureType = (String) dataMap.get( ClickThruPlugIn.FEATURE_TYPE );
        
        HttpClient httpClient = (HttpClient)dataMap.get( ClickThruPlugIn.HTTP_CLIENT );
        String methType = (String)dataMap.get( ClickThruPlugIn.HTTP_METHOD );
        String serverURL = (String)dataMap.get( ClickThruPlugIn.WFS_URL );
        String request = "request=GetFeature&version=1.1.0&service=WFS&typename="
            + featureType;
        Document doc = ClientHelper.createDocFromRequest( httpClient, methType, serverURL, request);
        
        DOMPrinter.printNode( doc, " ");
    }
    
    private Component createAcceptRejectButtons(  ) {
        //	      JPanel p = new JPanel();
        Box p = Box.createVerticalBox();

        final String[] text = new String[] { "Accept", "Reject" };
        
        ActionListener al = new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                // if user clicked, no matter whether accept or reject, he may continue
                hasChoosen = true;
                JRadioButton jb = (JRadioButton) e.getSource();
                hasAccepted = jb.getText().equals( text[0] );
                inputChangedFirer.fire();
            }
        };

        ButtonGroup group = new ButtonGroup();
        JRadioButton[] buttons = new JRadioButton[text.length];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JRadioButton( text[i] );
            buttons[i].addActionListener( al );
            group.add( buttons[i] );
            p.add( buttons[i] );
        }

        return p;
    }
}