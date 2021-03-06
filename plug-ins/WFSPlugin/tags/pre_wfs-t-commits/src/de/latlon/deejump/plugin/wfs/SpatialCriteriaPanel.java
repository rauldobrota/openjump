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
import java.awt.Dimension;
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

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryImpl;

import de.latlon.deejump.ui.Messages;

/**
 * This panel provides a user interface to spatial filter operations.<p/> 
 * Original design: Poth 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
class SpatialCriteriaPanel extends JPanel {
    
    /**Operation names as defined by the OGC*/
    public static final String[] OPERATION_NAMES = new String[]{
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
    private WFSPanel wfsPanel;

    private JComboBox geomPropsCombo;

    private JComboBox srsCombo;
    
    private AbstractButton[] opButtons;
    
    /** Create a SpatialResearchPanel.
     * 
     * @param rd the parent FeatureResearchDialog
     */
    public SpatialCriteriaPanel(WFSPanel panel) {
        super();
        this.wfsPanel = panel;
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
        
        //FIXME is the user allowed to input own geometry???
        String[] gg = new String[0]; 
        
        geomPropsCombo = new JComboBox( gg );
        
        /*
        geomPropsCombo.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    QualifiedName geoProp = (QualifiedName)e.getItem();
                    
                    researchDialog.setGeoProp( geoProp );
                                        
                    // create a new entry 
                    if ( e.getStateChange() == ItemEvent.SELECTED ){
                        if ( !"".equals( newName )){
                            addGeoPropName( newName );
                        }
                    }
                }
            }
        );
*/
        
        JPanel p = new JPanel();
        p.add( new JLabel(Messages.getString("SpatialResearchPanel.geometryName") ) );
        p.add( geomPropsCombo );
        add( p );
        return p;
    }
        
    private JComponent createSRSCombo(){
        
        srsCombo = new JComboBox( new DefaultComboBoxModel(new String[0]) );
        srsCombo.setEnabled( false );
        //UT nothing happens if user selects combo
        /*
        srsCombo.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    String newSRS = (String)e.getItem();
                    CoordinateSystem cs = null;
                    try {
                        cs = CRSFactory.create( newSRS );
                    } catch ( UnknownCRSException e1 ) {
                        e1.printStackTrace();
                    }
                    
                    wfsPanel.setGMLGeometrySRS( cs );
//                  FIXME this is not qut nice here, but JUMP is lacking 
                    //a mechanism for srs, anyway
                    String srs = (String)srsCombo.getSelectedItem();
                    
                    //UT TODO
                    //this is bad -> dependency
                    JOptionPane.showMessageDialog( null, "FIX ME" );
                    //TransactionFactory.setCrs( srs );

                    // create a new entry 
                    if ( e.getStateChange() == ItemEvent.SELECTED ){
                        if ( !"".equals( newSRS )){
                            srsCombo.addItem( newSRS );
                        }
                    }
                }
            }
        );
        */
        //UT combo is not editable. There should be one SRS to rule them all
//        srsCombo.setEditable( true );
        
        JPanel p = new JPanel();
        p.add( new JLabel( Messages.getString("SpatialResearchPanel.srs")) );
        p.add( srsCombo );
        add( p );
        return p;
    }
     
    public void setCrs( String[] crs ){
        this.srsCombo.setModel( new DefaultComboBoxModel(crs) );
        srsCombo.setEnabled( true );

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
        
        LayoutManager lm2 = new GridLayout( OPERATION_NAMES.length, 1);
        opsPanel.setLayout( lm2 );
        opsFieldPanel.setLayout( lm2 );
        
        ActionListener bal = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton rb = (JRadioButton)e.getSource();
                selectedOperation = rb.getActionCommand();
            }
        };

        ButtonGroup bg = new ButtonGroup();
        
                
        opButtons = new JRadioButton[ OPERATION_NAMES.length ];
        String simpleName = getClass().getSimpleName();  
        for (int i = 0; i < OPERATION_NAMES.length; i++) {
            String txt = simpleName + "." + OPERATION_NAMES[i];
            String buttonTxt = Messages.getString( txt );
            opButtons[i] = new JRadioButton( buttonTxt );
            txt += ".descrip";
            buttonTxt = Messages.getString( txt );
            opButtons[i].setToolTipText( buttonTxt );
            opButtons[i].setActionCommand( OPERATION_NAMES[i] );
            opButtons[i].addActionListener( bal );
            opButtons[i].setBounds( PropertyCriteriaPanel.LEFT_MARGIN + 10, (i*25) + 25, 270, 20);
            opButtons[i].setAlignmentX( Component.LEFT_ALIGNMENT );

            bg.add(opButtons[i]);
            
            if ( "DWithin".equals( OPERATION_NAMES[i] ) ){
                opsFieldPanel.add( dWithinDistanceField );
            } else if ( "Beyond".equals( OPERATION_NAMES[i] ) ){
                beyondDistanceField.setEnabled( false ); 
                opsFieldPanel.add( beyondDistanceField);
            }else{
                opsFieldPanel.add( Box.createHorizontalStrut(1) );
            }
            
            opsPanel.add( opButtons[i] );
            
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
    
   /* public void addGeoPropName(String name){
        
        int size = geomPropsCombo.getModel().getSize();
        List candidateGeoProps = new ArrayList( size );
        for (int i = 0; i < size; i++) {
            candidateGeoProps.add( geomPropsCombo.getModel().getElementAt( i ) );
        }
        if( name != null && !candidateGeoProps.contains( name ) ){
			geomPropsCombo.addItem(name);
        }
        geomPropsCombo.setSelectedItem( name );
    }*/
    
    /**Returns the XML fragment conating the spatial operation
     * 
     * @return the XML description of the spatial operation
     */
    public StringBuffer getXmlElement(){
        
        int opType = OperationDefines.getIdByName( selectedOperation );

        double dist = 0;
        if( "DWithin".equals(selectedOperation) ){
            dist = dWithinDistanceField.getDistance();
        } else if( "Beyond".equals(selectedOperation) ){
            dist = beyondDistanceField.getDistance();
        }
        StringBuffer sb = new StringBuffer();
        Geometry geometry = wfsPanel.getSelectedGeometry();
        if (geometry == null){
            return sb;
        }
        try {
            ((GeometryImpl)geometry).setCoordinateSystem( CRSFactory.create( (String)this.srsCombo.getSelectedItem() )  );
        } catch ( UnknownCRSException e ) {
            e.printStackTrace();
        }
        
        //TODO when JUMP accepts more than one geo pro, should offer it in a combo
        // only that is returning the string only, need the wualified name here
//        String geoProp = this.geomPropsCombo.getSelectedItem();
        
        QualifiedName qn = this.wfsPanel.getFeatureType();
        QualifiedName geoQn = (QualifiedName)geomPropsCombo.getSelectedItem();
        
        geoQn = new QualifiedName ( qn.getPrefix(), 
                                    geoQn.getLocalName() ,
                                    qn.getNamespace() );
        SpatialOperation spatialOp = 
            new SpatialOperation( opType, 
                    new PropertyName( geoQn ),
                    geometry, dist);

        // ad-hoc stuff -> will add this to deegree core
        // but not like this ;-)
        sb = spatialOp.toXML(); 
/*
 * This is not needed anymore since it is included in deegree somewhere,
 * otherwise the request is wrong and it has doubled ogc:Distance-tags.
 * SH: 18.05.2007
        if( "DWithin".equals(selectedOperation) || "Beyond".equals(selectedOperation) ){
            String d = "<ogc:Distance unit='http://www.uomdict.com/uom.html#meters' units='m'>" + 
            	dist + "</ogc:Distance>";
            String insert = "</ogc:" + selectedOperation + ">";
            int i = sb.indexOf(insert);            
            sb.insert( i, d);
        }
*/
        
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

    public void resetGeoCombo( QualifiedName[] geometryProperties ) {
        this.geomPropsCombo.removeAllItems();
        if( geometryProperties != null ){
            for ( int i = 0; i < geometryProperties.length; i++ ) {
                this.geomPropsCombo.addItem( geometryProperties[i] );            
            }
        }
    }
    
    public void setEnabled(boolean enabled){
        super.setEnabled( enabled );
        if( !enabled ){
            resetGeoCombo( new QualifiedName[0] );
            setCrs( new String[0] );
        }
        this.srsCombo.setEnabled( enabled );
        this.geomPropsCombo.setEnabled( enabled );
        for ( AbstractButton b : opButtons ) {
            b.setEnabled( enabled );
        }
    }
    
}
