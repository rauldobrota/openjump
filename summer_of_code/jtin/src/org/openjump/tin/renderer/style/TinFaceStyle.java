package org.openjump.tin.renderer.style;

import java.awt.Graphics2D;

import org.openjump.tin.TriangulatedIrregularNetwork;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;

public class TinFaceStyle implements TinStyle {

	public void initialize(Layer layer) {
		// TODO Auto-generated method stub

	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public void paint(TriangulatedIrregularNetwork tin, Graphics2D g,
			Viewport viewport) throws Exception {
		// TODO Auto-generated method stub

	}

	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub

	}

	  public Object clone() {
	        try {
	            return super.clone();
	        } catch (CloneNotSupportedException e) {
	            Assert.shouldNeverReachHere("TinFaceStyle.clone()");
	
	            return null;
	        }
	    }
}
