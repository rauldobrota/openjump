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
			Assert.isTrue(triTable[i].length==6, 
						  "Malformed triangle table: doesn't contain six fields for face #" + i + ".");
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
		return this;
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


