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

public class LineProjector {

	private Point2D origin;
	private Point2D direction;
	private double  scale;

	public LineProjector(Point2D p1, Point2D p2) {
		origin    = (Point2D)p1.clone();
		direction = GeometricMath.sub(p2, p1);
		scale     = 1d/GeometricMath.dot(direction, direction);
	}

	public Point2D nearestPointOnLine(Point2D p) {
		double t = scale *
			GeometricMath.dot(
				direction, GeometricMath.sub(p, origin));

		return GeometricMath.add(
			origin, GeometricMath.scale(direction, t));
	}

	public double distanceFromLine(Point2D p) {
		double s = Math.sqrt(scale);
		double nx = -direction.getY()*s;
		double ny = direction.getX()*s;
		// x*nx + y*nx + d = 0
		double d  = -nx*origin.getX() - ny*origin.getY();
		return p.getX()*nx + p.getY()*ny + d;
	}

	public Point2D getOrigin() {
		return origin;
	}

	public Point2D getDirection() {
		return direction;
	}
}
// end of file
