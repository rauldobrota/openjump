
/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * JUMP is Copyright (C) 2003 Vivid Solutions
 *
 * This program implements extensions to JUMP and is
 * Copyright (C) 2004 Integrated Systems Analysts, Inc.
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
 * Integrated Systems Analysts, Inc.
 * 630C Anchors St., Suite 101
 * Fort Walton Beach, Florida
 * USA
 *
 * (850)862-7321
 * www.ashs.isa.com
 */


package org.openjump.core.ui.plugin.layer;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPopupMenu;

import org.openjump.io.SIDLayer;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class ToggleVisiblityPlugIn extends AbstractPlugIn
{   
	final static String toggleVisibility =I18N.get("org.openjump.core.ui.plugin.layer.ToggleVisiblityPlugIn.Toggle-Visibility");
	final static String errorSeeOutputWindow =I18N.get("org.openjump.core.ui.plugin.layer.ToggleVisiblityPlugIn.Error-See-Output-Window");
	
    public void initialize(PlugInContext context) throws Exception
    {
        WorkbenchContext workbenchContext = context.getWorkbenchContext();
        FeatureInstaller featureInstaller = new FeatureInstaller(workbenchContext);
        JPopupMenu layerNamePopupMenu = workbenchContext.getWorkbench()
                                                        .getFrame()
                                                        .getLayerNamePopupMenu();
//        layerNamePopupMenu.setToolTipText("5 selected objects");
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
            this, toggleVisibility,
            false, null,
            ToggleVisiblityPlugIn.createEnableCheck(workbenchContext));
        
        JPopupMenu wmsLayerNamePopupMenu = workbenchContext.getWorkbench()
                                                        .getFrame()
                                                        .getWMSLayerNamePopupMenu();
        featureInstaller.addPopupMenuItem(wmsLayerNamePopupMenu,
            this, toggleVisibility,
            false, null,
            ToggleVisiblityPlugIn.createEnableCheck2(workbenchContext));
        
    }
    
    public boolean execute(PlugInContext context) throws Exception
    {
        try
        {
            context.getWorkbenchFrame().getOutputFrame().createNewDocument();
            LayerNamePanel layerNamePanel = context.getWorkbenchContext().getLayerNamePanel();
            //layerNamePanel.addListener(layerNamePanelListener);
            
            Collection layerCollection = (Collection) context.getWorkbenchContext().getLayerNamePanel().selectedNodes(Layer.class);
            for (Iterator j = layerCollection.iterator(); j.hasNext();)
         {
                Layer layer = (Layer) j.next();
                layer.setVisible(!layer.isVisible());
                //context.getWorkbenchFrame().getOutputFrame().addText(layer.getName());
            }
            
            Collection sidLayerCollection = (Collection) context.getWorkbenchContext().getLayerNamePanel().selectedNodes(SIDLayer.class);
            for (Iterator j = sidLayerCollection.iterator(); j.hasNext();)
            {
                SIDLayer layer = (SIDLayer) j.next();
                layer.setVisible(!layer.isVisible()); 
                //context.getWorkbenchFrame().getOutputFrame().addText(layer.getName());
            }
            return true;
        }
        catch (Exception e)
        {
            context.getWorkbenchFrame().warnUser(errorSeeOutputWindow);
            context.getWorkbenchFrame().getOutputFrame().createNewDocument();
            context.getWorkbenchFrame().getOutputFrame().addText("ToggleVisiblityPlugIn Exception:" + e.toString());
            return false;
        }
    }
    
    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext)
    {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);        
        return new MultiEnableCheck()
           .add(checkFactory.createWindowWithSelectionManagerMustBeActiveCheck())
           .add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1));
    } 
    
    public static MultiEnableCheck createEnableCheck2(WorkbenchContext workbenchContext)
    {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);        
        return new MultiEnableCheck()
           .add(checkFactory.createWindowWithSelectionManagerMustBeActiveCheck())
           .add(checkFactory.createAtLeastNLayerablesMustBeSelectedCheck(1, SIDLayer.class));
    }     
}

