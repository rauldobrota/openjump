/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */

package de.latlon.deejump.plugin.wfs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;

import de.latlon.deejump.ui.Messages;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class RequestTextArea extends JPanel {
        
    final WFSPanel wfsPanel;
    
    private JTextArea requestTextArea;

    private JButton createReqButton;

    private JButton validateReq;
    
    RequestTextArea( WFSPanel wfsPanel ) {
        this.wfsPanel = wfsPanel;
        
        createTextArea();
        createRequestButton();
        
        //TODO i18n
       
        JPanel innerPanel = new JPanel();
        innerPanel.add( createReqButton );
        innerPanel.add( createValidationtButton() );

        add( innerPanel );
        
    }

    private JComponent createValidationtButton() {
        validateReq = new JButton( Messages.getString( "FeatureResearchDialog.validateRequest" ) );

        validateReq.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                
                String reqTxt = requestTextArea.getText();
                if( reqTxt == null || reqTxt.length() == 0 ){
                    return;
                }
                try {
                    
                    //simple test for well-formedness
                    XMLFragment xf = new XMLFragment();
                    xf.load( new StringReader( reqTxt ), "http://empty" );

                    if( "1.1.0".equals( wfsPanel.wfService.getServiceVersion() ) ){
                        // use deegree to validate request
                        GetFeature.create( null, xf.getRootElement() );
                    } 
                } catch ( Exception ex ) {
                    ex.printStackTrace();                                       
                    JOptionPane.showMessageDialog( wfsPanel, ex.getMessage(), 
                                                   Messages.getString( "error"),
                                                   JOptionPane.ERROR_MESSAGE );
                }
            }
        } );
        return validateReq;
    }

    private JComponent createRequestButton() {
        createReqButton = new JButton(
                                      Messages.getString( "FeatureResearchDialog.createWFSRequest" ) );
        createReqButton.setBounds( 260, 20, 80, 20 );
        createReqButton.setAlignmentX( 0.5f );
        createReqButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                setRequestText( wfsPanel.createRequest() );
                requestTextArea.setCaretPosition( 0 );
            }
        } );
        return createReqButton;
    }

    private void createTextArea() {
        requestTextArea = new JTextArea();
        requestTextArea.setLineWrap( true );
        requestTextArea.setWrapStyleWord( true );
        requestTextArea.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        JScrollPane jsp = new JScrollPane( requestTextArea );
        jsp.setPreferredSize( new Dimension(390,475) );
        add( jsp );

    }

    void setRequestText( String txt ){
        this.requestTextArea.setText(  txt.replaceAll( ">", ">\n" ) );
    }

    String getText() {
        return this.requestTextArea.getText();
    }
    
    public void setEnabled( boolean enabled ){
        super.setEnabled( enabled );
        this.createReqButton.setEnabled( enabled );
        this.validateReq.setEnabled( enabled );
        this.requestTextArea.setEnabled( enabled );
    }
    
}

/* ********************************************************************
Changes to this class. What the people have been up to:

$Log$
Revision 1.3  2007/05/16 14:43:33  taddei
Externalisation of i18n

Revision 1.2  2007/05/14 08:49:02  taddei
Removed println

Revision 1.1  2007/05/10 07:36:34  taddei
Added

********************************************************************** */