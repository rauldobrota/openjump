package org.openjump.tin.triangulation;

import java.util.ArrayList;

import org.openjump.tin.TriangulatedIrregularNetwork;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public interface DelaunayTriangulator {
    public void addPoints(ArrayList<Point> pointList);
    public void addPoint(double x, double y);
    public TriangulatedIrregularNetwork getTin();
    public TriangulatedIrregularNetwork getTin(int SRID);
	public TriangulatedIrregularNetwork getTin(int SRID, Envelope envelope);
}
