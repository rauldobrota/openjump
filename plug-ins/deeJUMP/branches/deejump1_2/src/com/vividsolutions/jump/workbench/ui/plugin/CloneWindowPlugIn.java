
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

package com.vividsolutions.jump.workbench.ui.plugin;

import javax.swing.JInternalFrame;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.CloneableInternalFrame;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;


public class CloneWindowPlugIn extends AbstractPlugIn {
    public CloneWindowPlugIn() {
    }

    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);

        JInternalFrame frame = ((CloneableInternalFrame) context.getActiveInternalFrame()).internalFrameClone();
        context.getWorkbenchFrame().addInternalFrame(frame);
        //[sstein 23.04.2006] workaround since a white window will appear
        //         in the original task window if cloned window is closed   
        frame.setClosable (true);
        if (frame instanceof LayerViewPanelProxy) {
            //Need to update image buffer; otherwise, user will just see a white panel.
            //[Jon Aquino]
            ((LayerViewPanelProxy) frame).getLayerViewPanel().repaint();
        }

        return true;
    }
}
