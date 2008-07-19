/**
 * 
 */
package org.openjump.tin;

import org.openjump.tin.io.JTFLayout;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;



/**
 * This class is the initial implementation of the TriangulatedIrregularNetwork
 * interface. As the name would imply, this class doesn't allow for changing
 * the internal data after creation of an instance.
 * 
 * @author Christopher DeMars
 */
public final class ImmutableTin implements TriangulatedIrregularNetwork {

	// list of vertices that compose the tin
	private Coordinate[] vertices;
	
	// the faces of the tin. Each TinFace is a collection of array indices.
	// Three indices point to the vertices array above, delineating the points
	// of the triangle. The other three indices point to the faceTable array
	// itself and create a connectedness map of the triangles.
	private TinFace[] faceTable;
	
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
	
	private boolean debug = false;

	
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
	public ImmutableTin (final Coordinate[] points, final int[][] triTable, 
						 final List<int[]> breaklines, final List<int[]> boundaries, 
						 final int spatialID) {
		
		// initialize global datastructures
		this.faceIndex = new STRtree();
		this.SRID = spatialID;
		this.vertices = (Coordinate[])points.clone();
		this.faceTable = triTableToFaceTable(triTable, this.vertices, this.faceIndex);
		
		// initialize then fill the breaklines and boundaries datastructures
		if (breaklines != null)	{		
			this.breaklines = new ArrayList<int[]>(breaklines.size());
			for (Iterator<int[]> itr = breaklines.iterator(); itr.hasNext(); ) {
				this.breaklines.add(itr.next().clone());
			}
		}	
		if (boundaries != null)	{		
			this.boundaries = new ArrayList<int[]>(boundaries.size());
			for (Iterator<int[]> itr = boundaries.iterator(); itr.hasNext(); ) {
				this.boundaries.add(itr.next().clone());
			}
		}
	}
	
	
	/**
	 * Given a face table in a JTFLayout format, make then return a TinFace
	 * 
	 * @param triTable		the triangle table in a JTFLayout format
	 * @param vertices		the vertices that the entries in the 
	 * 						<code>triTable</code> point to
	 * @param faceIndex		the index that each face should be placed
	 * @see JTFLayout
	 * @return				A <code>TinFace</code> array that describes the 
	 * 						same set of triangles as <code>triTable</code>
	 */
	protected TinFace[] triTableToFaceTable (final int[][] triTable, 
			final Coordinate[] vertices, SpatialIndex faceIndex) {
		
		TinFace[] faceTable = new TinFace[triTable.length];
		
		
		// fill the face table by converting the triangle table to an array of 
		// TinFace then enter that face into the spatial index
		for (int i=0; i < triTable.length; i++) {
			Assert.isTrue(triTable[i].length==JTFLayout.NUM_TRIANGLE_INT_FIELDS, 
						  "Malformed triangle table: doesn't contain "+
						  JTFLayout.NUM_TRIANGLE_INT_FIELDS+" fields for face #"
						  + i + ".");
			faceTable[i] = new TinFace (i, triTable[i][JTFLayout.TRITABLE_VERTEX_0], 
										triTable[i][JTFLayout.TRITABLE_VERTEX_1], 
										triTable[i][JTFLayout.TRITABLE_VERTEX_2],
										triTable[i][JTFLayout.TRITABLE_NEIGHBOR_0],
										triTable[i][JTFLayout.TRITABLE_NEIGHBOR_1],
										triTable[i][JTFLayout.TRITABLE_NEIGHBOR_2],
										vertices, faceTable);
			faceIndex.insert(faceTable[i].getEnvelope(), faceTable[i]);
		}
		
		return faceTable;
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
	public ImmutableTin (final List<TinFace> faceSubset, final List<int[]> breaklines, 
			final List<int[]> boundaries, final int SRID) {
		int idx;
		int faceSubsetSize = faceSubset.size() + 1;
		if (debug) System.out.println("faceSubsetSize = "+faceSubsetSize);
		this.faceIndex = new STRtree();
		this.SRID = SRID;
		
		// collect a map between the old points and the old point indexes, 
		// then use that map to get a set of old indexes used in this subset
		// and a set of coordinates. Make this.vertices equal to the set of
		// coordinates.
		HashMap<Coordinate, Integer> pointToOldPointIndexMap = new HashMap<Coordinate, Integer>(faceSubsetSize);
		for (TinFace face : faceSubset) {
			pointToOldPointIndexMap.put(face.getVertex0(), new Integer(face.getVertex0Index()));
			pointToOldPointIndexMap.put(face.getVertex1(), new Integer(face.getVertex1Index()));
			pointToOldPointIndexMap.put(face.getVertex2(), new Integer(face.getVertex2Index()));
		}
		HashSet<Coordinate> pointSet= new HashSet<Coordinate>(pointToOldPointIndexMap.keySet());
		HashSet<Integer> oldPointIndexSet = new HashSet<Integer>(pointToOldPointIndexMap.values());
		this.vertices = new Coordinate[pointSet.size()];
		this.vertices = pointSet.toArray(this.vertices);
		
		
		//create map between vertex and array index
		HashMap<Coordinate, Integer> pointToNewIndexMap = new HashMap<Coordinate, Integer>(faceSubsetSize);
		for (int i=0; i<this.vertices.length; i++) {
			pointToNewIndexMap.put(this.vertices[i], new Integer(i));
		}
		
		// create map between old point array index and new point array index
		Hashtable<Integer, Integer> oldPointIndexToNewIndexMap = new Hashtable<Integer, Integer>(faceSubsetSize);
		for (int i=0; i<this.vertices.length; i++) {
			oldPointIndexToNewIndexMap.put(pointToOldPointIndexMap.get(this.vertices[i]), new Integer(i));
		}
		
		// for each face is faceSubset, add a new face to this.faceTable with
		// the vertices set to the index in the new point array that is 
		// equivalent to the coordinates of the old face we're working with,
		// then add a maping between the old face and the new face indexes.
		HashMap<Integer, Integer> oldFaceIndexToNewIndexMap = new HashMap<Integer, Integer>(faceSubsetSize);
		this.faceTable = new TinFace[faceSubsetSize];
		idx=0;
		for (TinFace face : faceSubset) {
			oldFaceIndexToNewIndexMap.put(new Integer(face.getThisIndex()), new Integer(idx++));
		}
		
		// for each face in faceSubset, set the neighbors of the equivalent
		// face in the new faceTable to be the same as those in the face we're
		// working with. Then add the new face to the spatial face index.
		for (TinFace face : faceSubset) {
			int newFaceIndex = oldFaceIndexToNewIndexMap.get(face.getThisIndex()).intValue();
			int newVertex0Index = pointToNewIndexMap.get(face.getVertex0()).intValue();
			int newVertex1Index = pointToNewIndexMap.get(face.getVertex1()).intValue();
			int newVertex2Index = pointToNewIndexMap.get(face.getVertex2()).intValue();
			int newNeighbor0Index = -1, newNeighbor1Index = -1, newNeighbor2Index = -1;
			if (oldFaceIndexToNewIndexMap.containsKey(face.getNeighbor0Index()))
				newNeighbor0Index = oldFaceIndexToNewIndexMap.get(face.getNeighbor0Index()).intValue();
			if (oldFaceIndexToNewIndexMap.containsKey(face.getNeighbor1Index()))
				newNeighbor1Index = oldFaceIndexToNewIndexMap.get(face.getNeighbor1Index()).intValue();
			if (oldFaceIndexToNewIndexMap.containsKey(face.getNeighbor2Index()))
				newNeighbor2Index = oldFaceIndexToNewIndexMap.get(face.getNeighbor2Index()).intValue();
			
			if (debug) System.out.println("idx = "+newFaceIndex+
					"\tv0 = "+newVertex0Index+"\tv1 = "+newVertex1Index+"\tv2 = "+newVertex2Index+
					"\tn0 = "+newNeighbor0Index+"\tn1 = "+newNeighbor1Index+"\tn2 = "+newNeighbor2Index+
					"\tvertices.length = "+this.vertices.length+"\tthis.faceTable.length = "+this.faceTable.length);
			
			this.faceTable[newFaceIndex] = new TinFace (idx, 
					newVertex0Index, newVertex1Index, newVertex2Index, 
					newNeighbor0Index, newNeighbor1Index, newNeighbor2Index,
					this.vertices, this.faceTable);
			
			faceIndex.insert(faceTable[newFaceIndex].getEnvelope(), faceTable[newFaceIndex]);
		}
		
		// get subset of breaklines and boundaries that are within this envelope.
		// If a line goes in, out, and back into the envelope, it will be divided 
		// into multiple lines that are each contained within the envelope
		this.breaklines = lineSubset(breaklines, oldPointIndexSet, oldPointIndexToNewIndexMap);
		this.boundaries = lineSubset(boundaries, oldPointIndexSet, oldPointIndexToNewIndexMap);
	}
	
	
	/**
	 * Returns an ImmutableTin that contains a copy of this tin containing all 
	 * the TinFaces who's envelope intersects the given envelope. The returned 
	 * TIN will have partial faces and possibly full faces that lie outside the
	 * given envelope.
	 * 
	 * @param envelope	The bounding box delinating which TinFaces should be
	 * 					included in the returned TIN.
	 * @return			an ImmutableTin that contains all the faces within the
	 * 					given envelope.
	 */
	public TriangulatedIrregularNetwork subset (final Envelope envelope) {
		List<TinFace> faceSubset = getSubsetTriangles(envelope);
		int faceSubsetSize = faceSubset.size();

		// collect the indexes of all the points and faces that are used in 
		// this subset
		HashSet<Integer> pointSet= new HashSet<Integer>(faceSubsetSize);
		HashSet<Integer> faceSet= new HashSet<Integer>(faceSubsetSize);
		for (TinFace face : faceSubset) {
			pointSet.add(new Integer(face.getVertex0Index()));
			pointSet.add(new Integer(face.getVertex1Index()));
			pointSet.add(new Integer(face.getVertex2Index()));
			faceSet.add(new Integer(face.getThisIndex()));
		}
				
		// convert pointSet and faceSet to arrays
		Integer[] pointArray = new Integer[pointSet.size()];
		pointSet.toArray(pointArray);
		Integer[] faceArray = new Integer[faceSet.size()];
		faceSet.toArray(faceArray);
		
		// create new point array for subset tin & create map between indexes 
		// of this.vertices and points
		Coordinate[] points = new Coordinate[pointArray.length];
		Hashtable<Integer, Integer> pointsHash = new Hashtable<Integer, Integer>(faceSubsetSize);
		for (int i=0; i<pointArray.length; i++) {
			points[i] = this.vertices[pointArray[i].intValue()];
			pointsHash.put(pointArray[i], new Integer(i));
		}
		
		// create map between faceAray and this.triTable indexes
		Hashtable<Integer, Integer> facesHash = new Hashtable<Integer, Integer>(faceSubsetSize);
		for (int i=0; i<faceArray.length; i++) {
			facesHash.put(faceArray[i], new Integer(i));
		}
		
		// create triangle table for this subset tin
		int[][] triTable = new int[faceSet.size()][JTFLayout.NUM_TRIANGLE_INT_FIELDS];
		for (int i=0; i<faceSet.size(); i++) {
			int faceIndex = faceArray[i].intValue();
			TinFace face = this.faceTable[faceIndex];
			int vertex0SubsetIdx = pointsHash.get(new Integer(face.getVertex0Index())).intValue();
			int vertex1SubsetIdx = pointsHash.get(new Integer(face.getVertex1Index())).intValue();
			int vertex2SubsetIdx = pointsHash.get(new Integer(face.getVertex2Index())).intValue();
			int neighbor0SubsetIdx = -1, neighbor1SubsetIdx = -1, neighbor2SubsetIdx = -1;
			Integer n0 = new Integer(face.getNeighbor0Index());
			Integer n1 = new Integer(face.getNeighbor1Index());
			Integer n2 = new Integer(face.getNeighbor2Index());
			if (faceSet.contains(n0))
				neighbor0SubsetIdx = facesHash.get(n0).intValue();
			if (faceSet.contains(n1))
				neighbor1SubsetIdx = facesHash.get(n1).intValue();
			if (faceSet.contains(n2))
				neighbor2SubsetIdx = facesHash.get(n2).intValue();
			
			triTable[i][JTFLayout.TRITABLE_VERTEX_0] = vertex0SubsetIdx;
			triTable[i][JTFLayout.TRITABLE_VERTEX_1] = vertex1SubsetIdx;
			triTable[i][JTFLayout.TRITABLE_VERTEX_2] = vertex2SubsetIdx;
			triTable[i][JTFLayout.TRITABLE_NEIGHBOR_0] = neighbor0SubsetIdx;
			triTable[i][JTFLayout.TRITABLE_NEIGHBOR_1] = neighbor1SubsetIdx;
			triTable[i][JTFLayout.TRITABLE_NEIGHBOR_2] = neighbor2SubsetIdx;
		}
		
		// get subset of breaklines and boundaries that are within this envelope.
		// If a line goes in, out, and back into the envelope, it will be divided 
		// into multiple lines that are each contained within the envelope
		LinkedList<int[]> bk = lineSubset(this.breaklines, pointSet, pointsHash);
		LinkedList<int[]> bd = lineSubset(this.boundaries, pointSet, pointsHash);

		return new ImmutableTin(points, triTable, bk, bd, this.SRID);
	}
	
	
	/**
	 * Utility method for ImmutableTin.subset(). Given a list of lines,
	 * each represented by an array of indicies to a coordinate array of
	 * points, a set of point indicies that make up the subset, and a 
	 * hashtable that links the index of the parent tin's point array to
	 * the index of that point within the subset tin's point array.
	 * 
	 * @param lineList		list of arrays, each containing indicies to an
	 * 						external array of coordinates
	 * @param pointSet		set of points that are contained part of the subset
	 * @param pointHash		hashtable that translates the index of a point from
	 * 						the array representing the parent's points to the
	 * 						index of the same point in the subset's point array
	 */
	protected LinkedList<int[]> lineSubset (final List<int[]> lineList, 
			final HashSet<Integer> pointSet, 
			final Hashtable<Integer, Integer> pointHash) {
		
		if (lineList == null) return null;
		
		LinkedList<int[]> returnLine = new LinkedList<int[]>();
		for (int[] line : lineList) {
			LinkedList<Integer> tmpLine = new LinkedList<Integer>();
			// for each point in the current line, see if it is within the
			// subset's point array.
			// If it is add that point to the current temporary line.
			// If not, see if the temporary line contains more than a pair of 
			// points, if it does, write out that line to the line list for 
			// this subset
			for (int i=0; i<line.length; i++) {
				Integer tmpPointIdx = new Integer(line[i]);
				if (pointSet.contains(tmpPointIdx))
					tmpLine.add(pointHash.get(tmpPointIdx));
				else if (tmpLine.size() > 1) {
					int[] nextLine = new int[tmpLine.size()];
					int j=0;
					for (Integer pt : tmpLine) {
						nextLine[j] = pt.intValue();
						j++;
					}
					returnLine.add(nextLine);
					tmpLine.clear();
				}
			}
		}
		return returnLine;
	}
	
	
	/**
	 * Returns a list of the TinFaces who's envelope lie within the given 
	 * envelope. The returned faces are not copies.
	 * 
	 * @param envelope	the bounding box that the faces are tested against
	 * @return			a list of TinFaces that have an envelope that overlap
	 * 					with the given envelope.
	 */
	public List<TinFace> getSubsetTriangles(final Envelope envelope) {
		return (List<TinFace>)faceIndex.query(envelope);
	}
	
	/**
	 * Returns a list of the TinFaces that have all three vertexes within the
	 * envelope. The returned faces are not copies.
	 * 
	 * @param envelope	the bounding box that the faces are tested against
	 * @return			a list of TinFaces that have an envelope that overlap
	 * 					with the given envelope.
	 */
	public List<TinFace> getStrictSubsetTriangles(final Envelope envelope) {
		List<TinFace> subset = getSubsetTriangles(envelope);
		LinkedList<TinFace> returnSubset = new LinkedList<TinFace>();
		
		// make sure each face is totally within the given envelope
		for (TinFace face : subset) {
			if (envelope.intersects(face.getVertex0()) &&
					envelope.intersects(face.getVertex1()) &&
					envelope.intersects(face.getVertex2()))
				returnSubset.add(face);
		}
		return returnSubset;
	}
	
	/**
	 * Returns the number of vertices/points that make up this TIN.
	 * 
	 * @return	number of points in the TIN
	 */
	public int getNumVertices() {
		return vertices.length;
	}
	
	/**
	 * Returns the OpenJUMP compatible spatial reference ID of this TIN.
	 * 
	 *  @return This tin's spatial reference identification
	 */
	public int getSRID() {
		return SRID;
	}
	
	/**
	 * Returns the number of triangular faces that make up this TIN.
	 * 
	 * @return a count of triangular faces that make up this TIN.
	 */
	public int getNumTriangles() {
		return faceTable.length;
	}
	
	/**
	 * Returns the number of breaklines in this TIN.
	 * 
	 * @return a count of breaklines present in this TIN.
	 */
	public int getNumBreaklines() {
		return breaklines.size();
	}
	
	/**
	 * Returns the number of boundaries in this TIN.
	 * 
	 * @return a count of boundaries present in this TIN.
	 */
	public int getNumBoundaries() {
		return boundaries.size();
	}
	
	/**
	 * Returns the x,y,z coordinate of point number N of this TIN
	 * 
	 * @param n		the index of the requested face between 0..getNumVerticies()
	 * @return		the x,y,z coordinate of the requested point number
	 */
	public Coordinate getVertexN (final int n) {
		Assert.isTrue(n>=0 && n<getNumVertices(), 
				"ImmutableTin.getVertexN: index out of bounds, N="+n);
		return vertices[n];
	}

	/**
	 * Return an array containing all the points of this TIN. The returned
	 * value is not a copy.
	 * 
	 * @return all the vertices that compose this TIN.
	 */
	public Coordinate[] getVertices() {
		return vertices;
	}

	/**
	 * Creates an array compatible with JTFLayout that describes the triangular
	 * TIN face #N
	 * 
	 * @param n		the face number to be translated into a triangle array, 
	 * 				should be between 0..getNumTriangles()
	 * @return		a JTFLayout compatible triangle array
	 * @see			JTFLayout
	 */
	public int[] getTriangleArrayN (final int n) {
		Assert.isTrue(n>=0 && n<getNumTriangles(),
				"ImmutableTin.getTriangleArrayN: index out of bounds, N="+n);
		return new int[] { faceTable[n].getVertex0Index(),
				faceTable[n].getVertex1Index(),
				faceTable[n].getVertex2Index(),
				faceTable[n].getNeighbor0Index(),
				faceTable[n].getNeighbor1Index(),
				faceTable[n].getNeighbor2Index() };			
	}
	
	/**
	 * Returns the TinFace that describes face #N. Returned value is not a copy
	 * 
	 * @param n		the face to be returned, should be between 
	 * 				0..getNumTriangles()
	 * @return		the TinFace that represents face #N in the TIN.
	 * @see			TinFace
	 */
	public TinFace getTriangleN (final int n) {
		Assert.isTrue(n>=0 && n<getNumTriangles(),
				"ImmutableTin.getTriangleArrayN: index out of bounds, N="+n);
		return this.faceTable[n];
	}
	
	/**
	 * Returns a list of int arrays, each representing an ordered list of 
	 * indices to the point array, that each describe a single breakline.
	 * 
	 * @return a list of array indices that each represent a breakline
	 */
	public List<int[]> getBreaklines() {
		return breaklines;
	}
	
	/**
	 * Returns a list of int arrays, each representing an ordered list of 
	 * indices to the point array, that each describe a single boundary.
	 * 
	 * @return a list of array indices that each represent a boundary
	 */
	public List<int[]> getBoundaries() {
		return boundaries;
	}
	
	/**
	 * Translates the breaklines into a JTS MultiLineString collection.
	 * 
	 * @return a JTS MultiLineString representation of the breaklines.
	 */
	public MultiLineString getBreaklinesAsMultiLineString() {
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(), this.SRID);
		return lineArrayListToMultiLineString(this.breaklines, gf);
	}

	
	/**
	 * Translates the boundaries into a JTS MultiLineString collection.
	 * 
	 * @return a JTS MultiLineString representation of the boundaries.
	 */
	public MultiLineString getBoundariesAsMultiLineString() {
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(), this.SRID);
		return lineArrayListToMultiLineString(this.boundaries, gf);
	}
	
	/**
	 * Utility method for getBreaklinesAsMultiLineString and 
	 * getBoundariesAsMultiLineString. Given a list of lines represented as an
	 * array of indices to a point array and a GeometryFactory, return a 
	 * JTS MultiLineString equivlant to the given lines.
	 * 
	 * @param lines		A list of arrays of indices to this TIN's point array
	 * @param gf		The JTS GeometryFactory that the MultiLineString will
	 * 					be made with
	 * @return			A JTS MultiLineString that is equivlant to the given
	 * 					lines list 
	 */
	protected MultiLineString lineArrayListToMultiLineString (final List<int[]> lines, 
			final GeometryFactory gf) {
		LineString[] linesArray = new LineString[lines.size()];
		for (int i=0; i<lines.size(); i++) {
			linesArray[i] = gf.createLineString(indexArrayToCoordinateArray(lines.get(i), this.vertices));
		}
		return gf.createMultiLineString(linesArray);
	}
	
	/**
	 * Utility method for lineArrayListToMultiLineString. Given a line that 
	 * is represented by an array of index values pointing to coordinateArray,
	 * return a coordinate array that is equal to the line with each index
	 * replaced by the Coordinate it points to.
	 * 
	 * @param indexArray		An array of indices of coordinateArray
	 * @param coordinateArray	Array that each element in the indexArray
	 * 							points to
	 * @return					A coordinate array where each element i is the
	 * 							coordinate in coordinateArray pointed to by
	 * 							indexArray[i].
	 */
	protected Coordinate[] indexArrayToCoordinateArray(final int[] indexArray, 
			final Coordinate[] coordinateArray) {
		Coordinate[] coords = new Coordinate[indexArray.length];
		for (int i=0; i<indexArray.length; i++) {
			coords[i] = coordinateArray[indexArray[i]];
		}
		return coords;
	}

	/**
	 * Convert this TIN to a human readable string.
	 * 
	 * @return a human readable string that describes this TIN.
	 */
	public String toString(){
		StringBuffer verticesString = new StringBuffer("\n\nVertices:\n");
		StringBuffer faceTableString = new StringBuffer("\n\nTriangle Table:\n");
		StringBuffer boundariesString = new StringBuffer("\n\nBoundaries:\n");
		StringBuffer breaklinesString = new StringBuffer("\n\nBreaklines:\n");
		
		for (int i=0; i<vertices.length; i++) {
			verticesString.append(vertices[i].toString() + "\n");
		}
		
		for (int i=0; i<faceTable.length; i++) {
			faceTableString.append(faceTable[i].toString() + "\n");
		}
		
		for (int[] line : boundaries) {
			for (int i=0; i< line.length; i++)
				boundariesString.append(line[i] + " ");
			boundariesString.append("\n");
		}
		
		for (int[] line : breaklines) {
			for (int i=0; i< line.length; i++)
				breaklinesString.append(line[i] + " ");
			breaklinesString.append("\n");
		}
		
		return ("Spatial ID: "+ this.SRID + 
				verticesString.toString() + 
				faceTableString.toString() + 
				boundariesString.toString() +
				breaklinesString.toString());
	}


}


