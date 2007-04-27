/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */

package de.latlon.deejump.plugin.wfs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * My minimalistic version of a Menu. It only has menu items for saving
 * a request, a response and exiting the application.  
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class MiniMenu extends JMenuBar {
    
    private String lastDirectory; 
    
    public MiniMenu( final WFSPanel wfsPanel ) {
        super();
        
        JMenu topMenu = new JMenu("File");
        
        JMenuItem subMenu = new JMenuItem( "Save Request" );
        topMenu.add(  subMenu  );
        subMenu.addActionListener( new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                try {
                    saveTextToFile( wfsPanel.getRequest() );
                } catch ( IOException e1 ) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog( MiniMenu.this, 
                                                   e1.getMessage(), 
                                                   "Error",
                                                   JOptionPane.ERROR_MESSAGE );
                }
            }
        } );
        
        subMenu = new JMenuItem( "Save Response" );
        topMenu.add(  subMenu  );
        subMenu.addActionListener( new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                try {
                    saveTextToFile( wfsPanel.getResponse() );
                } catch ( IOException e1 ) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog( MiniMenu.this, 
                                                   e1.getMessage(), 
                                                   "Error",
                                                   JOptionPane.ERROR_MESSAGE );
                }
            }
        } );
        
        
        topMenu.addSeparator();
        
        subMenu = new JMenuItem( "Exit" );
        subMenu.addActionListener( new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
               System.exit( 0 ); 
            }
        } );
        topMenu.add(  subMenu  );
        add( topMenu );
        
        
    }
    
    private void saveTextToFile( String txt ) throws IOException{
        
        JFileChooser jfc = new JFileChooser();
        int i = jfc.showSaveDialog( this );
        if ( i == JFileChooser.APPROVE_OPTION ){
            
            FileWriter fw = new FileWriter( jfc.getSelectedFile() );
            fw.write( txt );
            fw.close();
            lastDirectory = jfc.getSelectedFile().getParent();
        } 
        
    }
    
    
}

/* ********************************************************************
Changes to this class. What the people have been up to:

$Log$
Revision 1.2  2007/04/27 07:27:29  taddei
More major refactoring to use WFSPanel the WFSFrame) and in the  WFSDialog. Also some GUI improvements.

Revision 1.1  2007/04/26 09:19:26  taddei
Added initial working version of classes and complementary files.

********************************************************************** */