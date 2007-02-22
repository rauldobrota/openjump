/*
 * OnScreenBox.java
 * ----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Rectangle;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.w3c.dom.svg.SVGRect;

import de.intevation.printlayout.util.GeometricMath;
import de.intevation.printlayout.util.LineProjector;

public class OnScreenBox
{

	public static final Object OUTSIDE        = null;
	public static final Object INSIDE         = new Object();

	protected String     id;
	protected Shape      shape;

	protected Point2D [] points;

	protected Shape []   decoration;

	protected int        transformTime;

	public OnScreenBox() {
	}

	public OnScreenBox(String id) {
		this.id = id;
	}

	public int getTransformTime() {
		return transformTime;
	}

	public void setTransformTime(int transformTime) {
		this.transformTime = transformTime;
	}

	public Object inside(int x, int y) {
		if (points == null)
			return OUTSIDE;

		if (decoration != null)
			for (int i = 0; i < decoration.length; ++i)
				if (decoration[i].contains(x, y)) {
					int k = i >> 1;
					int j = ((i + points.length) >> 1) % points.length;
					Point2D current, origin;
					if ((i & 1) == 0) {
						current = points[k];
						origin  = points[j];
					}
					else {
						current = GeometricMath.mid(
							points[k], points[(k+1) % points.length]);
						origin = GeometricMath.mid(
							points[j], points[(j+1) % points.length]);
					}
					return new LineProjector(origin, current);
				}

		return getShape().contains(x, y)
			? INSIDE
			: OUTSIDE;
	}

	public boolean equals(Object other) {
		return id.equals(((OnScreenBox)other).id);
	}

	public String toString() {
		return id;
	}

	public String getID() {
		return id;
	}

	public Rectangle bbox2shape(SVGRect bbox, AffineTransform xform) {

		if (points == null) {
			points = new Point2D[4];
			for (int i = 0; i < points.length; ++i)
				points[i] = new Point2D.Double();
		}

		shape      = null;
		decoration = null;

		double x1 = bbox.getX();
		double y1 = bbox.getY();

		double x2 = x1 + bbox.getWidth();
		double y2 = y1 + bbox.getHeight();

		Point2D.Double src = new Point2D.Double(x1, y1);
		xform.transform(src, points[0]);

		/* src.x = x1; */ src.y = y2;
		xform.transform(src, points[1]);

		src.x = x2;  /* src.y = y2; */
		xform.transform(src, points[2]);

		/* src.x = x2; */  src.y = y1;
		xform.transform(src, points[3]);

		return getShape().getBounds();
	}

	public Rectangle buildDecoration(int type) {

		switch (type) {
			case PickingInteractor.SCALE_DECORATION:
				buildScaleDecoration();
				break;
			case PickingInteractor.ROTATE_DECORATION:
				buildRotateDecoration();
				break;
			default:
				decoration = null;
		}
		if (decoration != null && decoration.length > 0) {
			Rectangle damaged = decoration[0].getBounds();
			for (int i = 1; i < decoration.length; ++i)
				damaged.add(decoration[i].getBounds());
			return damaged;
		}
		return null;
	}

	protected void buildScaleDecoration() {

		Point2D [] ps = new Point2D [] {
			points[0],
			GeometricMath.mid(points[0], points[1]),
			points[1],
			GeometricMath.mid(points[1], points[2]),
			points[2],
			GeometricMath.mid(points[2], points[3]),
			points[3],
			GeometricMath.mid(points[3], points[0])
		};

		double ang1 = GeometricMath.angleBetween(points[0], points[2]);
		double ang2 = GeometricMath.angleBetween(points[1], points[3]);

		double [] a = new double[ps.length];

		a[0] = ang1 + Math.PI*0.5d;
		a[2] = ang2 + Math.PI*0.5d;
		a[1] = 0.5d*(a[2] + a[0]);
		a[4] = ang1 + Math.PI*1.5d;
		a[3] = 0.5d*(a[4] + a[2]) + Math.PI;
    a[6] = ang2 + Math.PI*1.5d;
		a[5] = 0.5d*(a[6] + a[4]);
		a[7] = 0.5d*(a[6] + a[0]);

		decoration = new Shape[ps.length];

		for (int i = 0; i < decoration.length; ++i)
			decoration[i] = Arrows.createArrow(
				0f, -11f,
				a[i],
				1f,
				ps[i].getX(),
				ps[i].getY());
	}

	protected void buildRotateDecoration() {
		double ang1 = GeometricMath.angleBetween(points[0], points[2]);
		double ang2 = GeometricMath.angleBetween(points[1], points[3]);

		Shape s1 = Arrows.createArrow(
			+10f, 0f,
			ang1,
			1f,
			points[0].getX(),
			points[0].getY());

		Shape s2 = Arrows.createArrow(
			+10f, 0f,
			ang2,
			1f,
			points[1].getX(),
			points[1].getY());

		Shape s3 = Arrows.createArrow(
			+10f, 0f,
			ang1 - Math.PI,
			1f,
			points[2].getX(),
			points[2].getY());

		Shape s4 = Arrows.createArrow(
			+10f, 0f,
			ang2 - Math.PI,
			1f,
			points[3].getX(),
			points[3].getY());

		decoration = new Shape [] { s1, s2, s3, s4 };
	}

	public Shape getShape() {
		if (shape == null && points != null) {
			GeneralPath path = new GeneralPath();

			path.moveTo(
				(float)points[0].getX(),
				(float)points[0].getY());

			for (int i = 1; i <= points.length; ++i) {
				Point2D p = points[i % points.length];
				path.lineTo((float)p.getX(), (float)p.getY());
			}

			path.closePath();
			shape = path;
		}
		return shape;
	}

	public Rectangle draw(Graphics2D g2d) {
		if (points == null)
			return null;

		Shape shape = getShape();

		g2d.draw(shape);
		
		return shape.getBounds();
	}

	public static final BasicStroke STROKE = new BasicStroke(2f);

	public boolean hasDecoration() {
		return decoration != null;
	}

	public void drawDecoration(Graphics2D g2d) {

		if (decoration != null)
			for (int i = 0; i < decoration.length; ++i) {
				Shape deco = decoration[i];
				g2d.setPaint(Color.red);
				g2d.fill(deco);
				g2d.setPaint(Color.black);
				g2d.setStroke(STROKE);
				g2d.draw(deco);
			}
	}
}
// end of file
