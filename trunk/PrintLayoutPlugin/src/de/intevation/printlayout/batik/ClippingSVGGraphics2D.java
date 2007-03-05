/*
 * ClippingSVGGraphics2D.java
 * --------------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.batik;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.CubicCurve2D;

import java.awt.Shape;

import de.intevation.printlayout.util.CohenSutherland;

/**
 * This class extends Batik's SVGGraphics2D 
 * to do real clipping in draw() and fill.
 * This slows down the SVG generation process
 * a bit but produces less geometry in the
 * end and a tighter bounding box.
 * TODO: handle the imagery stuff piped
 * through drawImage(Image img, AffineTransform xform, ImageObserver obs)
 * as well.
 */
public class ClippingSVGGraphics2D
extends      SVGGraphics2D
{
	/**
	 * bounds to clip
	 */
	protected Rectangle2D bounds;

	/**
	 * Instance for Cohen Sutherland line clipping.
	 */
	protected CohenSutherland clip;

	/**
	 * Caches the last transform to reduce the number
	 * of matrix inversions.
	 */
	protected AffineTransform lastTransform;

	/**
	 * the cache inverse of lastTransform
	 */
	protected AffineTransform lastInverse;

	/**
	 * Creates the inverse of the given matrix.
	 * This method caches the last inverse to reduce
	 * needless recalculations of identical inverse matrices.
	 * @param xform the matrix to invert
	 * @return the inverse of xform
	 * @throws java.awt.geom.NoninvertibleTransformException
	 *        if xform is not invertible
	 */
	protected final AffineTransform createInverse(AffineTransform xform) 
	throws NoninvertibleTransformException
	{
		if (lastTransform == null || !lastTransform.equals(xform)) {
			AffineTransform inv = xform.createInverse();
			lastTransform = xform;
			lastInverse   = inv;
		}
		return lastInverse;
	}

	/**
	 * Create a new ClippingSVGGraphics2D.
	 * @param ctx          the SVGGeneratorContext
	 * @param textAsShapes generate shapes for text?
	 * @param bounds       clippings bounds
	 */
	public ClippingSVGGraphics2D(
		SVGGeneratorContext ctx, 
		boolean             textAsShapes,
		Rectangle2D         bounds
	) {
		super(ctx, textAsShapes);
		this.bounds = (Rectangle2D)bounds.clone();
		clip = new CohenSutherland(this.bounds);
	}

	/**
	 * Clips a path iterator against bounds.
	 * Only line segments are actually clipped.
	 * Other types quads and cubics are simply checked
	 * by there bounding box and ignored when they totally
	 * outside. If they intersect the bounds they are
	 * taken over completely.
	 * @param pi the PathIterator to clip
	 */
	protected GeneralPath clipPathIterator(PathIterator pi) {
		float [] p = new float[6];

		float x = 0f;
		float y = 0f;

		GeneralPath gp = new GeneralPath(pi.getWindingRule());

		Line2D.Float line = new Line2D.Float();

		while (!pi.isDone()) {
			switch (pi.currentSegment(p)) {

				case PathIterator.SEG_MOVETO:
					gp.moveTo(x = p[0], y = p[1]);
					break;

				case PathIterator.SEG_LINETO:
					line.setLine(x, y, p[0], p[1]);

					if (clip.clip(line)) {
						if (line.getX1() != x || line.getY1() != y)
							gp.moveTo((float)line.getX1(), (float)line.getY1());

						gp.lineTo((float)line.getX2(), (float)line.getY2());

						if (line.getX2() != p[0] || line.getY2() != p[1])
							gp.moveTo(p[0], p[1]);
					}
					else {
						gp.moveTo(p[0], p[1]);
					}
					x = p[0];
					y = p[1];
					break;

				case PathIterator.SEG_QUADTO:
					// do at least a simple bounding box check
					QuadCurve2D quad = new QuadCurve2D.Float(
						x, y, p[0], p[1], p[2], p[3]);
					if (quad.getBounds2D().intersects(bounds)) 
						gp.quadTo(p[0], p[1], x = p[2], y = p[3]);
					else
						gp.moveTo(x = p[2], y = p[3]);
					break;

				case PathIterator.SEG_CUBICTO:
					// do at least a simple bounding box check
					CubicCurve2D cubic = new CubicCurve2D.Float(
						x, y, p[0], p[1], p[2], p[3], p[4], p[5]);
					if (cubic.getBounds2D().intersects(bounds)) 
						gp.curveTo(p[0], p[1], p[2], p[3], x = p[4], y = p[5]);
					else
						gp.moveTo(x = p[4], y = p[5]);
					break;

				case PathIterator.SEG_CLOSE:
					gp.closePath();
					break;
			}
			pi.next();
		}

		return gp;
	}

	/**
	 * Detects if the given shape is a sequence of closed
	 * geometries.
	 * @param shape the shape to test
	 * @return true if the shape is a sequence of closed geometries,
	 *         else false
	 */
	private static final boolean isSimpleClosed(Shape shape) {
		PathIterator pi = shape.getPathIterator(null);
		float [] data = new float[6];
		while (!pi.isDone()) {
			int type = pi.currentSegment(data);
			pi.next();
			if (type == PathIterator.SEG_CLOSE && pi.isDone())
				return true;
		}
		return false;
	}

	/**
	 * Clips a given shape to bounds.
	 * @param shape the shape to clip.
	 * @return shape if shape is inside bounds,
	 *         null if outside bounds,
	 *         a new clipped shape if there is an intersection.
	 */
	protected final Shape clipToBounds(Shape shape) {
		try {
			AffineTransform xform = getTransform();
			AffineTransform inv = createInverse(xform);

			Area boundsAsArea = new Area(bounds);
			boundsAsArea.transform(inv);

			Rectangle2D shapeBounds = shape.getBounds2D();

			// inside
			if (boundsAsArea.contains(shapeBounds))
				return shape;

			// outside
			if (!boundsAsArea.intersects(shapeBounds))
				return null;

			if (!isSimpleClosed(shape)) {
				PathIterator path = shape.getPathIterator(xform);
				GeneralPath gp = clipPathIterator(path);
				gp.transform(inv);
				return gp;
			}

			// intersection
			Area area = new Area(shape);
			area.intersect(boundsAsArea);
			return area;
		}
		catch (NoninvertibleTransformException nite) {
			return null;
		}
	}

	/**
	 * Overwrites draw() from base class. This version of
	 * draw clips a shape to the bounds before calling
	 * draw() of the base class.
	 */
	public void draw(Shape shape) {
		if ((shape = clipToBounds(shape)) != null)
			super.draw(shape);
	}

	/**
	 * Overwrites fill() from base class. This version of
	 * draw clips a shape to the bounds before calling 
	 * draw() of the base class.
	 */
	public void fill(Shape shape) {
		if ((shape = clipToBounds(shape)) != null)
			super.fill(shape);
	}
}
// end of file
