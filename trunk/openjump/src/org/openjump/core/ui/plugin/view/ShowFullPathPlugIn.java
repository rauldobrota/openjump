
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


package org.openjump.core.ui.plugin.view;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Collection;
import java.util.Iterator;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelListener;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import com.vividsolutions.jump.workbench.ui.TaskFrame;

public class ShowFullPathPlugIn extends AbstractPlugIn
{
    PlugInContext gContext;
	final static String sErrorSeeOutputWindow =I18N.get("org.openjump.core.ui.plugin.view.ShowFullPathPlugIn.Error-See-Output-Window");
	final static String sNumberSelected = I18N.get("org.openjump.core.ui.plugin.view.ShowFullPathPlugIn.NumberSelected");
	
    private LayerNamePanelListener layerNamePanelListener =
    new LayerNamePanelListener()
    {
        public void layerSelectionChanged()
        {
            Collection layerCollection = (Collection) gContext.getWorkbenchContext().getLayerNamePanel().getLayerManager().getLayers();
            for (Iterator i = layerCollection.iterator(); i.hasNext();)
            {
                Layer layer = (Layer) i.next();
                if (layer.hasReadableDataSource())
                {
                    DataSourceQuery dsq = layer.getDataSourceQuery();
                    String fname = dsq.getDataSource().getProperties().get("File").toString();
                    layer.setDescription(fname);
                }
            }            
        }
    };
   
    private LayerViewPanelListener layerViewPanelListener =
    new LayerViewPanelListener()
    {
        public void selectionChanged()
        {
            int numSel = gContext.getWorkbenchContext().getLayerViewPanel().getSelectionManager().getFeatureSelection().getSelectedItems().size();
            gContext.getWorkbenchFrame().setTimeMessage(sNumberSelected + " " + numSel);
        }
        
        public void cursorPositionChanged(String x, String y)
        {
            
        }
        
        public void painted(Graphics graphics)
        {
            
        }
    };
            
    public void initialize(PlugInContext context) throws Exception
    {
        gContext = context;                
        context.getWorkbenchFrame().getDesktopPane().addContainerListener(
        new ContainerListener()
        {
            public void componentAdded(ContainerEvent e)
            {                              
                Component child = e.getChild();
                if (child.getClass().getName().equals("com.vividsolutions.jump.workbench.ui.TaskFrame"))
                {
                    ((TaskFrame)child).getLayerNamePanel().addListener(layerNamePanelListener);
                    ((TaskFrame)child).getLayerViewPanel().addListener(layerViewPanelListener);                    
            	}
            }
            
            public void componentRemoved(ContainerEvent e)
            {
                Component child = e.getChild();
                if (child.getClass().getName().equals("com.vividsolutions.jump.workbench.ui.TaskFrame"))
                {
                    ((TaskFrame)child).getLayerNamePanel().removeListener(layerNamePanelListener);
                    ((TaskFrame)child).getLayerViewPanel().removeListener(layerViewPanelListener);
            	}
            }
        });
    }
    
    public boolean execute(PlugInContext context) throws Exception
    {
        try
        {
            return true;
        }
        catch (Exception e)
        {
            context.getWorkbenchFrame().warnUser(sErrorSeeOutputWindow);
            context.getWorkbenchFrame().getOutputFrame().createNewDocument();
            context.getWorkbenchFrame().getOutputFrame().addText("ShowFullPathPlugIn Exception:" + e.toString());
            return false;
        }
    }
    
}

