/*
 * SimplifyingJava2DConverter.java
 * -------------------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.jump;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import com.vividsolutions.jts.simplify.DouglasPeuckerLineSimplifier;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import java.awt.Shape;

import java.awt.geom.NoninvertibleTransformException;

public class SimplifyingJava2DConverter
extends      PreciseJava2DConverter
{
	protected double tolerance;

	public SimplifyingJava2DConverter(
		PointConverter converter, 
		double         tolerance
	) {
		super(converter);
		this.tolerance = tolerance;
	}

  protected Shape toPreciseShape(Polygon p) 
	throws NoninvertibleTransformException 
	{
		return super.toPreciseShape(
			(Polygon)TopologyPreservingSimplifier.simplify(p, tolerance));
	}

	protected Shape toPreciseShape(LineString lineString)
	throws NoninvertibleTransformException 
	{
		return coords2shape(DouglasPeuckerLineSimplifier.simplify(
			lineString.getCoordinates(),
			tolerance));
	} 
}
// end of file
