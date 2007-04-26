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

import de.latlon.deejump.ui.DeeJUMPException;
import de.latlon.deejump.ui.Messages;
import de.latlon.deejump.util.data.WFSClientHelper;

public class WFSFrame extends JFrame {

    private WFSPanel wfsPanel;

    public WFSFrame( String[] wfsURLs ) {
        super( "WFS" );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        initGUI( wfsURLs );
        setDefaultLookAndFeelDecorated( true );
        
        setSize( 400, 300 );
    }

    private void initGUI(String[] wfsURLs) {
        getContentPane().setLayout( new FlowLayout() );
        this.wfsPanel = new WFSPanel( Arrays.asList( wfsURLs ) );
        
        
        add( this.wfsPanel );
        
        add( createButtonsPanel() );
    }

    private Component createButtonsPanel() {
        
        Box box = Box.createHorizontalBox();
        box.setBorder( BorderFactory.createEmptyBorder( 20, 5, 10, 5 ));
        
        JButton okButton = new JButton( Messages.getString( "OK" ) );
        okButton.addActionListener( new ActionListener(){
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

        okButton.setEnabled( true );
        okButton.setFocusable( true );

        JButton cancelButton = new JButton( Messages.getString( "CANCEL" ) );

        cancelButton.setAlignmentX( 0.5f );
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                System.out.println("setVisible( false ); only makes sense in a dialog");
                //setVisible( false );
                
                System.out.println("setCanSearch( true ); only makes sense in a dialog");
                //setCanSearch( true );
                //setCanSearch( false );

            }
        } );

        //TODO externalize
        final String showAdvanced = "Advanced";
        final String hideAdvanced = "Hide Advanced Settings";

        JButton extrasButton = new JButton( showAdvanced );
        extrasButton.setBounds( 260, 20, 80, 20 );
        extrasButton.setAlignmentX( 0.5f );
        extrasButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                String actComm = e.getActionCommand();
                JButton b = (JButton) e.getSource();
                if ( showAdvanced.equals( actComm ) ) {
                    wfsPanel.setTabsVisible( true );
//                    remove( box );
                    //hmm, size is hard coded :-(
                    setSize( 450, 900 );
                    //pack(); //is not looking very nice
//                    add( tabs );
//                    add( box );
                    b.setText( hideAdvanced );
                    b.setActionCommand( hideAdvanced );
                } else {
//                    remove( box );
//                    remove( tabs );
                    wfsPanel.setTabsVisible( false );
                    setSize( 450, 300 );
                    //pack();
//                    add( box );
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
//        add( Box.createVerticalStrut( 50 ) );
        add( box );
        
        return box;
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
