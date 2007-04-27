/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.latlon.deejump.plugin.wfs;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import de.latlon.deejump.ui.DeeJUMPException;
import de.latlon.deejump.ui.Messages;
import de.latlon.deejump.util.data.WFSClientHelper;

public class WFSFrame extends JFrame {

    private WFSPanel wfsPanel;

    public WFSFrame( String[] wfsURLs ) {
        super( "WFSFrame v. " + WFSPanel.releaseVersion );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        initGUI( wfsURLs );
        setDefaultLookAndFeelDecorated( true );
        
        setSize( 500, 300 );
    }

    private void initGUI(String[] wfsURLs) {
        getContentPane().setLayout( new FlowLayout() );
        
//        setJMenuBar( new MiniMenu(  ) );
        
        this.wfsPanel = new WFSPanel( Arrays.asList( wfsURLs ) );
        
        add( this.wfsPanel );
        
        WFSPanelButtons buttons = new WFSPanelButtons( this, this.wfsPanel );
        this.wfsPanel.controlButtons = buttons ;
        
        buttons.okButton.addActionListener( new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                   
                String resp  = null;
                try {
                    resp = 
                        WFSClientHelper.createResponsefromWFS( wfsPanel.getWfService().getGetFeatureURL() , 
                                                               wfsPanel.getRequest() );
                } catch ( DeeJUMPException e1 ) {
                    e1.printStackTrace();
                    resp = e1.getMessage();
                }
                wfsPanel.setResposeText( resp );
            }
        });
        buttons.okButton.setText( "Do GetFeature" );
        buttons.okButton.setEnabled( false );
        
        buttons.cancelButton.setText( "Exit" );
        buttons.cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                int i = JOptionPane
                            .showConfirmDialog( WFSFrame.this, 
                                               "Really exit?", 
                                               "Exit?", 
                                               JOptionPane.OK_CANCEL_OPTION, 
                                               JOptionPane.QUESTION_MESSAGE);
                if( i == JOptionPane.OK_OPTION ){
                    System.exit( 0 );
                }
            }
        });   
        add( buttons );
    }

    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI(String[] wfsURLs) {

        WFSFrame wfsFrame = new WFSFrame( wfsURLs );
        wfsFrame.setVisible( true );
    }

    private static List<String> createInitialServerList( String[] serverURLs )
                            throws MalformedURLException {
        List<String> servers = new ArrayList<String>();

        for ( int i = 0; i < serverURLs.length; i++ ) {
            URL tmpUrl = new URL( serverURLs[i] );
            if ( !"http".equals( tmpUrl.getProtocol() ) ) {
                throw new IllegalArgumentException( "Protocol must be http: " + servers );
            }
            // create URLs to check if input string are valid URLs
            servers.add( tmpUrl.toString() );
        }

        return servers;
    }

    /**
     * @param args
     */
    public static void main( final String[] args ) {
        //      Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                createAndShowGUI( args );
            }
        } );
    }

}
