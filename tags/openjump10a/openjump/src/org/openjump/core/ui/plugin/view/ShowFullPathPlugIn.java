
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
import java.util.Map;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelListener;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import com.vividsolutions.jump.workbench.ui.TaskFrame;

public class ShowFullPathPlugIn extends AbstractPlugIn
{
    PlugInContext gContext;
	final static String sErrorSeeOutputWindow =I18N.get("org.openjump.core.ui.plugin.view.ShowFullPathPlugIn.Error-See-Output-Window");
	final static String sNumberSelected = I18N.get("org.openjump.core.ui.plugin.view.ShowFullPathPlugIn.NumberSelected");
	
	//-- added by sstein for test reasons
//	private LayerListener myLayerListener = new LayerListener() {
//        public void categoryChanged(CategoryEvent e) {}
//        public void featuresChanged(FeatureEvent e)  {}        
//		public void layerChanged(LayerEvent e) {
//        if (e.getType() == LayerEventType.ADDED || e.getType() == LayerEventType.REMOVED) {
//            Collection layerCollection = (Collection) gContext.getWorkbenchContext().getLayerNamePanel().getLayerManager().getLayers();
//            for (Iterator i = layerCollection.iterator(); i.hasNext();)
//            {
//                Layer layer = (Layer) i.next();
//                if (layer.hasReadableDataSource())
//                {
//                    DataSourceQuery dsq = layer.getDataSourceQuery();
//                    try{
//                    	String fname = dsq.getDataSource().getProperties().get("File").toString();                   	
//                    	layer.setDescription(fname);
//                    }
//                    catch(Exception e){
//                    	System.out.println("seems to be a database dataset" + e);
//                    }                }
//            	}   
//        	}
//		}
//	};
	
    private LayerNamePanelListener layerNamePanelListener =
    new LayerNamePanelListener()
    {
        public void layerSelectionChanged()
        {
        	LayerNamePanel lnp = gContext.getWorkbenchContext().getLayerNamePanel();
        	if(lnp !=null){
        		LayerManager lm = lnp.getLayerManager();
        		if (lm != null){
		            Collection layerCollection = (Collection) gContext.getWorkbenchContext().getLayerNamePanel().getLayerManager().getLayers();
		            for (Iterator i = layerCollection.iterator(); i.hasNext();)
		            {
		                Layer layer = (Layer) i.next();
		                if (layer.hasReadableDataSource())
		                {
		                	DataSourceQuery dsq = layer.getDataSourceQuery();			
			                try{
			                    	Map props = dsq.getDataSource().getProperties();
			                    	String fname = dsq.getDataSource().getProperties().get("File").toString();  
			                    	
			                    	layer.setDescription(fname);
			                }
			                catch(Exception e){
			                    	//System.out.println("ShowFullPathPlugIn: seems to be a database dataset?: " + layer.getDataSourceQuery().getDataSource().getClass() + "  " + e);			                		
			                		layer.setDescription("database");
			                		//exc eaten
			                }                
		                }
		            }
        		}
        	}
        }
    };
   
    private LayerViewPanelListener layerViewPanelListener =
    new LayerViewPanelListener()
    {
        public void selectionChanged()
        {
        	LayerViewPanel lvp=gContext.getWorkbenchContext().getLayerViewPanel();
        	if (lvp != null){
            	int numSel = gContext.getWorkbenchContext().getLayerViewPanel().getSelectionManager().getFeatureSelection().getSelectedItems().size();        
            	gContext.getWorkbenchFrame().setTimeMessage(sNumberSelected + " " + numSel);
        	}
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
//    	/***	added by sstein ***********************/
//	    //
//        // Whenever anything happens on an internal frame we want to do this.
//        //
//	    GUIUtil.addInternalFrameListener(
//	            context.getWorkbenchFrame().getDesktopPane(),
//	            GUIUtil.toInternalFrameListener(new ActionListener() {
//	        public void actionPerformed(ActionEvent e) {
//	            installListenersOnCurrentPanel();
//	        }
//	    }));
	    /**** original *********************************/
        context.getWorkbenchFrame().getDesktopPane().addContainerListener(
                new ContainerListener()
                {
                    public void componentAdded(ContainerEvent e)
                    {                              
                        Component child = e.getChild();
                        if (child.getClass().getName().equals("com.vividsolutions.jump.workbench.ui.TaskFrame"))
                        {
                        	LayerNamePanel lnp =((TaskFrame)child).getLayerNamePanel();                        	
                        	if(lnp != null){ //can we use LayerListener instead??
                        		((TaskFrame)child).getLayerNamePanel().addListener(layerNamePanelListener); 
                        	}
                            LayerViewPanel lvp = ((TaskFrame)child).getLayerViewPanel();
                            if (lvp != null){                            	
								lvp.addListener(layerViewPanelListener);
                            }
                    	}
                    }
                    
                    public void componentRemoved(ContainerEvent e)
                    {
                        Component child = e.getChild();
                        if (child.getClass().getName().equals("com.vividsolutions.jump.workbench.ui.TaskFrame"))
                        {
                        	LayerNamePanel lnp =((TaskFrame)child).getLayerNamePanel();                        	
                        	if(lnp != null){
                        		((TaskFrame)child).getLayerNamePanel().removeListener(layerNamePanelListener); 
                        	}
                            LayerViewPanel lvp = ((TaskFrame)child).getLayerViewPanel();
                            if (lvp != null){
                            ((TaskFrame)child).getLayerViewPanel().removeListener(layerViewPanelListener);
                            }
                    	}
                    }
                });   	
        
    }
    
//    //-- method by sstein adapted from Zoombar
//    private void installListenersOnCurrentPanel(){
//    	 System.out.println("try to install listener");
//        String LAYER_PATH_LISTENERS_INSTALLED_KEY =
//            Integer.toHexString(hashCode()) + " - LAYER PATH LISTENERS INSTALLED";
//        if (viewBlackboard().get(LAYER_PATH_LISTENERS_INSTALLED_KEY) != null) {
//            return;
//        }
//
//    	if(gContext.getLayerViewPanel() == null){
//    		return;
//    	}
//        //[sstein]        
//        LayerManager lm = gContext.getLayerManager();
//        lm.addLayerListener(this.myLayerListener);
//        System.out.println("listener installed");
//        viewBlackboard().put(LAYER_PATH_LISTENERS_INSTALLED_KEY, new Object());
//    }
//    
//    //-- method by sstein adapted from Zoombar    
//    private Blackboard viewBlackboard() {
//        return gContext.getLayerViewPanel() != null ? gContext.getLayerViewPanel().getBlackboard() : new Blackboard();
//    }
//    
    public boolean execute(PlugInContext context) throws Exception
    {
        return true;
    }
    
}

