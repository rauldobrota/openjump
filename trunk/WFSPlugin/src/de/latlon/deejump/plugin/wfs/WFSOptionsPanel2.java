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
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import sun.util.calendar.JulianCalendar;

import com.sun.org.apache.xalan.internal.xsltc.dom.AbsoluteIterator;

public class WFSOptionsPanel2 extends JPanel {

    private WFSOptions options;

    private JFormattedTextField maxFeaturesField;
    
    private JComponent protocolPanel;

    private JComboBox outputFormatChooser;
    
    
    public WFSOptionsPanel2(WFSOptions options) {
        super();
        if( options == null ) {
            throw new IllegalArgumentException("WFSOptions cannot be null.");
        }
        this.options = options;
        initGUI();
    }

    private void initGUI() {
        LayoutManager layoutManager = new BoxLayout(this, BoxLayout.Y_AXIS );
        setLayout( layoutManager );
        
        maxFeaturesField = new JFormattedTextField();
        maxFeaturesField.setColumns(4);
        maxFeaturesField.addPropertyChangeListener( "value", new PropertyChangeListener(){
            public void propertyChange( PropertyChangeEvent evt ) {
                options.setMaxFeatures( ((Integer)evt.getNewValue()).intValue() );
            }
        });
        JPanel dummy = new JPanel();
        dummy.add( new JLabel("Max number of Features:"));
        dummy.add( maxFeaturesField );
        
        add( dummy );
        add( createProtocolPanel() );
        add( Box.createVerticalStrut( 5 ) );
        add( createOutputFormatChooser() );
        
        refreshGUI();
        
    }

    private void refreshGUI() {
        
        maxFeaturesField.setValue( new Integer( options.getMaxFeatures() ));

        protocolPanel.removeAll();
        createProtocolButtons();        
        
        outputFormatChooser
            .setModel( new DefaultComboBoxModel(options.getOutputFormats()) );
        
    }

    private void createProtocolButtons() {
        
        ButtonGroup bg = new ButtonGroup();
        final String[] protocs = options.getProtocols();
        final String selProtoc = options.getSelectedProtocol();
        
        for (int i = 0; i < protocs.length; i++) {
            JRadioButton rb = new JRadioButton( protocs[i] );
            if( selProtoc.equals( protocs[i] ) ) {
                rb.setSelected(true);
            }
            final int ix = i; 
            rb.addActionListener( new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    WFSOptionsPanel2.this.options.setSelectedProtocol( protocs[ix] );
                }
            }); 
            bg.add( rb );
            protocolPanel.add( rb );
        }
    }

    private Component createProtocolPanel() {
        protocolPanel = new JPanel();
        protocolPanel.setBorder( BorderFactory.createTitledBorder("Protocols"));
        protocolPanel.setLayout( new BoxLayout(protocolPanel, BoxLayout.Y_AXIS ) );
        protocolPanel.setMaximumSize( new Dimension(150,100));
        protocolPanel.setAlignmentX(1f);
        return protocolPanel;
    }

    private JComponent createOutputFormatChooser() {
        
        outputFormatChooser = new JComboBox();
        Border b1 = BorderFactory.createTitledBorder("Output Formats: ");
        Border b2 = BorderFactory.createEmptyBorder(2,4,4,4);
        
        outputFormatChooser.setBorder( BorderFactory.createCompoundBorder(b1,b2));
        outputFormatChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox jcb = (JComboBox)e.getSource();
                WFSOptionsPanel2.this.options.setSelectedOutputFormat((String)jcb.getSelectedItem());
            }
        });
        
        JPanel dummy = new JPanel();
        dummy.add( new JLabel( "Output Formats: ") );
        dummy.add( outputFormatChooser );
        
        return outputFormatChooser;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(new JFrame(),
                        new WFSOptionsPanel2(new WFSOptions()), "Options",
                        -1);
                System.exit(0);
            }
        });

    }

}
