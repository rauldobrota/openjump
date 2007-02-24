/*
 * LineProjector.java
 * ------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.util;

import java.awt.geom.Point2D;

/**
 * An instance of this class represents a line in 2D
 * with the ability to calculate the point on
 * the line which is closest given another point
 * in the plane. This is used to stabilize motions
 * along this line. If the user e.g. moves the mouse
 * along a certain line on the screen the measured
 * coordinates always have some deviation in some
 * other direction. The measured point can be 
 * reprojected to this line by the LineProjector.
 */
public class LineProjector {

	/**
	 * the origin of the line
	 */
	private Point2D origin;

	/**
	 * the direction vector of the line
	 */
	private Point2D direction;

	/**
	 * scale of the direction vector
	 */
	private double  scale;

	/**
	 * Creates a line out of two points in the plane.
	 * Be aware of the fact that points need to be
	 * different, else there is no line.
	 * @param p1 the first point
	 * @param p2 the second point
	 */
	public LineProjector(Point2D p1, Point2D p2) {
		origin    = (Point2D)p1.clone();
		direction = GeometricMath.sub(p2, p1);
		scale     = 1d/GeometricMath.dot(direction, direction);
	}

	/**
	 * Calculates the point on the line which is closest
	 * to the given point.
	 * @param p the point in the plane
	 * @return the point on the line
	 */
	public Point2D nearestPointOnLine(Point2D p) {
		double t = scale *
			GeometricMath.dot(
				direction, GeometricMath.sub(p, origin));

		return GeometricMath.add(
			origin, GeometricMath.scale(direction, t));
	}

	/**
	 * Calculates the distance from the line for
	 * a given point. (e.g. 0 if its on the line)
	 * @param p the point in the plane
	 * @return the distance to the line
	 */
	public double distanceFromLine(Point2D p) {
		double s = Math.sqrt(scale);
		double nx = -direction.getY()*s;
		double ny = direction.getX()*s;
		// x*nx + y*nx + d = 0
		double d  = -nx*origin.getX() - ny*origin.getY();
		return p.getX()*nx + p.getY()*ny + d;
	}

	/**
	 * The origin vector of the line.
	 * @return the origin vector
	 */
	public Point2D getOrigin() {
		return origin;
	}

	/**
	 * The direction vector of the line.
	 * @return the direction vector.
	 */
	public Point2D getDirection() {
		return direction;
	}
}
// end of file
