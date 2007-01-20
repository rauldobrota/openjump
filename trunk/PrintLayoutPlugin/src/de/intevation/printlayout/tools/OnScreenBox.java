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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.w3c.dom.svg.SVGRect;

public class OnScreenBox
{
	protected String     id;
	protected Shape      shape;

	protected Point2D [] points;

	protected int        decorationMode;
	protected Shape []   decoration;

	protected PickingInteractor.TransformOperation operation;

	public OnScreenBox() {
		decorationMode = 1;
		operation      = PickingInteractor.SCALE;
	}

	public OnScreenBox(String id) {
		this();
		this.id = id;
	}

	protected boolean insideRect(int x, int y) {

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
				return false;
		}

		return true;
	}

	public PickingInteractor.TransformOperation chooseOperation(int x, int y) {
		if (points == null)
			return null;

		if (decoration != null)
			for (int i = 0; i < decoration.length; ++i)
				if (decoration[i].contains(x, y))
					return operation;

		return insideRect(x, y)
			? PickingInteractor.TRANSLATE 
			: null;
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

	public void buildDecoration() {

		switch (decorationMode) {
			case 1:
				buildScale();
				break;
			default:
				decoration = null;
		}
	}

	private static final double angleBetween(Point2D p1, Point2D p2) {
		double dx = p1.getX() - p2.getX();
		double dy = p1.getY() - p2.getY();
		double a = Math.atan2(dy, dx);
		if (a < 0d) a += 2d*Math.PI;
		return a;
	}

	protected void buildScale() {
		double ang1 = angleBetween(points[0], points[2]);
		double ang2 = angleBetween(points[1], points[3]);

		AffineTransform rot1 = AffineTransform.getRotateInstance(ang1 - Math.PI);
		AffineTransform rot2 = AffineTransform.getRotateInstance(ang2 - Math.PI);
		AffineTransform rot3 = AffineTransform.getRotateInstance(ang1);
		AffineTransform rot4 = AffineTransform.getRotateInstance(ang2);

		AffineTransform trans1 = AffineTransform.getTranslateInstance(
			points[0].getX(), points[0].getY());

		AffineTransform trans2 = AffineTransform.getTranslateInstance(
			points[1].getX(), points[1].getY());

		AffineTransform trans3 = AffineTransform.getTranslateInstance(
			points[2].getX(), points[2].getY());

		AffineTransform trans4 = AffineTransform.getTranslateInstance(
			points[3].getX(), points[3].getY());

		trans1.concatenate(rot1);
		trans2.concatenate(rot2);
		trans3.concatenate(rot3);
		trans4.concatenate(rot4);

		decoration = new Shape [] {
			Arrows.L_R.createTransformedShape(trans1),
			Arrows.L_R.createTransformedShape(trans2),
			Arrows.L_R.createTransformedShape(trans3),
			Arrows.L_R.createTransformedShape(trans4)
		};
	}

	public void draw(Graphics2D g2d) {
		if (points == null)
			return;
		for (int i = 0; i < points.length; ++i) {
			int j = (i + 1) % points.length;

			Point2D p1 = points[i];
			Point2D p2 = points[j];
			g2d.drawLine(
				(int)Math.round(p1.getX()), (int)Math.round(p1.getY()), 
				(int)Math.round(p2.getX()), (int)Math.round(p2.getY()));
		}
	}

	public static final BasicStroke STROKE = new BasicStroke(2f);

	public void drawDecoration(Graphics2D g2d) {

		if (decoration == null)
			buildDecoration();

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
