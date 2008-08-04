package org.openjump.tin.renderer.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.NoninvertibleTransformException;
import java.util.List;
import java.util.Random;
import java.awt.Shape;

import javax.vecmath.Vector3d;

import org.openjump.tin.TinFacet;
import org.openjump.tin.TriangulatedIrregularNetwork;
import org.openjump.tin.ImmutableArrayTinFacet;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.StyleUtil;


public class BasicTinStyle implements TinStyle {

	//public static final Color       DEFAULT_FILL_COLOR  = new Color(0, 0, 0, 255);
	public static final Color       DEFAULT_FILL_COLOR  = new Color(0, 193, 0, 255);
	public static final Color       DEFAULT_LINE_COLOR  = DEFAULT_FILL_COLOR;
	public static final BasicStroke DEFAULT_FILL_STROKE = new BasicStroke(0);

	private Color fillColor = DEFAULT_FILL_COLOR;
	private Color lineColor = DEFAULT_LINE_COLOR;
	
	private BasicStroke lineStroke;
	private Stroke fillStroke = DEFAULT_FILL_STROKE;
	private boolean enabled = true;
	private String linePattern = "3";    
	
	private boolean debug = false;
	
	
    /*
    private LineStringStyle boundaryStyle;
    private LineStringStyle borderStyle;
    public BasicTinStyle( LineStringStyle boundaryStyle, LineStringStyle borderStyle ) {
    	this.borderStyle = borderStyle;
    	this.boundaryStyle = boundaryStyle;
    }
    */
    
	public BasicTinStyle() {
	}
	
	/**
     * Called before #paint is applied to each Feature.
     * @return false if #paint should not be called e.g. because vertices are not
     * shown. Don't need to check whether the layer is visible.
     */
	public void initialize(Layer layer) {
	}


	public void paint(TriangulatedIrregularNetwork tin, Graphics2D g,
			Viewport viewport) throws Exception {

		List<ImmutableArrayTinFacet> subsetTriangles = tin.getSubsetTriangles(viewport.getEnvelopeInModelCoordinates());
		if (debug) System.out.println("Number of triangles rendered: "+subsetTriangles.size());
		Random rand = new Random();
		
		// paint the facets
		for (TinFacet face : subsetTriangles) {
			try {
				//Color randColor = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
				Double red = Math.abs(this.fillColor.getRed() * face.getShadingDotProduct());
				Double green = Math.abs(this.fillColor.getGreen() * face.getShadingDotProduct());
				Double blue = Math.abs(this.fillColor.getBlue() * face.getShadingDotProduct());
				if (debug) System.out.println("face dot product: "+face.getShadingDotProduct()+"\tR.d: "+red+" R.i: "+red.intValue()+"\tG.d "+green+" G.i: "+green.intValue()+"\tB.d: "+blue+" B.i: "+blue.intValue());
				Color faceColor = new Color(red.intValue(), green.intValue(), blue.intValue(), 255);
		        g.setPaint(faceColor);
				g.setStroke(fillStroke);
				Shape faceShape = face.toShape(viewport);
		        g.draw(faceShape);
		        g.fill(faceShape);
			}
			catch (Exception e) {
				Assert.shouldNeverReachHere("BasicTinStyle.paint - face paint: "+e.toString());
			}
		}

		
		// draw the boundaries
		lineStroke = new BasicStroke(5);
		if (tin.getBoundaries() != null)
		try {
			Color randColor = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
			StyleUtil.paint(tin.getBoundariesAsMultiLineString(), g, viewport,
							false, null, null, true, lineStroke, randColor);	
		}
		catch (Exception e) {
			Assert.shouldNeverReachHere("BasicTINStyle.paint - boundary paint: "+e.toString());
		}
		
		// draw the breaklines
		if (tin.getBreaklines() != null)
		try {		
			Color randColor = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
			StyleUtil.paint(tin.getBreaklinesAsMultiLineString(), g, viewport,
					false, null, null, true, lineStroke, randColor);
		}
		catch (Exception e) {
			Assert.shouldNeverReachHere("BasicTINStyle.paint - breakline paint: "+e.toString());
		}
		
	}
	
	
	public boolean isEnabled() {
		return enabled;
	}

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

	
   public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere("BasicTinStyle.clone()");

            return null;
        }
    }

}
