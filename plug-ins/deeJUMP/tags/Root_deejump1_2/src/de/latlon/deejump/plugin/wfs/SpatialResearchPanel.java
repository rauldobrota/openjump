/*----------------    FILE HEADER  ------------------------------------------

Copyright (C) 2001-2005 by:
lat/lon GmbH
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
lat/lon GmbH
Aennchenstraße 19
53177 Bonn
Germany


 ---------------------------------------------------------------------------*/

package de.latlon.deejump.plugin.wfs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.deegree.gml.GMLGeometry;
import org.deegree_impl.services.wfs.filterencoding.OperationDefines;
import org.deegree_impl.services.wfs.filterencoding.PropertyName;
import org.deegree_impl.services.wfs.filterencoding.SpatialOperation;

import de.latlon.deejump.ui.Messages;

/**
 * This panel provides a user interface to spatial filter operations.<p/> 
 * Original design: Poth 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
class SpatialResearchPanel extends JPanel {
    
    /**Operation definitons (in Deutsch ;-). These are used in the tool tips */
    public static final String[] OP_DEFINITONS = new String[]{
            "Intersects (Vergleichgeometrie berührt/schneidet überprüfte Geometrie, " +
            "ist in dieser enthalten oder enthält sie)",
            "Within (in Vergleichgeometrie enthalten)",
            "DWithin (Vergleichgeometrie enthält überprüfte Geometrie mit einem " +
            "definiertem Abstand zum Rand [Buffer] der Geometrie)",
            "Contains (Vergleichgeometrie enthält überprüfte Geometrie)",
            "Beyond (Vergleichgeometrie befindet sich im angegebenen Abstand zur " +
            "überprüften Geometrie)",
            "Touches (Vergleichgeometrie berührt überprüfte Geometrie)", 
            "Crosses (Vergleichgeometrie schneidet überprüfte Geometrie)",
            "Overlaps (Vergleichgeometrie überlagert überprüfte Geometrie)",
            "Equals (Vergleichgeometrie ist identisch mit überprüfter Geometrie)",
            "Disjoint (Vergleichgeometrie ist verschieden von überprüfter Geometrie)",
    };

    /** Operation translations. Short form of the above, directly used in the buttons*/
    public static final String[] OP_TRANSLATIONS = new String[]{
            "(berührt/schneidet/ist enthalten)",
            "(ist enthalten)",
            "(ist enthalten innerhalb Abstands)",
            "(ist enthalten)",
            "(im angegebenen Abstand)",
            "(berührt)", 
            "(schneidet)",
            "(überlagert)",
            "(ist identisch)",
            "(ist verschieden)",
    };
                               
    /**Operation names as defined by the OGC*/
    public static final String[] OP_NAMES = new String[]{
            "Intersects",
            "Within",
            "DWithin",
            "Contains",
            "Beyond",
            "Touches", 
            "Crosses",
            "Overlaps",
            "Equals",
            "Disjoint",
    };
    
    
    /**A Distance input filed for the DWithin operation*/
    private DistanceInputField dWithinDistanceField = new DistanceInputField("0");

    /**A Distance input filed for the Beyond operation*/
    private DistanceInputField beyondDistanceField = new DistanceInputField("0");
    
    /**The currently selected operation*/
    private String selectedOperation = "Intersects";
        
    /**The parent dialog. Keep this reference to make matters simple */
    private FeatureResearchDialog researchDialog;

    private JComboBox geomPropsCombo;
    
    
    /** Create a SpatialResearchPanel.
     * 
     * @param rd the parent FeatureResearchDialog
     */
    public SpatialResearchPanel(FeatureResearchDialog rd) {
        super();
        this.researchDialog = rd;
        initGUI();
    }
    
    /**Initialize the GUI*/
    private void initGUI(){
        
        //setLayout( null );
        LayoutManager lm = new BoxLayout( this, BoxLayout.Y_AXIS); 
        setLayout( lm );
        
        add( createGeomPropCombo() );
        add( createSRSCombo() );
        
        add( createOperationButtons() );
    }

    private JComponent createGeomPropCombo(){
        
        String[] gg = new String[]{
                "GEOM",
                "Geometry"};
        
        geomPropsCombo = new JComboBox( gg );
        
        geomPropsCombo.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    String newName = (String)e.getItem();
                    
                    researchDialog.setGeoPropName( newName );
                                        
                    // create a new entry 
                    if ( e.getStateChange() == ItemEvent.SELECTED ){
                        if ( !"".equals( newName )){
                            addGeoPropName( newName );
                        }
                    }
                }
            }
        );

        geomPropsCombo.setEditable( true );
        
        JPanel p = new JPanel();
        p.add( new JLabel(Messages.getString("SpatialResearchPanel.geometryName") ) );
        p.add( geomPropsCombo );
        add( p );
        return p;
    }
        
    private JComponent createSRSCombo(){
        //TODO crete a resources file for this
        String[] srs = new String[]{
                                    "4326",
                                    "31466",
                                    "31467",
                                    "25833"
/*"EPSG:4326",
                "EPSG:31466",
				"EPSG:31467",
                "EPSG:25833"*/
        		};
        
        final JComboBox srsCombo = new JComboBox( srs );
        
        srsCombo.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    String newSRS = (String)e.getItem();
                    
                    researchDialog.setGMLGeometrySRS( newSRS );
//                  FIXME this is not qut nice here, but JUMP is lacking 
                    //a mechanism for srs, anyway
                    String srs = (String)srsCombo.getSelectedItem();
                    
                    TransactionFactory.setSrs( srs );

                    // create a new entry 
                    if ( e.getStateChange() == ItemEvent.SELECTED ){
                        if ( !"".equals( newSRS )){
                            srsCombo.addItem( newSRS );
                        }
                    }
                }
            }
        );

        srsCombo.setEditable( true );
        
        JPanel p = new JPanel();
        p.add( new JLabel( Messages.getString("SpatialResearchPanel.srs")) );
        p.add( srsCombo );
        add( p );
        return p;
    }
        
    /**Creates a panel containing the radio buttons representaing the spatial operations*/
    private JComponent createOperationButtons() {
        JPanel b = new JPanel();
        LayoutManager lm = new BoxLayout( this, BoxLayout.Y_AXIS); 
        setLayout( lm );
        
        JPanel opsPanel = new JPanel();
        JPanel opsFieldPanel = new JPanel();
        Border bo = BorderFactory.createEmptyBorder(10,10,10,10);
        opsPanel.setBorder( bo );
        opsFieldPanel.setBorder( bo );
        
        LayoutManager lm2 = new GridLayout( OP_NAMES.length, 1);
        opsPanel.setLayout( lm2 );
        opsFieldPanel.setLayout( lm2 );
        
        ActionListener bal = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton rb = (JRadioButton)e.getSource();
                selectedOperation = rb.getActionCommand();
            }
        };

        ButtonGroup bg = new ButtonGroup();
        
                
        JRadioButton[] opButtons = new JRadioButton[ OP_NAMES.length ]; 
        for (int i = 0; i < OP_NAMES.length; i++) {
            opButtons[i] = new JRadioButton( OP_NAMES[i] );//+ " " + OP_TRANSLATIONS[i] );
            opButtons[i].setToolTipText( OP_NAMES[i] );
            opButtons[i].setActionCommand( OP_NAMES[i] );
            opButtons[i].addActionListener( bal );
            opButtons[i].setBounds( AttributeResearchPanel.LEFT_MARGIN + 10, (i*25) + 25, 270, 20);
            opButtons[i].setAlignmentX( Component.LEFT_ALIGNMENT );

            bg.add(opButtons[i]);
            
            if ( "DWithin".equals( OP_NAMES[i] ) ){
                opsFieldPanel.add( dWithinDistanceField );
            } else if ( "Beyond".equals( OP_NAMES[i] ) ){
                beyondDistanceField.setEnabled( false ); 
                opsFieldPanel.add( beyondDistanceField);
            }else{
                opsFieldPanel.add( Box.createHorizontalStrut(1) );
            }
            
            opsPanel.add( opButtons[i] );
            
            //FIXME
            /*
            if( "Touches".equals( OP_NAMES[i] ) ||
                    "Crosses".equals( OP_NAMES[i] ) ||
                    "Equals".equals( OP_NAMES[i] ) ||
                    "Overlaps".equals( OP_NAMES[i] )||
                    "Beyond".equals( OP_NAMES[i] )
                    ){
                opButtons[i].setToolTipText( "Operation '" 
                        + OP_NAMES[i] + "' is not implemented yet");  
  
                //	+ "' is too resource-consuming and has been turned off in this demo." );
                
                opButtons[i].setEnabled(false);
            }
            */
        }
        
        opButtons[0].doClick();
        JPanel combiPanel = new JPanel();
        combiPanel.setBorder(
                BorderFactory.createTitledBorder( Messages.getString("SpatialResearchPanel.spatialOperation") ));
        combiPanel.setLayout( new GridLayout( 1, 2) );
        combiPanel.add( opsPanel );
        combiPanel.add( opsFieldPanel );
        combiPanel.setPreferredSize( new Dimension(300,380));
        b.add( combiPanel );

        return b;
    }
    
    public void addGeoPropName(String name){
        
        int size = geomPropsCombo.getModel().getSize();
        List candidateGeoProps = new ArrayList( size );
        for (int i = 0; i < size; i++) {
            candidateGeoProps.add( geomPropsCombo.getModel().getElementAt( i ) );
        }
        if( name != null && !candidateGeoProps.contains( name ) ){
			geomPropsCombo.addItem(name);
        }
        geomPropsCombo.setSelectedItem( name );
    }
    
    /**Returns the XML fragment conating the spatial operation
     * 
     * @return the XML description of the spatial operation
     */
    public StringBuffer getXmlElement(){
        
        int opType = OperationDefines.getIdByName( selectedOperation );

        double dist = 0;
        if( "DWithin".equals(selectedOperation) )
            dist = dWithinDistanceField.getDistance();
        else if( "Beyond".equals(selectedOperation) )
            dist = beyondDistanceField.getDistance();
        
        StringBuffer sb = new StringBuffer();
        GMLGeometry geometry = researchDialog.getSelectedGMLGeometry();
        if (geometry == null){
            return sb;
        }
        SpatialOperation spatialOp = 
            new SpatialOperation( opType, 
                    new PropertyName( FeatureResearchDialog.GEOMETRY_PROPERTY_NAME), 
                    geometry, dist);
        
        // ad-hoc stuff -> will add this to deegree core
        // but not like this ;-)
        sb = spatialOp.toXML(); 
        if( "DWithin".equals(selectedOperation) || "Beyond".equals(selectedOperation) ){
            String d = "<ogc:Distance unit='http://www.uomdict.com/uom.html#meters'>" + 
            	dist + "</ogc:Distance>";
            String insert = "</ogc:" + selectedOperation + ">";
            int i = sb.indexOf(insert);            
            sb.insert( i, d);
        }

        return sb;        	
    }
    
    /**A conveniece class containing a text field for value input and a label. This
     * class is used for the operations DWithin and Beyond.*/
    class DistanceInputField extends JPanel{
        
        private JFormattedTextField distanceField;
        private double distance = 0d;
        
        DistanceInputField(String text){
            super();
            distanceField = 
                    new JFormattedTextField( new Float( 0.0 ) );
            distanceField.addPropertyChangeListener("value", new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent evt) {
                    distance = ((Number)distanceField.getValue()).doubleValue();
                }
            });
                        
            distanceField.setColumns(5);
            add(distanceField);
            add( new JLabel("m"));
          
            
        }               
        
        public double getDistance(){ 
            return distance;
        }

        public void setEnabled( boolean enabled){
            distanceField.setEnabled(enabled);
        }
        
      
    }
    
}
