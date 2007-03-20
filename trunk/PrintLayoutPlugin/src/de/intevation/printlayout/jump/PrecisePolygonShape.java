/*
 * PrecisePolygonShape.java
 * ------------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.jump;

import com.vividsolutions.jump.workbench.ui.renderer.java2D.ShapeCollectionPathIterator;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;  
import java.awt.geom.GeneralPath;  
import java.awt.geom.Point2D;  
import java.awt.geom.Rectangle2D;  

import java.awt.Rectangle;
import java.awt.Shape;

import java.util.ArrayList;

public class PrecisePolygonShape
implements   Shape
{
	protected GeneralPath    shell;
	protected GeneralPath [] holes;

	public PrecisePolygonShape() {
	}

	public PrecisePolygonShape(
		Point2D []   shellCoordinates,
		Point2D [][] holesCoordinates
	) {
		shell = toShape(shellCoordinates);
		holes = new GeneralPath[holesCoordinates.length];
		for (int i = 0; i < holes.length; ++i)
			holes[i] = toShape(holesCoordinates[i]);
	}

	public static final GeneralPath toShape(Point2D [] coords) {
		GeneralPath path = new GeneralPath();

		if (coords.length > 0) {
			path.moveTo((float)coords[0].getX(), (float)coords[0].getY());
			for (int i = 1; i < coords.length; ++i) {
				Point2D c = coords[i];
				path.lineTo((float)c.getX(), (float)c.getY());
			}
			path.closePath();
		}

		return path;
	}

	public Rectangle getBounds() {
		throw new UnsupportedOperationException(
			"Method getBounds() not yet implemented.");
	}

	public Rectangle2D getBounds2D() {
		return shell.getBounds2D();
	}

	public boolean contains(double x, double y) {
		throw new UnsupportedOperationException(
			"Method contains() not yet implemented.");
	}

	public boolean contains(Point2D p) {
		throw new UnsupportedOperationException(
			"Method contains() not yet implemented.");
	}

	public boolean intersects(double x, double y, double w, double h) {
		throw new UnsupportedOperationException(
			"Method intersects() not yet implemented.");
	}

	public boolean intersects(Rectangle2D r) {
		throw new UnsupportedOperationException(
			"Method intersects() not yet implemented.");
	}

	public boolean contains(double x, double y, double w, double h) {
		throw new UnsupportedOperationException(
			"Method contains() not yet implemented.");
	}

	public boolean contains(Rectangle2D r) {
		throw new UnsupportedOperationException(
			"Method contains() not yet implemented.");
	}

	public PathIterator getPathIterator(AffineTransform xform) {

		ArrayList rings = new ArrayList(1 + holes.length);
		rings.add(shell);

		for (int i = 0; i < holes.length; ++i)
			rings.add(holes[i]);

		return new ShapeCollectionPathIterator(rings, xform);
	}

	public PathIterator getPathIterator(AffineTransform xform, double flatness) {
		return getPathIterator(xform);
	}
}
// end of file
