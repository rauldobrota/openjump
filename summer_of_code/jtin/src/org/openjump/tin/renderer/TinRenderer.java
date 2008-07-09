/**
 * 
 */
package org.openjump.tin.renderer;

import java.awt.Graphics2D;

import org.openjump.tin.TinLayer;
import org.openjump.tin.TriangulatedIrregularNetwork;
import org.openjump.tin.renderer.style.TinStyle;
import org.openjump.tin.renderer.style.BasicTinStyle;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;



/**
 * @author paradox
 *
 */
public class TinRenderer implements Renderer {

	private TinLayer tinLayer;
	private TinStyle style;
	
	 private boolean cancelled;
	 protected volatile boolean rendering = false;
	 
	 private LayerViewPanel layerViewPanel;

	 
	/**
	 * 
	 */
	public TinRenderer(TinLayer tinLayer, LayerViewPanel layerViewPanel) {
		this(tinLayer, layerViewPanel, new BasicTinStyle());
		
	}
	
	public TinRenderer(TinLayer tinLayer, LayerViewPanel layerViewPanel, TinStyle style) {
		this.tinLayer = tinLayer;
		this.layerViewPanel = layerViewPanel;
		this.style = style;
	}


	public void cancel() {
	    cancelled = true;
	}


	public void clearImageCache() {
		// TODO Auto-generated method stub

	}

	
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
		TriangulatedIrregularNetwork tin = this.tinLayer.getTin();
		Viewport viewport = layerViewPanel.getViewport();
		
		try {
			this.style.paint(tin, graphics, viewport);
		}
		catch (Exception e) {
			Assert.shouldNeverReachHere("TinRenderer.paint: "+e.toString());
		}
	}
}
