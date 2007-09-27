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
package de.latlon.deejump.plugin.wfs;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;

import de.latlon.deejump.util.data.JUMPFeatureFactory;


/**
 * ...
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class WFSOptionsPanel extends JPanel implements OptionsPanel {

    
    private JTextField maxFeaturesTxtField = 
        new JTextField( String.valueOf( JUMPFeatureFactory.getMaxFeatures()  ));
    
    private JCheckBox reloadLayerCheckBox = new JCheckBox( "Layer nach Update neu laden" );
    
    private Blackboard blackboard;
    
    public WFSOptionsPanel( Blackboard blackboard) {
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

        // don't add synchor layer check box
//        b.add( reloadLayerCheckBox );
        reloadLayerCheckBox.setSelected( true );

        maxFeaturesTxtField.setColumns( 3 );
        p.add( new JLabel( "Max Features" ) );
        p.add( maxFeaturesTxtField );
        b.add(p);
        
        
        add( b );
    }


    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.OptionsPanel#validateInput()
     */
    public String validateInput() {
        String errorMessage = "\"" + maxFeaturesTxtField.getText() +
        "\" ist keine gültige Angabe";//is not a valid grid size";

    try {
        if (Integer.parseInt(maxFeaturesTxtField.getText()) <= 0) {
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
        JUMPFeatureFactory.setMaxFeatures( Integer.parseInt(maxFeaturesTxtField.getText()) );
        blackboard.put( UpdateWFSLayerPlugIn.RELOAD_LAYER_KEY, reloadLayerCheckBox.isSelected() );
    }

    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.OptionsPanel#init()
     */
    public void init() {
    }

}
