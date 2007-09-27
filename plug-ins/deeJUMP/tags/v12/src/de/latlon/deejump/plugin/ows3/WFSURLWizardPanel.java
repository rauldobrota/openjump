package de.latlon.deejump.plugin.ows3;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.httpclient.HttpClient;
import org.deegree.xml.DOMPrinter;
import org.deegree_impl.tools.StringExtend;
import org.w3c.dom.Document;

import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;

public class WFSURLWizardPanel extends JPanel implements WizardPanel {

    private InputChangedFirer inputChangedFirer = new InputChangedFirer();

    private Map dataMap;

    private JLabel urlLabel = new JLabel();

    private JTextField urlTextField = new JTextField();

    private String demoURL_1 = "http://localhost:8081/ows3/deegreewfs";
    private String demoURL_2 = "http://wfs.lat-lon.de/deegree2/ogcwebservice";
    
    public WFSURLWizardPanel() {

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

        this.add( urlLabel );
        String[] urlList = {
                            demoURL_2,
                            demoURL_1};
        JComboBox jcb = new JComboBox( urlList );
        jcb.addActionListener( new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                JComboBox combo = (JComboBox)e.getSource();
                WFSURLWizardPanel.this.dataMap.put( ClickThruPlugIn.WFS_URL, combo.getSelectedItem());
            }
        });

        Box box = Box.createVerticalBox();

        box.add( jcb );
        box.add( Box.createVerticalStrut( 40 ) );
        box.add( createMethodButtons());

        add( box );

    }

    public String getInstructions() {
        return "Select a WFS and a method type";
    }


    public void exitingToRight() {
        
        HttpClient httpClient = (HttpClient)dataMap.get( ClickThruPlugIn.HTTP_CLIENT );
        String methType = (String)dataMap.get( ClickThruPlugIn.HTTP_METHOD );
        String serverURL = (String)dataMap.get( ClickThruPlugIn.WFS_URL );
        String query = "SERVICE=WFS&VERSION=1.1.0&REQUEST=GetCapabilities";

        Document doc = null;
        try {
            doc = ClientHelper.createDocFromRequest( httpClient,methType, serverURL, query);
            dataMap.put( ClickThruPlugIn.CAPABILITIES, doc );

        } catch (IOException e) {
            JOptionPane.showMessageDialog( this,
                "Error connecting to WFS\n"
                    + StringExtend.stackTraceToString( e ), "Error",
                JOptionPane.ERROR_MESSAGE );
        }
    }

    public void enteredFromLeft( Map dataMap ) {
        this.dataMap = dataMap;
        this.dataMap.put( ClickThruPlugIn.WFS_URL, demoURL_2);
        this.dataMap.put( ClickThruPlugIn.HTTP_METHOD, ClickThruPlugIn.HTTP_GET);
        urlTextField.setCaretPosition( 0 );
        urlTextField.moveCaretPosition( urlTextField.getText().length() );
    }

    public String getTitle() {
        return "Choose WFS";
    }

    public String getID() {
        return getClass().getName();
    }

    public boolean isInputValid() {
        return true;
    }

    public String getNextID() {
        return FeatureListPanel.class.getName();
    }

    private Component createMethodButtons() {
        //JPanel p = new JPanel();
        Box p = Box.createVerticalBox();
        p.setAlignmentX( 1f );
        String[] meths = new String[] { ClickThruPlugIn.HTTP_GET, ClickThruPlugIn.HTTP_POST };
        
        ActionListener al = new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                JRadioButton jb = (JRadioButton) e.getSource();
                dataMap.put( ClickThruPlugIn.HTTP_METHOD, jb.getActionCommand() );
            }
        };

        ButtonGroup group = new ButtonGroup();
        JRadioButton[] buttons = new JRadioButton[meths.length];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JRadioButton( meths[i] );
            buttons[i].addActionListener( al );
            buttons[i].setActionCommand( meths[i] );
            group.add( buttons[i] );
            p.add( buttons[i] );
        }
        group.setSelected( buttons[0].getModel(), true );

        return p;
    }

}