/*----------------    FILE HEADER  ------------------------------------------

This file is part of deegree.
Copyright (C) 2001 by:
EXSE, Department of Geography, University of Bonn
http://www.giub.uni-bonn.de/exse/
lat/lon Fitzke/Fretter/Poth GbR
http://www.lat-lon.de

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Contact:

Andreas Poth
lat/lon Fitzke/Fretter/Poth GbR
Meckenheimer Allee 176
53115 Bonn
Germany
E-Mail: poth@lat-lon.de

Jens Fitzke
Department of Geography
University of Bonn
Meckenheimer Allee 166
53115 Bonn
Germany
E-Mail: jens.fitzke@uni-bonn.de

                 
 ---------------------------------------------------------------------------*/
package de.latlon.deejump.ui;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;

import de.latlon.deejump.plugin.wfs.UpdateWFSLayerPlugIn;
import de.latlon.deejump.util.data.JUMPFeatureFactory;


/**
 * ...
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class LayerViewOptionsPanel extends JPanel implements OptionsPanel {

    public static final Envelope startUpEnvelope;
    
    static {
        //TODO init from props file
        startUpEnvelope = null;
    }
    
    /*
ZoomToLKEEPlugIn.minX=3363400
ZoomToLKEEPlugIn.minY=5685300
ZoomToLKEEPlugIn.maxX=3447600
ZoomToLKEEPlugIn.maxY=5754900    
    */
    
    private JTextField minXTxtField = 
        new JTextField( "0.3" );
    
    
    private JTextField minYTxtField = 
        new JTextField( "0.4" );

    private JTextField maxXTxtField = 
        new JTextField( "0.6" );
    
    
    private JTextField maxYTxtField = 
        new JTextField( "0.7" );

    private JCheckBox reloadLayerCheckBox = new JCheckBox( "Layer nach Update neu laden" );
    
    private Blackboard blackboard;
    
    public LayerViewOptionsPanel( Blackboard blackboard ) {
        super();
        this.blackboard = blackboard;
        initGUI();       
    }

    
    /**
     * 
     */
    private void initGUI() {
        
        Box b = Box.createVerticalBox();
        JPanel p = new JPanel();

        p.add( new JLabel( "Min X:" ) );
        p.add( minXTxtField );
        p.add( new JLabel( "Min Y:" ) );
        p.add( minYTxtField );
        p.add( new JLabel( "Max X:" ) );
        p.add( maxXTxtField );
        p.add( new JLabel( "Max Y:" ) );
        p.add( maxYTxtField );
        
        b.add(p);
        
        
        add( b );
    }


    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.OptionsPanel#validateInput()
     */
    public String validateInput() {
        String errorMessage = "\"" + minXTxtField.getText() +
        "\" ist keine gültige Angabe";

    try {
        if (Integer.parseInt(minXTxtField.getText()) <= 0) {
            return errorMessage;
        }
    } catch (NumberFormatException e) {
        return errorMessage;
    }

    return null;
    }

    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.OptionsPanel#okPressed()
     */
    public void okPressed() {
        JUMPFeatureFactory.setMaxFeatures( Integer.parseInt(minXTxtField.getText()) );
        blackboard.put( UpdateWFSLayerPlugIn.RELOAD_LAYER_KEY, reloadLayerCheckBox.isSelected() );
        
        
        
    }

    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.OptionsPanel#init()
     */
    public void init() {
    }

}
