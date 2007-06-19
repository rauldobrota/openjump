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

/**
 * Used by PickingInteractor to store a selected item.
 * <br>
 * The selected item carries the id for the element
 * in the SVG DOM tree.
 * <br>
 * This class is also used to draw a rubber band around
 * the bounding box of the selected item. If this
 * item is the only selected one a decoration
 * for scaling rotating and shearing is drawn also.
 */
public class OnScreenBox
{
	/**
	 * result of inside() if point is outside.
	 */
	public static final Integer OUTSIDE = null;

	/**
	 * result of inside() if point is inside bounding box.
	 */
	public static final Integer INSIDE  = new Integer(-1);

	/**
	 * the id in the DOM tree
	 */
	protected String     id;

	/**
	 * a cached shape of the bounding box
	 */
	protected Shape      shape;

	/**
	 * the edge points of the bounding box
	 */
	protected Point2D [] points;

	/**
	 * shapes of the decoration
	 */
	protected Shape []   decoration;

	/**
	 * 'time' when the geometries where updated last.
	 */
	protected int        transformTime;

	/**
	 * Creates an uninitalized OnScreenBox. 
	 * Use OnScreenBox(id) instead.
	 */
	public OnScreenBox() {
	}

	/**
	 * Creates an OnScreenBox with a given DOM tree id.
	 * @param id id of the selected DOM tree element
	 */
	public OnScreenBox(String id) {
		this.id = id;
	}

	/**
	 * the last time the PickingInteractor forces the
	 * geometries to be recalculated. Used for optimizing
	 * the region damage calculation.
	 * @return the 'time' of update. Only meaningful to PickingInteractor
	 */
	public int getTransformTime() {
		return transformTime;
	}

	/**
	 * sets the update time for the geometries.
	 * Only meaningful to PickingInteractor.
	 * @param transformTime the update 'time'
	 */
	public void setTransformTime(int transformTime) {
		this.transformTime = transformTime;
	}

	/**
	 * checks if a point (x, y) is inside the screen representation
	 * of this selected item.
	 * @param x x coord of the point
	 * @param y y coord of the point
	 * @return OUTSIDE if point is outside.<br>
	 *         INSIDE  if point is inside.<br>
	 *         if this item has a decoration it
	 *         is checked if the point is inside in one
	 *         of the shapes. If this is true the number
	 *         of the decoration shape is returned.
	 */
	public Integer inside(int x, int y) {
		if (points == null)
			return OUTSIDE;

		if (decoration != null)
			for (int i = 0; i < decoration.length; ++i)
				if (decoration[i].contains(x, y))
					return new Integer(i);

		return getShape().contains(x, y)
			? INSIDE
			: OUTSIDE;
	}

	/**
	 * Two OnScreenBox are equal if they represent the same
	 * DOM tree element.
	 * @param other has to be a OnScreenBox
	 * @return returns true if both represent the same id
	 */
	public boolean equals(Object other) {
		return id.equals(((OnScreenBox)other).id);
	}

	/**
	 * simply dumps the id.
	 * @return the DOM tree id
	 */
	public String toString() {
		return id;
	}

	/**
	 * The id of the DOM tree element represented by this OnScreenBox.
	 * @return the id
	 */
	public String getID() {
		return id;
	}

	/**
	 * The point of the bounding box at a given index.
	 * 0, 2, 4, 6 return the edges. 1, 3, 5, 7 the mid points.
	 * @param index the index of the requested point
	 * @return the point
	 */
	public Point2D getPoint(int index) {
		if ((index & 1) == 0)
			return points[index >> 1];

		index >>= 1;
		return GeometricMath.mid(
			points[index],
			points[(index + 1) % points.length]);
	}

	/**
	 * returns the number of points. Actually 8
	 */
	public int numPoints() {
		return points.length << 1;
	}

	/**
	 * calculates the 4 edge points of the given bounding box
	 * and transform them to screen coordinates by applying
	 * the given AffineTransform.
	 * @param bbox the bounding box
	 * @param xform the AffineTransform
	 * @return the bounding box of the projected bounding box.
	 * This is used for screen damage calculation.
	 */
	public Rectangle bbox2shape(SVGRect bbox, AffineTransform xform) {

		if (bbox == null)
			return null;

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

	/**
	 * builds a decoration for a given decoration type.
	 * The type is defined by PickingInteractor.
	 * Two types are currently known: SCALE_DECORATION
	 * (decoration for scaling) and ROTATE_DECORATION
	 * (decoration for rotation and shear).
	 * @param type the type of the decoration.
	 * @return the bounding box of all the decoration
	 * parts. This is used for region damage calculation.
	 */
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

	/**
	 * this builds the scale decoration. called by
	 * buildDecoration
	 */
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

		double ang1 =
			GeometricMath.angleBetween(points[0], points[2]) + 0.5d*Math.PI;
		double ang2 =
			GeometricMath.angleBetween(points[1], points[3]) + 0.5d*Math.PI;
		double ang4 =
			GeometricMath.angleBetween(points[0], points[1]) + 1.5d*Math.PI;
		double ang3 = 
			GeometricMath.angleBetween(points[1], points[2]) + 0.5d*Math.PI;  

		double [] angles = new double[] {
			ang1,
			ang3,
			ang2,
			ang4,
			ang1 + Math.PI,
			ang3 + Math.PI, 
			ang2 + Math.PI,
			ang4 + Math.PI
		};

		decoration = new Shape[ps.length];

		for (int i = 0; i < decoration.length; ++i)
			decoration[i] = Arrows.createArrow(
				0d, -11d,
				angles[i],
				1d,
				ps[i].getX(),
				ps[i].getY());
	}

	/**
	 * this builds the rotate and shear decoration.
	 * Called by buildDecoration()
	 */
	protected void buildRotateDecoration() {
		double ang1 = GeometricMath.angleBetween(points[0], points[2]);
		double ang2 = GeometricMath.angleBetween(points[1], points[3]);
		double ang3 =
			GeometricMath.angleBetween(points[0], points[1]) + 1.5d*Math.PI;
		double ang4 = 
			GeometricMath.angleBetween(points[1], points[2]) + 1.5d*Math.PI;

		double [] angles = new double[] {
			ang1,
			ang3,
			ang2,
			ang4,
			ang1 + Math.PI,
			ang3 + Math.PI, 
			ang2 + Math.PI,
			ang4 + Math.PI
		};

		Point2D [] ps = new Point2D[] {
			points[0],
			GeometricMath.mid(points[0], points[1]),
			points[1],
			GeometricMath.mid(points[1], points[2]),
			points[2],
			GeometricMath.mid(points[2], points[3]),
			points[3],
			GeometricMath.mid(points[3], points[0])
		};

		decoration = new Shape[angles.length];

		for (int i = 0; i < decoration.length; ++i)
			decoration[i] = Arrows.createArrow(
				+10d, 0d,
				angles[i],
				1d,
				ps[i].getX(), ps[i].getY());

	}

	/**
	 * constructs and caches the shape of the projected
	 * bounding box.
	 * @return the shape of the bounding box in screen
	 * coordinates.
	 */
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

	/**
	 * draws the shape of the projected bounding box to
	 * a given Graphics2D context.
	 * @param g2d the graphics context
	 */
	public void draw(Graphics2D g2d) {
		if (points != null)
			g2d.draw(getShape());
	}

	/**
	 * the stroke of the decoration
	 */
	public static final BasicStroke DECORATION_STROKE = new BasicStroke(2f);

	/**
	 * has this OnScreenBox a decoration?
	 * @return true if this OnScreenBox has a decoration else false
	 */
	public boolean hasDecoration() {
		return decoration != null;
	}

	/**
	 * draws the decoration of this OnScreenBox to a given
	 * Graphics2D context.
	 * @param g2d the graphics context.
	 */
	public void drawDecoration(Graphics2D g2d) {

		if (decoration != null) {
			g2d.setStroke(DECORATION_STROKE);
			for (int i = 0; i < decoration.length; ++i) {
				Shape deco = decoration[i];
				g2d.setPaint(Color.red);
				g2d.fill(deco);
				g2d.setPaint(Color.black);
				g2d.draw(deco);
			}
		}
	}
}
// end of file
