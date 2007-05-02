/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */

package de.latlon.deejump.plugin.wfs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.filterencoding.AbstractOperation;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyName;

import de.latlon.deejump.ui.Messages;



/**
 * This panel is a graphical user interface to attribute-based feature search.
 * It is intended to be used inside a <code>FeatureResearchDialog</code><p/>
 * Original design: Poth 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
class PropertyCriteriaPanel extends JPanel {
    
    /**The possible logical relationships between operations*/
    public static final String[] logicalRelationships = new String[]{"And", "Or"};  //$NON-NLS-1$ //$NON-NLS-2$
    
    /**The current realtionship between attribute clauses*/
    protected String currentRelationship = logicalRelationships[0];
        
    protected String _featureTypeName = "dummyType"; //$NON-NLS-1$
    protected String[] _attributeNames = new String[]{""}; //$NON-NLS-1$
    
    // will be NONE, BBOX, SEL_GEOM
    private String spatialSearchCriteria = WFSPanel.NONE; 
    
    /**A list containing the criteria */
    private ArrayList criteriaList = new ArrayList();
    
    /**The parent dialog. Keep this reference to make matters simple */
    private WFSPanel researchDialog;

    protected JComboBox featureTypeCombo;
    
    protected JLabel attLabel;
    protected JLabel operLabel;
    protected JLabel valLabel;
    protected JButton newCriteriaButton;
    protected JButton remCriteriaButton; 
    protected JButton describeFTButton; 
    protected JComponent criteriaListPanel;
    protected JRadioButton selecGeoButton;
    protected JRadioButton noCritButton;
    protected JPanel criteriaPanel;

    
    // Layout Constants
    protected static final int LEFT_MARGIN = 10;
    protected static final int SECOND_COL = 140;
    protected static final int THIRD_COL = 200;
    
    protected static final int STD_HEIGHT = 22;
    
    
    protected JCheckBox editableCheckBox;

    private JRadioButton bboxCritButton;
    
    
    /**
     * Creates an AttributeResearchPanel.
     * @param rd the parent FeatureResearchDialog
     */
    public PropertyCriteriaPanel( WFSPanel rd, JComboBox jcb) {
        super();
        this.researchDialog = rd;
        initGUI(jcb);
    }

    /** Initialize GUI */
    private void initGUI(JComboBox jcb){
        
        LayoutManager lm = new BoxLayout( this, BoxLayout.PAGE_AXIS); 
        setLayout( lm );

        JPanel p = new JPanel();
        //TODO i18n
        describeFTButton = new JButton("DescribeFeatureType");
        describeFTButton.addActionListener( new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                QualifiedName ft = PropertyCriteriaPanel.this.researchDialog.getFeatureType();
                String schema = PropertyCriteriaPanel.this.researchDialog.getWfService().getRawSchemaForFeatureType( ft.getAsString() );
                WFSPanel.createXMLFrame( PropertyCriteriaPanel.this, schema );
            }
        });
        p.add( describeFTButton );
        add( p ); 
        //FIXME this is bad, keeping a ref
        featureTypeCombo = jcb;
        //featureTypeCombo.setBounds(90,40,120,STD_HEIGHT);
        
        featureTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshPanel();
            }
        });
        
        add( createCriteriaPanel() );        
        add( createCriteriaButtons() );
        JPanel innerPanel = new JPanel();
        innerPanel.add( createLogicalButtons() );  
        innerPanel.add( Box.createHorizontalStrut( 10 ) );  
        innerPanel.add( createSpatialButtons() );
        add( innerPanel );
        add( createEditableCheckBox());
    }
    
    public void refreshPanel() {
        criteriaListPanel.removeAll();
        criteriaList.clear();
        criteriaListPanel.revalidate();
        criteriaListPanel.repaint();
        setHeadersEnabled(false);
        remCriteriaButton.setEnabled(false);
    }
    /**Creates the panel where the criteria list will be shown*/
    private JComponent createCriteriaPanel(){
        JPanel criteriaPanel = new JPanel();
        criteriaPanel.setLayout(null);
        criteriaPanel.setPreferredSize( new Dimension(360,125));
        criteriaPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("AttributeResearchPanel.attributeBasedCriteria")));
        criteriaPanel.setBounds(LEFT_MARGIN,70,360,125);
        
        attLabel = new JLabel( Messages.getString("AttributeResearchPanel.attribute"));
        attLabel.setBounds(LEFT_MARGIN, 18, SECOND_COL - LEFT_MARGIN, STD_HEIGHT);
        criteriaPanel.add(attLabel);
        
        operLabel = new JLabel(  Messages.getString("AttributeResearchPanel.operator"));
        operLabel.setBounds(LEFT_MARGIN + SECOND_COL + 5, 18, THIRD_COL - SECOND_COL, STD_HEIGHT);
        criteriaPanel.add(operLabel);
        
        valLabel = new JLabel( Messages.getString( "AttributeResearchPanel.comparisonValue") ); //$NON-NLS-1$
        
        valLabel.setBounds(LEFT_MARGIN + THIRD_COL + 5, 18, SECOND_COL - LEFT_MARGIN, STD_HEIGHT);
        criteriaPanel.add(valLabel);
        
        setHeadersEnabled(false);
        
        criteriaListPanel = new JPanel(){
            public Dimension getPreferredSize(){return new Dimension(300,400);}
        };
        criteriaListPanel.setBounds(LEFT_MARGIN,40,360,400);
        criteriaListPanel.setOpaque(false);        
        criteriaListPanel.setLayout(null);
                	
        criteriaPanel.add( criteriaListPanel );
        
        return criteriaPanel;
    }

    /**Creates the And/Or radio buttons, their button group and a panel for them*/
    private JComponent createLogicalButtons() {
        
        JRadioButton andButton = new JRadioButton( Messages.getString("AttributeResearchPanel.logicalAnd") );
        andButton.setBorder(
                BorderFactory.createEmptyBorder(2,10,2,10));
        andButton.setActionCommand(logicalRelationships[0]);
        andButton.doClick();
       
        JRadioButton orButton = new JRadioButton( Messages.getString("AttributeResearchPanel.logicalOr"));
        orButton.setBorder(
                BorderFactory.createEmptyBorder(2,10,5,10));
        orButton.setActionCommand(logicalRelationships[1]);
        
        ActionListener bal = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton rb = (JRadioButton)e.getSource();
                currentRelationship = rb.getActionCommand();
            }
        };
        andButton.addActionListener( bal );
        orButton.addActionListener( bal );
        
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(andButton);
        bg.add(orButton);
        JPanel b = new JPanel();
        LayoutManager lm = new BoxLayout( b, BoxLayout.PAGE_AXIS); 
        b.setLayout( lm );
        b.setAlignmentX( Component.LEFT_ALIGNMENT );
        b.setBorder(							
                BorderFactory.createTitledBorder(Messages.getString("AttributeResearchPanel.logicalLink") ));
        
        // add a bit of space
        b.add( Box.createRigidArea( new Dimension(20,10) ) );
        b.add(andButton);
        b.add( Box.createRigidArea( new Dimension(20,10) ) );
        b.add(orButton);
        b.setPreferredSize( new Dimension(150, 100) );
        
        return b;
    }
    
    /** Creates buttons for adding and removing criteria and a panel for them */ 
    private JComponent createCriteriaButtons(){

        newCriteriaButton = new JButton( Messages.getString("AttributeResearchPanel.addCriteria"));
        
        newCriteriaButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // no do something useful
                int i = criteriaListPanel.getComponentCount();
                if ( i == 0 ){
                    // make it pretty
                    setHeadersEnabled(true);
                    remCriteriaButton.setEnabled(true);
                }

                if ( i >= 4 ){
                    newCriteriaButton.setEnabled(false);                    
                }
                AttributeComparisonPanel ac = new AttributeComparisonPanel( researchDialog.attributeNames );
                ac.setBounds(0, (STD_HEIGHT + 4)* (i), 340, STD_HEIGHT );
                criteriaList.add(ac);
                criteriaListPanel.add(ac);
                criteriaListPanel.revalidate();
                criteriaListPanel.repaint();
            }
        });
        
        remCriteriaButton = new JButton( Messages.getString("AttributeResearchPanel.delCriteria"));
        remCriteriaButton.setEnabled(false);
        remCriteriaButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                int i = criteriaListPanel.getComponentCount();                               
                
                //Component c = criteriaListPanel.getComponent(i-1); 
                if ( i > 0 ){// && c instanceof AttributeComparison ){
                    Object o = criteriaListPanel.getComponent( i - 1 );
	                criteriaListPanel.remove((Component)o);
	                criteriaList.remove(o);
	                criteriaListPanel.revalidate();
	                criteriaListPanel.repaint();
	                newCriteriaButton.setEnabled(true);
                }
                if ( i == 1 ){
                    // make it pretty
                    setHeadersEnabled(false);
                    remCriteriaButton.setEnabled(false);
                }
            }
        });
        
        
        Box b = Box.createHorizontalBox();
        b.setBounds(LEFT_MARGIN, 290,300,100);
        b.add(newCriteriaButton);
        b.add( Box.createHorizontalStrut( 10 ));
        b.add(remCriteriaButton);
        //add(b);
        return b;
    }

    private JComponent createEditableCheckBox(){
   
        editableCheckBox = new JCheckBox(Messages.getString("AttributeResearchPanel.editable"), true);
        Box b = Box.createHorizontalBox();
        b.add(editableCheckBox);
       	return b;
    } 

    boolean isEditable(){
    	return editableCheckBox.isSelected();
    }
    
    /**Create buttons for choosing the spatial criteria and a panel for them*/
    private JComponent createSpatialButtons() {
        //JLabel label = new JLabel( GUIMessages.SPATIAL_CRITERIA );
        //label.setBounds(LEFT_MARGIN + 2, 370,200,18);
        //add(label);

        noCritButton = new JRadioButton( Messages.getString("AttributeResearchPanel.none") );
        noCritButton.setActionCommand( WFSPanel.NONE );
        noCritButton.doClick();        
        bboxCritButton = new JRadioButton( Messages.getString("AttributeResearchPanel.bbox"));
        bboxCritButton.setActionCommand( WFSPanel.BBOX );
        bboxCritButton.setBounds(10,30,200, STD_HEIGHT);
        selecGeoButton = new JRadioButton( Messages.getString("AttributeResearchPanel.selectedGeometry") );
        selecGeoButton.setActionCommand( WFSPanel.SELECTED_GEOM );
        
        ActionListener bal = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton rb = (JRadioButton)e.getSource();
                spatialSearchCriteria = rb.getActionCommand();
            }
        };
        noCritButton.addActionListener( bal );
        bboxCritButton.addActionListener( bal );
        selecGeoButton.addActionListener( bal );
        
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(noCritButton);
        bg.add(bboxCritButton);
        bg.add(selecGeoButton);
        
        Box b = Box.createVerticalBox();
        b.setAlignmentX(0.95f);
        b.setBorder(
                BorderFactory.createTitledBorder( Messages.getString("AttributeResearchPanel.spatialCriteria") ));
       // b.setBounds(LEFT_MARGIN, 390,250,100);
        b.add(noCritButton);
        b.add(bboxCritButton);
        b.add(selecGeoButton);
        b.setPreferredSize( new Dimension(150, 100) );
        
        return b;
    }



    /**Turns label on/off. */
    private void setHeadersEnabled(boolean on){
        attLabel.setEnabled(on);
        operLabel.setEnabled(on);
        valLabel.setEnabled(on);
    }
    
    /**
     * Gets the logical relationship tag. This is used in concatenating the XML 
     * GetFeature request. Valid logical relationship tags are <code>&lt;ogc:And&gt;</code>
     * or <code>&lt;ogc:Or&gt;</code>
     * @return the start and end xml tags
     */
    public String[] getLogicalRelationshipTags(){
        return WFSPanel.createStartStopTags( currentRelationship );     
    }
    
    /**
     * Returns an XML fragment containing some attribute-only filter clauses 
     * @return the XML fragment containing some attribute-only filter clauses 
     */
    public StringBuffer getXmlElement(){
        
        StringBuffer sb = new StringBuffer( 5000 );
        for (Iterator iter = criteriaList.iterator(); iter.hasNext();) {
            sb.append( ((AttributeComparisonPanel) iter.next()).getXmlElement() );           
        }

        if ( criteriaList.size() > 1){
            String[] logRels = getLogicalRelationshipTags();
            sb.insert( 0, logRels[0] ).append( logRels[1] );
        }
        
        return sb;        	
    }
    
    /** Gets the number of attribute conditions. This is needed because if there is a
     * BBOX and at least one codition, then an &lt;Andgt; is needed. 
     * 
     * @return the number of attribute-based conditions
     */
    public int getListSize(){ return criteriaList.size(); }
    
    /**
     * Returns the current spatial criteria. This can be NONE, BBOX or SELECTED_GEOMETRY
     * @return the chosen spatial criteria
     */
    public String getSpatialCriteria(){return this.spatialSearchCriteria;}

    /**Turns the "selected Geometry" button on/off. This is used when the selected geometry
     * is null and the button should be disabled (as clicking on it would have no effect 
     * @param enabled whether the button should be on/off
     */
    public void setSelGeoButtonEnabled( boolean enabled){
        selecGeoButton.setEnabled(enabled);
        //noCritButton.setSelected( );
        if ( !enabled ){
            if( noCritButton.isSelected() ){
                noCritButton.doClick();
            } else {
                bboxCritButton.doClick();
            }
        }
    }
    
    public void setEnabled(boolean enabled){
        newCriteriaButton.setEnabled( enabled );
        remCriteriaButton.setEnabled( enabled );
        
    }
    public void setFeatureTypeComboEnabled(boolean enabled){
        featureTypeCombo.setEnabled( enabled );
    }
    
    /**
     * A convenience class encapsulating an abstract filter operation, i.e. a clause
     * such as PropertyIsLike.<br/>
     * This panel includes a combo box conataining all attributes of a given feature
     * type, a combo box containing operators and a text field for the user to
     * input a comparison value. 
     * 
     * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
     *
     */
    class AttributeComparisonPanel extends JPanel{
        
        private JComboBox attributeCombo;
        private JComboBox operatorCombo;
        private JTextField valueField;
        
        // these should move to top type; perhaps the whole widget should
        private final String[] OPERATORS = new String[]{"=", "<", ">", "<=", ">=", "<>", "LIKE", "NOT LIKE" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        
        /**
         * Creates an AttributeComparisonPanel from a list of attributes
         * @param attributes the list of attributes of some feature type
         */
        AttributeComparisonPanel(String[] attributes){
            super();
            attributeCombo = new JComboBox(attributes);
            attributeCombo.setSelectedIndex(0);
            operatorCombo = new JComboBox(OPERATORS);
            operatorCombo.setSelectedIndex(0);
            valueField = new JTextField(""); //$NON-NLS-1$
            initGUI();
        }
        
        /**Initialize the GUI */
        private void initGUI(){
            setLayout(null);
            attributeCombo.setBounds(0, 0, SECOND_COL , STD_HEIGHT);
            add(attributeCombo);
            
            operatorCombo.setBounds(SECOND_COL + 5, 0, THIRD_COL - SECOND_COL , STD_HEIGHT);
            add(operatorCombo);
            
            valueField.setColumns(16);
            valueField.setBounds(THIRD_COL + 10, 0, 130, STD_HEIGHT);
            add(valueField);
        }
        
        /**
         * Gets the XML fragment representing this operation/clause
         * @return the XML fragment
         */
        public StringBuffer getXmlElement(){
            
            String localName = (String) attributeCombo.getSelectedItem();
            String val = valueField.getText();
            
            int opIndex = operatorCombo.getSelectedIndex();
            AbstractOperation oper = null;
            
            QualifiedName ftQualiName = researchDialog.getFeatureType();
            QualifiedName propName = new QualifiedName( ftQualiName.getPrefix(), localName, ftQualiName.getNamespace());
            
            try {
                oper = createOperation( opIndex, propName, val);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            }
            
            
            return oper.toXML();
        }
        
        /** There should be a way to construct an operation from its name;
         * haven't found one; when I find should use that method or put this one 
         * in deegree's core*/
        private AbstractOperation createOperation(int opCode, QualifiedName propName, String propVal)
        	throws Exception{
            AbstractOperation oper = null;
            switch (opCode) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                oper = new PropertyIsCOMPOperation( opCode + 100, new PropertyName(propName), new Literal(propVal) );
                break;
            case 5:                 
                oper = new PropertyIsCOMPOperation( 100, new PropertyName(propName), new Literal(propVal) ){
                				public StringBuffer toXML(){
                				    return super.toXML().insert(0, "<ogc:Not>").append("</ogc:Not>"); //$NON-NLS-1$ //$NON-NLS-2$
                				}
            	};
            	break;
                
            case 6: 
                oper = new PropertyIsLikeOperation(
                        new PropertyName(propName),
                        new Literal(propVal),
                        '*', '#', '!');
                break;

            case 7:		//PropertyIsNOTLikeOperation! 
                oper = new PropertyIsLikeOperation(
                        new PropertyName(propName),
                        new Literal(propVal),
                        '*', '#', '!'){
                			public StringBuffer toXML(){
                			    return super.toXML().insert(0, "<ogc:Not>").append("</ogc:Not>"); //$NON-NLS-1$ //$NON-NLS-2$
                			}
            			};
                break;
                
             default: 
                throw new Exception("Operation not defined!"); //$NON-NLS-1$
        }
            return oper;
        }
                
        public Dimension getPreferredSize(){return new Dimension(300,18);}
    }    
}
