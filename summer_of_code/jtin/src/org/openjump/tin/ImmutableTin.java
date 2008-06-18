/**
 * 
 */
package org.openjump.tin;

import org.openjump.tin.io.JTFLayout;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
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
	
	// constants used in the formatting of passed triTables


	
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
		return new int[] { faceTable[n].vertex0,
				faceTable[n].vertex1,
				faceTable[n].vertex2,
				faceTable[n].neighbor0,
				faceTable[n].neighbor1,
				faceTable[n].neighbor2 };			
	}
	
	public List<int[]> getBreaklines() {
		return breaklines;
	}
	
	public List<int[]> getBoundaries() {
		return boundaries;
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


