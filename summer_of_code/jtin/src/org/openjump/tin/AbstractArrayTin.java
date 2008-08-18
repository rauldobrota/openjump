package org.openjump.tin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Vector3d;

import org.openjump.tin.io.JTFLayout;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.index.strtree.SIRtree;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.util.Assert;

public abstract class AbstractArrayTin implements TriangulatedIrregularNetwork {
	
	// list of vertices that compose the tin
	//private Coordinate[] vertices;
	private CoordinateArraySequence vertices;
	
	// the faces of the tin. Each TinFace is a collection of array indices.
	// Three indices point to the vertices array above, delineating the points
	// of the triangle. The other three indices point to the faceTable array
	// itself and create a connectedness map of the triangles.
	private TinFacet[] faceTable;
	
	// a list of arrays with each array containing the indices to the vertices
	// array that compose the given line. If the line is closed, the first and
	// last element will be equal. For each line, points should be arranged in
	// order.
	private List<int[]> breaklines = null;
	private List<int[]> boundaries = null;
	
	// the OpenJUMP compatible Spatial Reference ID of this TIN surface
	private int SRID;
	
	// a spatial index of the triangular faces, used for clipping and tin
	// subset creation.
	private STRtree faceIndex;
	
	// a spatial index of the height ranges of each face, used for elevation
	// sorting, height bands, and contour lines
	private SIRtree faceZIndex;
	
	// default light source vector used for hillshading
	private Vector3d lightVector = new Vector3d(0, 0, 1);
	
	private boolean debug = false;
	
	// these two coordinates describe a bounding cube that will encompass all
	// points and triangles. Min = lowest (x, y, z) value, Max = highest
	private Coordinate min = null, max = null;

	/**
	 * 
	 * @param points		an array of all the points that make up the tin
	 * @param triTable		a table of int array indicies: half of the elements
	 * 						point to points[], while the other half self 
	 * 						reflect to triTable[]
	 * @param breaklines	a list of arrays that contain ordered indicies to 
	 * 						points[], each array representing a breakline in 
	 * 						the TIN
	 * @param boundaries	a list of arrays that contain ordered indicies to 
	 * 						points[], each array representing a boundary in 
	 * 						the TIN
	 * @param spatialID		An OpenJUMP compatible spatial ID
	 * @see 				JTFLayout
	 */
	public AbstractArrayTin (final CoordinateSequence points, final int[][] triTable, 
						 final List<int[]> breaklines, final List<int[]> boundaries, 
						 final int spatialID) {

	}
	
	
	/**
	 * Constructs an <code>ImmutableTin</code> given a set of <code>TinFaces</code>
	 * from another <code>TriangulatedIrregularNetwork</code>, breaklines, 
	 * boundaries, and an SRID.
	 * 
	 * @param faceSubset	A list of faces from another TIN that this tin will
	 * 						duplicate
	 * @param breaklines	The breaklines from the other TIN. These will be 
	 * 						excerpted so that this TIN will include only those
	 * 						segments that border triangles found in 
	 * 						<code>faceSubset</code>
	 * @param boundaries	The boundaries from the other TIN. These will be 
	 * 						excerpted so that this TIN will include only those
	 * 						segments that border triangles found in 
	 * 						<code>faceSubset</code>
	 * @param SRID			The spatial reference ID for this TIN
	 */
	public AbstractArrayTin (final List<ImmutableArrayTinFacet> faceSubset, final List<int[]> breaklines, 
			final List<int[]> boundaries, final int SRID) {

	}


	public List<int[]> getBoundaries() {
		// TODO Auto-generated method stub
		return null;
	}

	public MultiLineString getBoundariesAsMultiLineString() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<int[]> getBreaklines() {
		// TODO Auto-generated method stub
		return null;
	}

	public MultiLineString getBreaklinesAsMultiLineString() {
		// TODO Auto-generated method stub
		return null;
	}

	public Geometry getContourLinesAtHeight(double height) {
		// TODO Auto-generated method stub
		return null;
	}

	public Coordinate getMaxBoundingCoordinate() {
		// TODO Auto-generated method stub
		return null;
	}

	public Coordinate getMinBoundingCoordinate() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumBoundaries() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumBreaklines() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumTriangles() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumVertices() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSRID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<ImmutableArrayTinFacet> getStrictSubsetTriangles(Envelope envelope) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ImmutableArrayTinFacet> getSubsetTriangles(Envelope envelope) {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] getTriangleArrayN(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	public TinFacet getTriangleN(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ImmutableArrayTinFacet> getTrianglesAtHeight(double z) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ImmutableArrayTinFacet> getTrianglesAtHeight(double z1, double z2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Coordinate getVertexN(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	public Coordinate[] getVertices() {
		// TODO Auto-generated method stub
		return null;
	}

	public TriangulatedIrregularNetwork subset(Envelope envelope) {
		// TODO Auto-generated method stub
		return null;
	}

}
