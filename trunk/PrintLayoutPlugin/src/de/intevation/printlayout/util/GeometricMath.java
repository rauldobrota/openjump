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

/**
 * Offers vector operations for points in the 2D plane.
 */
public final class GeometricMath
{
	private GeometricMath() {
	}

	/**
	 * Substracts a vector from another.
	 * @param p1 first vector
	 * @param p2 second vector
	 * @return p1 - p1 as a new vector
	 */
	public static Point2D sub(Point2D p1, Point2D p2) {
		return new Point2D.Double(
			p1.getX() - p2.getX(),
			p1.getY() - p2.getY());
	}

	/**
	 * Adds a vector to another.
	 * @param p1 first vector
	 * @param p2 second vector
	 * @return p1 + p1 as a new vector
	 */
	public static Point2D add(Point2D p1, Point2D p2) {
		return new Point2D.Double(
			p1.getX() + p2.getX(),
			p1.getY() + p2.getY());
	}

	/**
	 * Scale a vector by a scalar value.
	 * @param p1 the vector
	 * @param s the scalar value
	 * @return s*p1 as a new vector
	 */
	public static Point2D scale(Point2D p1, double s) {
		return new Point2D.Double(
			p1.getX()*s,
			p1.getY()*s);
	}

	/**
	 * Calculates the dot product of two vectors.
	 * @param p1 first vector
	 * @param p2 second vector
	 * @return p1 . p2
	 */
	public static double dot(Point2D p1, Point2D p2) {
		return 
			p1.getX()*p2.getX() + p1.getY()*p2.getY();
	}

	/**
	 * Calculates the mid point between two vectors.
	 * @param p1 first vector
	 * @param p2 second vector
	 * @return (p1 + p2)/2 as a new vector
	 */
	public static final Point2D mid(Point2D p1, Point2D p2) {
		return new Point2D.Double(
			0.5d*(p1.getX() + p2.getX()),
			0.5d*(p1.getY() + p2.getY()));
	}

	/**
	 * Calculates the angle between to vectors.
	 * @param p1 first vector
	 * @param p2 second vector
	 * @return angle between p1 and p2 in radian in range 0 to 2*pi
	 */
	public static final double angleBetween(Point2D p1, Point2D p2) {
		double dx = p1.getX() - p2.getX();
		double dy = p1.getY() - p2.getY();
		double a = Math.atan2(dy, dx);
		if (a < 0d) a += 2d*Math.PI;
		return a;
	}
}
// end of file

