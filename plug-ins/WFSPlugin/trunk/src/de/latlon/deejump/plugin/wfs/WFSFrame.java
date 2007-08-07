/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.latlon.deejump.plugin.wfs;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.vividsolutions.jump.workbench.WorkbenchContext;

import de.latlon.deejump.ui.DeeJUMPException;
import de.latlon.deejump.ui.Messages;
import de.latlon.deejump.util.data.WFSClientHelper;

/**
 * <code>WFSFrame</code> is the dialog window.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class WFSFrame extends JFrame {

    private static final long serialVersionUID = -4892892282926150006L;

    protected WFSPanel wfsPanel;

    protected Thread loaderThread;

    private WorkbenchContext context;
    
    /**
     * @param context 
     * @param wfsURLs an array with server addresses
     */
    public WFSFrame( WorkbenchContext context, String[] wfsURLs ) {
        super( "WFSFrame v. " + WFSPanel.releaseVersion );
        this.context = context;
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        initGUI( wfsURLs );
        setDefaultLookAndFeelDecorated( true );
        
        setSize( 500, 300 );
    }

    private void initGUI(String[] wfsURLs) {
        getContentPane().setLayout( new FlowLayout() );
        
        this.wfsPanel = new WFSPanel( context, Arrays.asList( wfsURLs ) );
        
        add( this.wfsPanel );
        
        WFSPanelButtons buttons = new WFSPanelButtons( this, this.wfsPanel );
        this.wfsPanel.controlButtons = buttons;
        buttons.okButton.setAction( new GetFeatureAction() );
        
        buttons.okButton.setText( Messages.getString( "WFSPanel.doGetFeature" ) );
        buttons.okButton.setEnabled( false );
        
        buttons.cancelButton.setText( Messages.getString( "WFSPanel.exit" ) );
        buttons.cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                int i = JOptionPane
                            .showConfirmDialog( WFSFrame.this, 
                                                Messages.getString( "WFSPanel.exitQuestion" ), 
                                                Messages.getString( "WFSPanel.exitQuestion2" ), 
                                               JOptionPane.OK_CANCEL_OPTION, 
                                               JOptionPane.QUESTION_MESSAGE);
                if( i == JOptionPane.OK_OPTION ){
                    System.exit( 0 );
                }
            }
        });   
        add( buttons );
    }

    protected void load(){
        final String resp;
        String tmp = null;
        try {
            tmp = 
                WFSClientHelper.createResponsefromWFS( wfsPanel.getWfService().getGetFeatureURL() , 
                                                       wfsPanel.getRequest() );
        } catch ( DeeJUMPException e1 ) {
            e1.printStackTrace();
            tmp = e1.getMessage();
        }
        resp = tmp;
        int arbitrarySize = 10000;
        if( resp.length() < arbitrarySize ){
            
            Thread worker = new Thread(){
                @Override
                public void run() {
                  wfsPanel.getTabs().setSelectedIndex( 4 );
                  wfsPanel.setResposeText( resp );  
                }
            };
            SwingUtilities.invokeLater( worker );
//            wfsPanel.getTabs().setSelectedIndex( 4 );
//            wfsPanel.setResposeText( resp );  
        } else {
            int i = JOptionPane.showConfirmDialog( WFSFrame.this, Messages.getString( "WFSPanel.responseTooLarge" ), "",
                                           JOptionPane.YES_NO_OPTION);
            if( i == JOptionPane.YES_OPTION ){
                WFSPanel.saveTextToFile( WFSFrame.this, resp );
            }
        }
    }
    
    class GetFeatureAction extends AbstractAction implements Runnable {
        private static final long serialVersionUID = 8707156676984986438L;
        public void actionPerformed( ActionEvent e ) {
            if( loaderThread != null ){
                return;
            }
            loaderThread = new Thread( this );
            loaderThread.start();
        }
        public void run() {
            load();
            loaderThread = null;
        }
    }
    
}
