/*
 * Created on 15.11.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.latlon.deejump.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

/**
 * @author hamammi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExtensibleComboBox extends JComboBox {

	public ExtensibleComboBox( Object[] objects ){
		super(objects);
		createServerComboBox();
	}
	
	public JComboBox createServerComboBox(){	        
	        addItemListener( new ItemListener() {
	                public void itemStateChanged(ItemEvent e) {
	                    if ( e.getStateChange() == ItemEvent.SELECTED ){
	                    	JComboBox box = (JComboBox)e.getSource();
	                        String newServer = (String)e.getItem();	                        
	                        int size = box.getModel().getSize();
	                        List candidateGeoProps = new ArrayList( size );
	                        for (int i = 0; i < size; i++) {
	                            candidateGeoProps.add( box.getModel().getElementAt( i ) );
	                        }
	                        if( newServer != null && !candidateGeoProps.contains( newServer ) ){
	                        	box.addItem(newServer);
	                        }
	                    }
	                }
	            }
	        );
	        setEditable(true);
	        return this;
	}
}
