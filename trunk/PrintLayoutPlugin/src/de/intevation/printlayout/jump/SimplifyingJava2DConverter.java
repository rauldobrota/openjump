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
import java.awt.geom.Point2D;


public class SimplifyingJava2DConverter
extends      PreciseJava2DConverter
{
	/**
	 * The tolerance for the simplifications.
	 */
	protected double  tolerance;

	protected boolean preserveTopology;

	public SimplifyingJava2DConverter(
		PointConverter converter, 
		double         tolerance
	) {
		this(converter, tolerance, false);
	}

	public SimplifyingJava2DConverter(
		PointConverter converter, 
		double         tolerance,
		boolean        preserveTopology
	) {
		super(converter);
		this.tolerance        = tolerance;
		this.preserveTopology = preserveTopology;
	}

  protected Shape toPreciseShape(Polygon p) 
	throws NoninvertibleTransformException 
	{
		if (preserveTopology)
			return super.toPreciseShape(
				(Polygon)TopologyPreservingSimplifier.simplify(p, tolerance));

		Point2D [][] holesCoordinates;

		holesCoordinates = new Point2D[p.getNumInteriorRing()][];

		for (int i = 0; i < holesCoordinates.length; ++i)
			holesCoordinates[i] =
				toViewPoints(
					DouglasPeuckerLineSimplifier.simplify(
						p.getInteriorRingN(i).getCoordinates(), tolerance));

		return new PrecisePolygonShape(
			toViewPoints(
				DouglasPeuckerLineSimplifier.simplify(
					p.getExteriorRing().getCoordinates(), tolerance)),
			holesCoordinates);
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
