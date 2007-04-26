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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.PropertyType;

/**
 * ... 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 * 
 * @since 2.0
 */

public class PropertySelectionPanel extends JPanel {

    
    private WFSPanel parentDialog;

    protected JList propertiesList;

    protected JComboBox geoPropsCombo; 
    
    public PropertySelectionPanel( WFSPanel parentDialog ){
        super();
        this.parentDialog = parentDialog;
        initGUI();
        setEnabled( false );
    }
    
    private void initGUI() {
        
        JPanel p = new JPanel();
        p.setLayout( new BoxLayout(p, BoxLayout.Y_AXIS ));
        p.setBorder( BorderFactory.createTitledBorder( "Download Properties: " ) );

        propertiesList = new JList();
        JScrollPane scrollPane = new JScrollPane( propertiesList );
        
        Dimension dim = new Dimension( 400, 200 );
        
        scrollPane.setMaximumSize( dim );
        scrollPane.setPreferredSize( dim );
        scrollPane.setMinimumSize( dim );
        
        
        p.add( scrollPane );
        
        add( p );
        
        geoPropsCombo = new JComboBox();
        
        dim = new Dimension( 200, 40 );
        geoPropsCombo.setMaximumSize( dim );
        geoPropsCombo.setPreferredSize( dim );
        geoPropsCombo.setMinimumSize( dim );
        
        
        geoPropsCombo.setBorder( BorderFactory.createTitledBorder( "Geometry Property: " ) );
        
        add( geoPropsCombo );
        
    }

    public void setProperties( String[] simpleProps, QualifiedName[] geoProps ){

        resetPropsList( simpleProps );
        resetGeoCombo( geoProps );
    }
    
    private void resetGeoCombo( QualifiedName[] geoProps ) {
        geoPropsCombo.removeAllItems();
        for ( int i = 0; i < geoProps.length; i++ ) {
            
            if( i == 0 ){
                this.parentDialog.setGeoProperty( geoProps[i] );
            }
            geoPropsCombo.addItem( geoProps[i] );
        }
    }

       
    private void resetPropsList( String[] props ){
        propertiesList.removeAll();
        DefaultListModel listModel = new DefaultListModel();
        int[] selIndices = new int[ props.length ];
        for ( int i = 0; i < props.length; i++ ) {
            listModel.addElement( props[i] );
            selIndices[i] = i;
        }
        propertiesList.setModel( listModel );
        propertiesList.setSelectedIndices( selIndices );
    
    }
    
    
    public StringBuffer getXmlElement(){
        
        StringBuffer sb = new StringBuffer( 5000 );
        
        QualifiedName ftQualiName = parentDialog.getFeatureType();

        GMLSchema schema = 
            this.parentDialog.getWfService().getSchemaForFeatureType( ftQualiName.getPrefix() + ":" + ftQualiName.getLocalName() );
            
        FeatureType[] featTypes = schema.getFeatureTypes();
        
        if( featTypes.length < 1 ){
            throw new RuntimeException( "Schema doesn't define any FeatureType. Must have at least one.");
        }
        
        // put what's been chosen in a list
        Object[] objs = propertiesList.getSelectedValues();
        List chosenProps = new ArrayList( objs.length );
        for ( int i = 0; i < objs.length; i++ ) {
            chosenProps.add( objs[i] );
        }
        
        // and loop over the correct order, seing what's in the list
        PropertyType[] featProperties = featTypes[0].getProperties();
        for ( int i = 0; i < featProperties.length; i++ ) {

            if ( chosenProps.contains( featProperties[i].getName().getLocalName() ) ){
                sb.append( "<wfs:PropertyName>" )
                    .append( ftQualiName.getPrefix() )
                    .append( ":" )
                    .append( featProperties[i].getName().getLocalName() )
                    .append( "</wfs:PropertyName>" );
            }
            
            //geom prop
            QualifiedName qn = (QualifiedName) geoPropsCombo.getSelectedItem();
            if ( qn.equals( featProperties[i].getName() ) ) {
                sb.append( "<wfs:PropertyName>" )
                    .append( ftQualiName.getPrefix() )
                    .append( ":" )
                    .append( qn.getLocalName() )
                    .append( "</wfs:PropertyName>" );    
            }
            
        }
              
        return sb;          
    }
    
}


/* ********************************************************************
Changes to this class. What the people have been up to:
$Log$
Revision 1.2  2007/04/26 13:14:59  taddei
Major refactoring to use WFSPanel in a Frame (WFSFrame) and in a Dialog (WFSDialog, still to do).

Revision 1.1  2007/04/26 09:19:26  taddei
Added initial working version of classes and complementary files.

Revision 1.1.2.3  2006/08/17 06:48:53  ut
pre-release2 2.0

Revision 1.1.2.2  2006/06/02 13:39:53  ut
updates

Revision 1.1.2.1  2006/05/31 10:04:35  ut
deejump pre deegree day


********************************************************************** */