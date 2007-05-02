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
import java.awt.FlowLayout;
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
import java.util.Arrays;
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
import de.latlon.deejump.util.data.WFSClientHelper;

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
    
    /**
     * Whether the dialog has enough info to produce a search or it makes sense
     * to carry on. For example, when the user closed (cancelled) the dialog.
     */
    private boolean canSearch = false;

    private WFSPanel wfsPanel;
    
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
    public WFSDialog( Frame owner, String title, String[] urls ) throws Exception {

        super( owner, title, true );
        setTitle( "WFSPlugin v. " + WFSPanel.releaseVersion );
        setLocation( 0, 50 );
        //        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent we ) {
                canSearch = false;
                dispose();
            }
        } );

        initGUI(urls);

    }


    /** Initialize main GUI and its children */
    private void initGUI(String[] wfsURLs) {
        
        getContentPane().setLayout( new FlowLayout() );
        
//      setJMenuBar( new MiniMenu(  ) );
      
        this.wfsPanel = new WFSPanel( Arrays.asList( wfsURLs ) );
  
        add( this.wfsPanel );
  
        WFSPanelButtons buttons = new WFSPanelButtons( this, this.wfsPanel );
        this.wfsPanel.controlButtons = buttons;
        buttons.okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                setVisible( false );
                setCanSearch( true );

            }
        } );
        buttons.okButton.setEnabled( false );
        
        buttons.cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                setVisible( false );
                setCanSearch( false );

            }
        } );
        add( buttons );

        setSize( 450, 300 );
        setResizable( true );
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

    /** Used for GUI layout testing */
    public static void main( String[] args ) {

        if ( args.length < 1 ) {
            System.out.println( "Usage TODO" );
        } else {
            
            try {
                
            JFrame jf = new JFrame();
            
            final WFSDialog rd = new WFSDialog( jf, "WFS Dialog", args );
            rd.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            rd.addWindowListener( new WindowAdapter() {
                public void windowClosing( WindowEvent we ) {
                    System.exit( 0 );
                }
            } );

            rd.setVisible( true );
            
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }


    public WFSPanel getWFSPanel() {
        return this.wfsPanel;
    }

}