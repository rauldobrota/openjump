/*----------------    FILE HEADER  ------------------------------------------

This file is part of deegree.
Copyright (C) 2001 by:
EXSE, Department of Geography, University of Bonn
http://www.giub.uni-bonn.de/exse/
lat/lon Fitzke/Fretter/Poth GbR
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
lat/lon Fitzke/Fretter/Poth GbR
Meckenheimer Allee 176
53115 Bonn
Germany
E-Mail: poth@lat-lon.de

Jens Fitzke
Department of Geography
University of Bonn
Meckenheimer Allee 166
53115 Bonn
Germany
E-Mail: jens.fitzke@uni-bonn.de

                 
 ---------------------------------------------------------------------------*/
package de.latlon.deejump.ui;

import java.awt.Component;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import org.openjump.core.ui.plugin.view.helpclassescale.ShowScaleRenderer;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarRenderer;

/**
 * ...
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class CustomInstallScalePlugIn extends AbstractPlugIn {

    private ComponentListener componentListener;
    
    /**
     * 
     */
    public CustomInstallScalePlugIn() {
        super();
    }


    
    public void initialize(PlugInContext context) throws Exception {
      
        ContainerListener ca = new ContainerAdapter(){
            public void componentAdded( ContainerEvent e ) {
                Component component = e.getChild();
                
                if ( component instanceof TaskFrame ) {
                    TaskFrame taskFrame = (TaskFrame) component;
                    
                    ShowScaleRenderer.setEnabled( true, taskFrame.getLayerViewPanel());
                    
                    taskFrame.getLayerViewPanel().getRenderingManager().render(ShowScaleRenderer.CONTENT_ID);
                }
            }
        };
        
        context.getWorkbenchFrame().getDesktopPane().addContainerListener( ca ); 
        
        
        /*ScaleBarRenderer.setEnabled(!ScaleBarRenderer.isEnabled(
            context.getLayerViewPanel()), context.getLayerViewPanel());
    context.getLayerViewPanel().getRenderingManager().render(ScaleBarRenderer.CONTENT_ID);
    */

    }
}
