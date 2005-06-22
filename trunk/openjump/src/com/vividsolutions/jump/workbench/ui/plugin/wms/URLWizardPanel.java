/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jump.workbench.ui.plugin.wms;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchException;
import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;
import com.vividsolutions.wms.MapImageFormatChooser;
import com.vividsolutions.wms.WMService;


public class URLWizardPanel extends JPanel implements WizardPanel {
    public static final String SERVICE_KEY = "SERVICE";
    public static final String FORMAT_KEY = "FORMAT";
    public static final String URL_KEY = "URL";
    private InputChangedFirer inputChangedFirer = new InputChangedFirer();
    private Map dataMap;
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel urlLabel = new JLabel();
    private JTextField urlTextField = new JTextField();
    private JPanel fillerPanel = new JPanel();
//  [UT]
    public static final String VERSION_KEY = "WMS_VERSION";
    
    public URLWizardPanel(String initialURL) {
        try {
            jbInit();
            urlTextField.setFont(new JLabel().getFont());
            urlTextField.setText(initialURL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void add(InputChangedListener listener) {
        inputChangedFirer.add(listener);
    }

    public void remove(InputChangedListener listener) {
        inputChangedFirer.remove(listener);
    }

    void jbInit() throws Exception {
        urlLabel.setText("URL:");
        this.setLayout(gridBagLayout1);
        urlTextField.setPreferredSize(new Dimension(300, 21));
        urlTextField.setText("http://");
        urlTextField.setCaretPosition(urlTextField.getText().length());
        urlLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 3) {
                    urlTextField.setText("http://slkapps2.env.gov.bc.ca/servlet/com.esri.wms.Esrimap");
                }
                super.mouseClicked(e);
            }
        });
        this.add(urlLabel,
            new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 4), 0, 0));
        this.add(urlTextField,
            new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 4), 0, 0));
        this.add(fillerPanel,
            new GridBagConstraints(2, 10, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
//      [UT]
        this.add(createVersionButtons(new String[]{WMService.WMS_1_0_0, WMService.WMS_1_1_1}),
                new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));    
    }

    public String getInstructions() {
        return I18N.get("ui.plugin.wms.URLWizardPanel.please-enter-the-url-of-the-wms-server");
    }


    //
    // The WMService appends other parameters to the end of the URL
    //
    private String fixUrlForWMService(String url) {
        String fixedURL = url.trim();

        if ( fixedURL.indexOf( "?" ) == -1 ) {
            fixedURL = fixedURL + "?";
        } else {
            if ( fixedURL.endsWith( "?" ) ) {
                // ok
            } else {
                // it must have other parameters
                if ( !fixedURL.endsWith( "&" ) ) {
                    fixedURL = fixedURL + "&";
                }
            }
        }

        return fixedURL;
    }


    public void exitingToRight() throws IOException, WorkbenchException {
        dataMap.put(URL_KEY, urlTextField.getText());
//      [UT]
        String ver = (String)dataMap.get(VERSION_KEY);
        
        String url = fixUrlForWMService( urlTextField.getText() );
        //[UT] 20.04.2005 
        WMService service = new WMService( url, ver );
        //WMService service = new WMService( url );
        
        service.initialize();
        dataMap.put(SERVICE_KEY, service);
//[UT] 20.04.2005 added version
        MapImageFormatChooser formatChooser = new MapImageFormatChooser(ver);
        String format = formatChooser.chooseFormat(service.getCapabilities()
                                                          .getMapFormats());

        if (format == null) {
            throw new WorkbenchException(I18N.get("ui.plugin.wms.URLWizardPanel.the-server-does-not-support-gif-png-or-jpeg-format"));
        }

        dataMap.put(FORMAT_KEY, format);
        dataMap.put(MapLayerWizardPanel.INITIAL_LAYER_NAMES_KEY, null);
        formatChooser.setPreferLossyCompression(false);
        formatChooser.setTransparencyRequired(true);
    }

    public void enteredFromLeft(Map dataMap) {
        this.dataMap = dataMap;
        //[UT] 20.04.2005 
        dataMap.put( VERSION_KEY, WMService.WMS_1_1_1);
        urlTextField.setCaretPosition(0);
        urlTextField.moveCaretPosition(urlTextField.getText().length());
    }

    public String getTitle() {
        return I18N.get("ui.plugin.wms.URLWizardPanel.select-uniform-resource-locator-url");
    }

    public String getID() {
        return getClass().getName();
    }

    public boolean isInputValid() {
        return true;
    }

    public String getNextID() {
        return MapLayerWizardPanel.class.getName();
    }
    //[UT] 10.01.2005 
    private Component createVersionButtons(String[] versions){
        JPanel p = new JPanel();
        
        ActionListener al = new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                JRadioButton jb = (JRadioButton)e.getSource();
                dataMap.put( VERSION_KEY, jb.getText());
            }	
        };
        
        ButtonGroup group = new ButtonGroup();        
        JRadioButton[] buttons = new JRadioButton[ versions.length ];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JRadioButton(versions[i]);
            buttons[i].addActionListener(al);
            group.add(buttons[i]);
            p.add(buttons[i]);
        }
        group.setSelected(buttons[1].getModel(), true);        
        
        return p;
    }    
}
