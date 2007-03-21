/*
 * PreciseJava2DConverter.java
 * ---------------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.jump;

import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.GeometryCollectionShape;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.Shape;

import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class PreciseJava2DConverter
extends      Java2DConverter
{ 
	public static final double POINT_MARKER_SIZE = 3.0d;

	/** same thing is private in base class */
	protected PointConverter converter;

	public PreciseJava2DConverter(PointConverter converter) {
		super(converter);
		this.converter = converter;
	}

  protected Shape toPreciseShape(Polygon p) 
	throws NoninvertibleTransformException 
	{
		Point2D [][] holesCoordinates;

		holesCoordinates = new Point2D[p.getNumInteriorRing()][];

		for (int i = 0; i < holesCoordinates.length; ++i)
			holesCoordinates[i] = toViewPoints(
				p.getInteriorRingN(i).getCoordinates());

		return new PrecisePolygonShape(
			toViewPoints(p.getExteriorRing().getCoordinates()),
			holesCoordinates);
	}  

	public Point2D [] toViewPoints(Coordinate [] coords) 
	throws NoninvertibleTransformException 
	{
		Point2D [] points = new Point2D[coords.length];
		for (int i = 0; i < coords.length; ++i)
			points[i] = toViewPoint(coords[i]);
		return points;
	}

 	protected Shape toPreciseShape(MultiLineString mls)
	throws NoninvertibleTransformException 
	{
		GeneralPath path = new GeneralPath();

		for (int i = 0, N = mls.getNumGeometries(); i < N ; ++i) {
			LineString lineString = (LineString)mls.getGeometryN(i);
			path.append(toPreciseShape(lineString), false);
		}
		return path;
	}

	protected Shape coords2shape(Coordinate [] coords)
	throws NoninvertibleTransformException 
	{
		GeneralPath shape = new GeneralPath();

		Point2D viewPoint = toViewPoint(coords[0]);
		shape.moveTo((float)viewPoint.getX(), (float)viewPoint.getY());

		for (int i = 1; i < coords.length; ++i) {
			viewPoint = toViewPoint(coords[i]);
			shape.lineTo((float)viewPoint.getX(), (float)viewPoint.getY());
		}

		return shape;
	}

	protected Shape toPreciseShape(LineString lineString)
	throws NoninvertibleTransformException 
	{
		return coords2shape(lineString.getCoordinates());
	} 

  protected Shape toPreciseShape(GeometryCollection gc)
	throws NoninvertibleTransformException
	{
		GeometryCollectionShape shape = new GeometryCollectionShape();

		for (int i = 0, N = gc.getNumGeometries(); i < N; ++i) {
			Geometry g = (Geometry)gc.getGeometryN(i);
			shape.add(toShape(g));
		}

		return shape;
	}  

  protected Shape toPreciseShape(Point point)
	throws NoninvertibleTransformException 
	{
		Rectangle2D.Double pointMarker =
			new Rectangle2D.Double(
				0.0,
				0.0,
				POINT_MARKER_SIZE,
				POINT_MARKER_SIZE);

		Point2D viewPoint = toViewPoint(point.getCoordinate());
		pointMarker.x = (double) (viewPoint.getX() - (POINT_MARKER_SIZE / 2));
		pointMarker.y = (double) (viewPoint.getY() - (POINT_MARKER_SIZE / 2));

		return pointMarker;
	}

  protected Point2D toViewPoint(Coordinate modelCoordinate)
	throws NoninvertibleTransformException 
	{
		return converter.toViewPoint(modelCoordinate);
	}  

	public Shape toShape(Geometry geometry)
	throws NoninvertibleTransformException
	{
		if (geometry.isEmpty())
			return new GeneralPath();

		if (geometry instanceof Polygon)
			return toPreciseShape((Polygon)geometry);

		if (geometry instanceof MultiPolygon)
			return toPreciseShape((MultiPolygon)geometry);

		if (geometry instanceof LineString)
			return toPreciseShape((LineString)geometry);

		if (geometry instanceof MultiLineString)
			return toPreciseShape((MultiLineString)geometry);

		if (geometry instanceof Point)
			return toPreciseShape((Point)geometry);

		if (geometry instanceof GeometryCollection)
			return toPreciseShape((GeometryCollection)geometry);

		return super.toShape(geometry);
	}
}
// end of file
