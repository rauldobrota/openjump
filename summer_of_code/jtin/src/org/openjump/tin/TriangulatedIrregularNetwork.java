/**
 * 
 */
package org.openjump.tin;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * @author paradox
 *
 */
public interface TriangulatedIrregularNetwork {
	public int getNumVertices();
	public int getNumTriangles();
	public int getSRID();
	public int getNumBreaklines();
	public int getNumBoundaries();
	public Coordinate getVertexN (int n);
	public Coordinate[] getVertices();
	public int[] getTriangleArrayN (int n);
	public List<int[]> getBreaklines();
	public List<int[]> getBoundaries();
	public MultiLineString getBreaklinesAsMultiLineString();
	public MultiLineString getBoundariesAsMultiLineString();
	public TinFace getTriangleN(int n);
	public TriangulatedIrregularNetwork subset(Envelope envelope);
	public List<TinFace> getSubsetTriangles(Envelope envelope);
}
