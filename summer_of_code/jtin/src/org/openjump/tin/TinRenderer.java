/**
 * 
 */
package org.openjump.tin;

import java.awt.Graphics2D;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.Viewport;



/**
 * @author paradox
 *
 */
public class TinRenderer implements Renderer {

	private TinLayer tinLayer;
	
	 private boolean cancelled;
	 protected volatile boolean rendering = false;
	 
	 private LayerViewPanel layerViewPanel;

	 
	/**
	 * 
	 */
	public TinRenderer(TinLayer tinLayer, LayerViewPanel layerViewPanel) {
		this.tinLayer = tinLayer;
		this.layerViewPanel = layerViewPanel;
	}
	

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.renderer.Renderer#cancel()
	 */
	public void cancel() {
	    cancelled = true;
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.renderer.Renderer#clearImageCache()
	 */
	public void clearImageCache() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.renderer.Renderer#copyTo(java.awt.Graphics2D)
	 */
	public void copyTo(Graphics2D graphics) {
		try {		
			rendering = true;			      
			cancelled = false;			      
			if (render()) {			        
				paint(graphics);			      
			}			    
		} catch (Throwable t) {			     
			t.printStackTrace();			      
			return;			   
		} finally {			     
			rendering = false;			     
			cancelled = false;
		}
	}

	
	public boolean render() {
		if (!tinLayer.isVisible()) {		      
			return false;		    
		}		
		if (!tinLayer.getLayerManager().getLayerables(Layerable.class).contains(		
				tinLayer)) {		      
			return false;		    
		}		  
		return withinVisibleScaleRange();		
	}

	
	public boolean withinVisibleScaleRange() {	
		if (tinLayer.isScaleDependentRenderingEnabled()) {		
			Double maxScale = tinLayer.getMaxScale();		    
			Double minScale = tinLayer.getMinScale();		    
			if (maxScale != null && minScale != null) {		    
				Viewport viewport = layerViewPanel.getViewport();		        
				double scale = 1d / viewport.getScale();
				if (scale < tinLayer.getMaxScale()) {		        
					return false;		        
				}		        
				if (scale > tinLayer.getMinScale()) {		        
					return false;		        
				}		      
			}
		}		
		return true;		
	}

	
	public Runnable createRunnable() {
		return null;
	}

	public Object getContentID() {
		return this.tinLayer;
	}

	public boolean isRendering() {
		return this.rendering;
	}
	
	  private void paint(Graphics2D graphics) {
		  
	  }
}
