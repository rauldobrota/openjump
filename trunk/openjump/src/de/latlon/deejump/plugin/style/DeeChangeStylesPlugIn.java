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
package de.latlon.deejump.plugin.style;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel;
import com.vividsolutions.jump.workbench.ui.style.DecorationStylePanel;
import com.vividsolutions.jump.workbench.ui.style.LabelStylePanel;
import com.vividsolutions.jump.workbench.ui.style.RenderingStylePanel;
import com.vividsolutions.jump.workbench.ui.style.ScaleStylePanel;
import com.vividsolutions.jump.workbench.ui.style.StylePanel;


public class DeeChangeStylesPlugIn extends AbstractPlugIn {
    private final static String LAST_TAB_KEY = DeeChangeStylesPlugIn.class.getName() +
        " - LAST TAB";

    public String getName(){
    	return I18N.get("ui.style.ChangeStylesPlugIn.change-styles");
    }
    
    public DeeChangeStylesPlugIn() {
    }

    public boolean execute(PlugInContext context) throws Exception {
        final Layer layer = context.getSelectedLayer(0);
        MultiInputDialog dialog = new MultiInputDialog(context.getWorkbenchFrame(),
                I18N.get("ui.style.ChangeStylesPlugIn.change-styles"), true);
        dialog.setInset(0);
        dialog.setSideBarImage(IconLoader.icon("Symbology.gif"));
        dialog.setSideBarDescription(
        		I18N.get("ui.style.ChangeStylesPlugIn.you-can-use-this-dialog-to-change-the-colour-line-width"));

        final ArrayList stylePanels = new ArrayList();
        DeeRenderingStylePanel renderingStylePanel = new DeeRenderingStylePanel(context.getWorkbenchContext()
                                                                                 .getWorkbench()
                                                                                 .getBlackboard(),
                layer);
                
        
        stylePanels.add(renderingStylePanel);
        stylePanels.add(new ScaleStylePanel(layer, context.getLayerViewPanel()));        

        //Only set preferred size for DecorationStylePanel or ColorThemingStylePanel;
        //otherwise they will grow to the height of the screen. But don't set
        //the preferred size of LabelStylePanel to (400, 300) -- in fact, it needs
        //a bit more height -- any less, and its text boxes will shrink to
        //zero-width. I've found that if you don't give text boxes enough height,
        //they simply shrink to zero-width. [Jon Aquino]
        DecorationStylePanel decorationStylePanel = new DecorationStylePanel(layer,
                context.getWorkbenchFrame().getChoosableStyleClasses());
        decorationStylePanel.setPreferredSize(new Dimension(400, 300));
        if (layer.getFeatureCollectionWrapper().getFeatureSchema()
                     .getAttributeCount() > 1) {
            Iterator iter = layer.getFeatureCollectionWrapper().getUltimateWrappee().getFeatures().iterator();
            String type = "";
            if( iter.hasNext() ){
                Feature f = (Feature)iter.next();
                type = f.getGeometry().getGeometryType();
                 
            }
            
            
            if( "MultiPoint".equals( type ) || "Point".equals( type ) ){
                DeeColorThemingStylePanel deeColorThemingStylePanel = new DeeColorThemingStylePanel(layer,
                    context.getWorkbenchContext());
            deeColorThemingStylePanel.setPreferredSize(new Dimension(400, 300));
            stylePanels.add(deeColorThemingStylePanel);
            GUIUtil.sync(renderingStylePanel.getTransparencySlider(),
                deeColorThemingStylePanel.getTransparencySlider());
            GUIUtil.sync(renderingStylePanel.getSynchronizeCheckBox(),
                deeColorThemingStylePanel.getSynchronizeCheckBox());

            } else {
                ColorThemingStylePanel colorThemingStylePanel = new ColorThemingStylePanel(layer,
                    context.getWorkbenchContext());
            colorThemingStylePanel.setPreferredSize(new Dimension(400, 300));
            stylePanels.add(colorThemingStylePanel);
            GUIUtil.sync(renderingStylePanel.getTransparencySlider(),
                colorThemingStylePanel.getTransparencySlider());
            GUIUtil.sync(renderingStylePanel.getSynchronizeCheckBox(),
                colorThemingStylePanel.getSynchronizeCheckBox());
                
            }

            
        } else {
            stylePanels.add(new DummyColorThemingStylePanel());
        }

        stylePanels.add(new LabelStylePanel(layer, context.getLayerViewPanel(),
                dialog, context.getErrorHandler()));
        stylePanels.add(decorationStylePanel);

        JTabbedPane tabbedPane = new JTabbedPane();

        for (Iterator i = stylePanels.iterator(); i.hasNext();) {
            final StylePanel stylePanel = (StylePanel) i.next();
            tabbedPane.add((Component) stylePanel, stylePanel.getTitle());
            dialog.addEnableChecks(stylePanel.getTitle(),
                Arrays.asList(
                    new EnableCheck[] {
                        new EnableCheck() {
                        public String check(JComponent component) {
                            return stylePanel.validateInput();
                        }
                    }
                    }));
        }

        dialog.addRow(tabbedPane);
        tabbedPane.setSelectedComponent(find(stylePanels,
                (String) context.getWorkbenchContext().getWorkbench()
                                .getBlackboard().get(LAST_TAB_KEY,
                    ((StylePanel) stylePanels.iterator().next()).getTitle())));
        dialog.setVisible(true);
        context.getWorkbenchContext().getWorkbench().getBlackboard().put(LAST_TAB_KEY,
            ((StylePanel) tabbedPane.getSelectedComponent()).getTitle());

        if (dialog.wasOKPressed()) {
            final Collection oldStyles = layer.cloneStyles();
            layer.getLayerManager().deferFiringEvents(new Runnable() {
                    public void run() {
                        for (Iterator i = stylePanels.iterator(); i.hasNext();) {
                            StylePanel stylePanel = (StylePanel) i.next();
                            stylePanel.updateStyles();
                        }
                        
                    }
                });

            final Collection newStyles = layer.cloneStyles();
            execute(new UndoableCommand(getName()) {
                    public void execute() {
                        layer.setStyles(newStyles);
                    }

                    public void unexecute() {
                        layer.setStyles(oldStyles);
                    }
                }, context);
            return true;
        }

        return false;
    }

    private Component find(Collection stylePanels, String title) {
        for (Iterator i = stylePanels.iterator(); i.hasNext();) {
            StylePanel stylePanel = (StylePanel) i.next();

            if (stylePanel.getTitle().equals(title)) {
                return (Component) stylePanel;
            }
        }

        Assert.shouldNeverReachHere();

        return null;
    }

    public ImageIcon getIcon() {
        return IconLoader.icon("Palette.gif");
    }

    public MultiEnableCheck createEnableCheck(
            final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(
                workbenchContext);

        return new MultiEnableCheck().add(
                checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck())
                // ScaledStylePanel assumes that the active window has a LayerViewPanel. [Jon Aquino 2005-08-09]
                .add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck())                
                .add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
    }

    private class DummyColorThemingStylePanel extends JPanel
        implements StylePanel {
        public DummyColorThemingStylePanel() {
            //GridBagLayout so it gets centered. [Jon Aquino]
            super(new GridBagLayout());
            add(new JLabel(I18N.get("ui.style.ChangeStylesPlugIn.this-layer-has-no-attributes")));
        }

        public String getTitle() {
            return DeeColorThemingStylePanel.TITLE;
        }

        public void updateStyles() {
        }

        public String validateInput() {
            return null;
        }
    }
}
