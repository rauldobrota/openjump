package org.openjump.tin;

import com.vividsolutions.jts.geom.*;

/**
 * This is a simple utility class to enclose the array indices that are used
 * in conjunction with a Coordinate array and an array of TinFace to describe
 * the TriangleIrregularNetwork. 
 * 
 * Array indices are used instead of a more object oriented approach in order
 * to eliminate duplicated Coordinates.
 * 
 * @author paradox
 *
 */
public class TinFace {
	
	// vertex array shared among all the TinFaces
	public Coordinate[] tinVertices;
	
	// array of TinFaces of which this Face is a member
	public TinFace[] tinTriangles;
	
	// indices that point to the three vertexes that compose this face.
	// indices are to tinVertices.
	public int vertex0;
	public int vertex1;
	public int vertex2;
	
	// indices that point to the neighboring triangles.
	// indices are to tinTriangles.
	public int neighbor0;
	public int neighbor1;
	public int neighbor2;
	
	// the index of this TinFace in the tinTriangles array.
	public int thisIndex;
	
	// constructor that creates a new TinFace with default values of -1
	public TinFace() {
		this.vertex0 = -1;
		this.vertex1 = -1;
		this.vertex2 = -1;
		this.neighbor0 = -1;
		this.neighbor1 = -1;
		this.neighbor2 = -1;
		this.thisIndex = -1;
		this.tinVertices = null;
		this.tinTriangles = null;
	}
	
	// construct a new TinFace with the given indices
	public TinFace (int idx,
					int v0, int v1, int v2,
					int n0, int n1, int n2,
					Coordinate[] vts, TinFace[] tfs) {
		this.thisIndex = idx;
		this.vertex0 = v0;
		this.vertex1 = v1;
		this.vertex2 = v2;
		this.neighbor0 = n0;
		this.neighbor1 = n1;
		this.neighbor2 = n2;
		this.tinVertices = vts;
		this.tinTriangles = tfs;
		
	}
	
	public String toString() {
		return("v0: " + vertex0 + "\tv1: " + vertex1 + "\tv2: " + vertex2 +
				"\tn0: " + neighbor0 + "\tn1: " + neighbor0 + "\tn2: " + neighbor0);
	}
	

}
