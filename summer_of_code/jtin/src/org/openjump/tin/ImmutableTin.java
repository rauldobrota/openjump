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
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;



/**
 * @author paradox
 *
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
	private List<int[]> breaklines;
	private List<int[]> boundaries;
	
	private int SRID;
	
	private STRtree faceIndex;

	
	/**
	 * 
	 * @param points
	 * @param triTable
	 * @param bl
	 * @param bd
	 * @param spatialID
	 */
	public ImmutableTin (Coordinate[] points, int[][] triTable, 
						 List<int[]> bl, List<int[]> bd, int spatialID) {
		faceIndex = new STRtree();
		this.SRID = spatialID;
		this.vertices = (Coordinate[])points.clone();
		this.faceTable = new TinFace[triTable.length];
		
		for (int i=0; i < triTable.length; i++) {
			Assert.isTrue(triTable[i].length==JTFLayout.NUM_TRIANGLE_INT_FIELDS, 
						  "Malformed triangle table: doesn't contain "+JTFLayout.NUM_TRIANGLE_INT_FIELDS+" fields for face #" + i + ".");
			this.faceTable[i] = new TinFace (i, triTable[i][JTFLayout.TRITABLE_VERTEX_0], 
										triTable[i][JTFLayout.TRITABLE_VERTEX_1], 
										triTable[i][JTFLayout.TRITABLE_VERTEX_2],
										triTable[i][JTFLayout.TRITABLE_NEIGHBOR_0],
										triTable[i][JTFLayout.TRITABLE_NEIGHBOR_1],
										triTable[i][JTFLayout.TRITABLE_NEIGHBOR_2],
										vertices, faceTable);
			faceIndex.insert(this.faceTable[i].getEnvelope(), this.faceTable[i]);
		}
		
		this.breaklines = new ArrayList<int[]>(bl.size());
		this.boundaries = new ArrayList<int[]>(bd.size());
		
		int[] tmp;
		for (Iterator<int[]> itr = bl.iterator(); itr.hasNext(); ) {
			tmp = itr.next();
			this.breaklines.add((int[])tmp.clone());
		}
		
		for (Iterator<int[]> itr = bd.iterator(); itr.hasNext(); ) {
			this.boundaries.add(itr.next().clone());
		}
	}
	
	public TriangulatedIrregularNetwork subset (Envelope envelope) {
		List<TinFace> faceSubset = getSubsetTriangles(envelope);
		int faceSubsetSize = faceSubset.size();

		// collect the indexes of all the points and faces that are used in this subset
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
		
		// create new point array for subset tin & create map between indexes of this.vertices and points
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
		
		// get subset of breaklines that are within this envelope by going 
		// through each breakline and seeing if the current point in the line
		// is contained in this subset. If it is, translate that point to the
		// subset point array and add it to a temp buffer.
		LinkedList<int[]> bk = lineSubset(this.breaklines, pointSet, pointsHash);
		LinkedList<int[]> bd = lineSubset(this.boundaries, pointSet, pointsHash);

		return new ImmutableTin(points, triTable, bk, bd, this.SRID);
	}
	
	private LinkedList<int[]> lineSubset (List<int[]> lineList, HashSet<Integer> pointSet, Hashtable<Integer, Integer> pointHash) {
		LinkedList<int[]> returnLine = new LinkedList<int[]>();
		for (int[] line : lineList) {
			LinkedList<Integer> tmpLine = new LinkedList<Integer>();
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
	
	
	public List<TinFace> getSubsetTriangles(Envelope envelope) {
		return (List<TinFace>)faceIndex.query(envelope);
	}
	
	public int getNumVertices() {
		return vertices.length;
	}
	
	public int getSRID() {
		return SRID;
	}
	
	public int getNumTriangles() {
		return faceTable.length;
	}
	
	public int getNumBreaklines() {
		return breaklines.size();
	}
	
	public int getNumBoundaries() {
		return boundaries.size();
	}
	
	public Coordinate getVertexN (int n) {
		return vertices[n];
	}

	public Coordinate[] getVertices() {
		return vertices;
	}

	public int[] getTriangleArrayN (int n) {
		return new int[] { faceTable[n].getVertex0Index(),
				faceTable[n].getVertex1Index(),
				faceTable[n].getVertex2Index(),
				faceTable[n].getNeighbor0Index(),
				faceTable[n].getNeighbor1Index(),
				faceTable[n].getNeighbor2Index() };			
	}
	
	public TinFace getTriangleN (int n) {
		return this.faceTable[n];
	}
	
	public List<int[]> getBreaklines() {
		return breaklines;
	}
	
	public List<int[]> getBoundaries() {
		return boundaries;
	}
	
	public MultiLineString getBreaklinesAsMultiLineString() {
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(), this.SRID);
		return lineArrayListToMultiLineString(this.breaklines, gf);
	}
	
	public MultiLineString getBoundariesAsMultiLineString() {
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(), this.SRID);
		return lineArrayListToMultiLineString(this.boundaries, gf);
	}
	
	protected MultiLineString lineArrayListToMultiLineString (List<int[]> lines, GeometryFactory gf) {
		LineString[] linesArray = new LineString[lines.size()];
		for (int i=0; i<lines.size(); i++) {
			linesArray[i] = gf.createLineString(indexArrayToCoordinateArray(lines.get(i)));
		}
		return gf.createMultiLineString(linesArray);
	}
	
	protected Coordinate[] indexArrayToCoordinateArray(int[] indexArray) {
		Coordinate[] coords = new Coordinate[indexArray.length];
		for (int i=0; i<indexArray.length; i++) {
			coords[i] = this.vertices[indexArray[i]];
		}
		return coords;
	}

	
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
		
		return ("Spatial ID: "+ SRID + 
				verticesString.toString() + 
				faceTableString.toString() + 
				boundariesString.toString() +
				breaklinesString.toString());
	}


}


