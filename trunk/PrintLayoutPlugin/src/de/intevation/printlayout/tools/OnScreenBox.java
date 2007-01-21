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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.svg.SVGRect;

import de.intevation.printlayout.GeometricMath;

public class OnScreenBox
{
	public static final int OUTSIDE           = 0;
	public static final int INSIDE            = 1;
	public static final int INSIDE_DECORATION = 2;

	protected String     id;
	protected Shape      shape;

	protected Point2D [] points;

	protected Shape []   decoration;

	public OnScreenBox() {
	}

	public OnScreenBox(String id) {
		this.id = id;
	}

	protected int insideRect(int x, int y) {

		for (int i = 0; i < points.length; ++i) {
			int j = (i + 1) % points.length;

			Point2D p1 = points[i];
			Point2D p2 = points[j];

			double dx = p1.getX() - p2.getX();
			double dy = p1.getY() - p2.getY();

			double nx = dy;
			double ny = -dx;

			// nx*p1.x + ny*p1.y + b = 0
			double b = -(nx*p1.getX() + ny*p1.getY());

			if (x*nx + y*ny + b > 0d)
				return OUTSIDE;
		}

		return INSIDE;
	}

	public int inside(int x, int y) {
		if (points == null)
			return OUTSIDE;

		if (decoration != null)
			for (int i = 0; i < decoration.length; ++i)
				if (decoration[i].contains(x, y))
					return INSIDE_DECORATION;

		return insideRect(x, y);
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

	public void bbox2shape(SVGRect bbox, AffineTransform xform) {

		if (points == null)
			points = new Point2D[4];

		decoration = null;

		double x1 = bbox.getX();
		double y1 = bbox.getY();

		double x2 = x1 + bbox.getWidth();
		double y2 = y1 + bbox.getHeight();

		Point2D.Double src = new Point2D.Double(x1, y1);
		xform.transform(src, points[0] = new Point2D.Double());

		/* src.x = x1; */ src.y = y2;
		xform.transform(src, points[1] = new Point2D.Double());

		src.x = x2;  /* src.y = y2; */
		xform.transform(src, points[2] = new Point2D.Double());

		/* src.x = x2; */  src.y = y1;
		xform.transform(src, points[3] = new Point2D.Double());
	}

	protected void buildDecoration(int type) {

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
	}

	protected void buildScaleDecoration() {
		double ang1 = GeometricMath.angleBetween(points[0], points[2]);
		double ang2 = GeometricMath.angleBetween(points[1], points[3]);

		Shape s1 = Arrows.createArrow(
			0f, -11f,
			ang1 + Math.PI*0.5d,
			1f,
			points[0].getX(),
			points[0].getY());

		Shape s2 = Arrows.createArrow(
			0f, -11f,
			ang2 + Math.PI*0.5d,
			1f,
			points[1].getX(),
			points[1].getY());

		Shape s3 = Arrows.createArrow(
			0f, -11f,
			ang1 + Math.PI*1.5d,
			1f,
			points[2].getX(),
			points[2].getY());

		Shape s4 = Arrows.createArrow(
			0f, -11f,
			ang2 + Math.PI*1.5d,
			1f,
			points[3].getX(),
			points[3].getY());

		decoration = new Shape [] { s1, s2, s3, s4 };
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

	public void draw(Graphics2D g2d, Rectangle damaged) {
		if (points == null)
			return;

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		for (int i = 0; i < points.length; ++i) {
			int j = (i + 1) % points.length;

			Point2D p1 = points[i];
			Point2D p2 = points[j];

			int p2x = (int)Math.round(p2.getX());
			int p2y = (int)Math.round(p2.getY());

			if (p2x < minX) minX = p2x;
			if (p2x > maxY) maxX = p2x;
			if (p2y < minY) minY = p2y;
			if (p2y > maxY) maxY = p2y;

			g2d.drawLine(
				(int)Math.round(p1.getX()), (int)Math.round(p1.getY()), 
				p2x, p2y);
		}

		damaged.add(new Rectangle(
			minX, minY, maxX-minX+1, maxY-minY+1));
	}

	public static final BasicStroke STROKE = new BasicStroke(2f);

	public void drawDecoration(Graphics2D g2d, int type, Rectangle damaged) {

		buildDecoration(type);

		for (int i = 0; i < decoration.length; ++i) {
			Shape deco = decoration[i];
			g2d.setPaint(Color.red);
			g2d.fill(deco);
			g2d.setPaint(Color.black);
			g2d.setStroke(STROKE);
			g2d.draw(deco);
			damaged.add(deco.getBounds());
		}
	}
}
// end of file
