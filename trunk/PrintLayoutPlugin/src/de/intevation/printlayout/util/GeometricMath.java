/*
 * GeometricMath.java
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

public final class GeometricMath
{
	private GeometricMath() {
	}

	public static Point2D sub(Point2D p1, Point2D p2) {
		return new Point2D.Double(
			p1.getX() - p2.getX(),
			p1.getY() - p2.getY());
	}

	public static Point2D add(Point2D p1, Point2D p2) {
		return new Point2D.Double(
			p1.getX() + p2.getX(),
			p1.getY() + p2.getY());
	}

	public static Point2D scale(Point2D p1, double s) {
		return new Point2D.Double(
			p1.getX()*s,
			p1.getY()*s);
	}

	public static double dot(Point2D p1, Point2D p2) {
		return 
			p1.getX()*p2.getX() + p1.getY()*p2.getY();
	}

	public static final Point2D mid(Point2D p1, Point2D p2) {
		return new Point2D.Double(
			0.5d*(p1.getX() + p2.getX()),
			0.5d*(p1.getY() + p2.getY()));
	}

	public static final double angleBetween(Point2D p1, Point2D p2) {
		double dx = p1.getX() - p2.getX();
		double dy = p1.getY() - p2.getY();
		double a = Math.atan2(dy, dx);
		if (a < 0d) a += 2d*Math.PI;
		return a;
	}
}
// end of file

