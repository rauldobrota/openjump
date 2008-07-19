package org.openjump.tin;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.PolygonShape;

import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;



/**
 * This is a simple utility class to enclose the array indices that are used
 * in conjunction with a Coordinate array and an array of TinFace to describe
 * the TriangleIrregularNetwork. 
 * 
 * Array indices are used instead of a more object oriented approach in order
 * to eliminate duplicated Coordinates.
 * 
 * @author Christopher DeMars
 *
 */
public class TinFace {
	
	// vertex array shared among all the TinFaces
	private Coordinate[] tinVertices;
	
	// array of TinFaces of which this Face is a member
	private TinFace[] tinTriangles;
	
	// indices that point to the three vertexes that compose this face.
	// indices are to tinVertices.
	private int vertex0;
	private int vertex1;
	private int vertex2;
	
	// indices that point to the neighboring triangles.
	// indices are to tinTriangles.
	private int neighbor0;
	private int neighbor1;
	private int neighbor2;
	
	// the index of this TinFace in the tinTriangles array.
	private int thisIndex;
	
	// the normal vector of this face, used for shading
	private Vector3d normalVector;
	
	boolean debug = false;
	
	/**
	 * Create a new TinFace.
	 * v0, v1, v2 should be in clockwise order.
	 * n0, n1, n2 should be equal to the bordering neighbors opposite of point
	 * 		v0, v1, and v2 respectfully.
	 * 
	 * @param idx	the index in <code>tfs</code> that points to this TinFace
	 * @param v0	the index in <code>vts</code> that points to the first 
	 * 				vertex of this triangle.
	 * @param v1	the index in <code>vts</code> that points to the second 
	 * 				vertex of this triangle.
	 * @param v2	the index in <code>vts</code> that points to the third 
	 * 				vertex of this triangle.
	 * @param n0	the neighboring TinFace opposite vertex v0
	 * @param n1	the neighboring TinFace opposite vertex v1
	 * @param n2	the neighboring TinFace opposite vertex v2
	 * @param vts	an array of points composing the TIN of which this face is
	 * 				a member of
	 * @param tfs	an array of TinFaces composing the TIN of which this face
	 * 				is a member of
	 */
	public TinFace (final int idx,
					final int v0, final int v1, final int v2,
					final int n0, final int n1, final int n2,
					final Coordinate[] vts, final TinFace[] tfs) {
		Assert.isTrue(idx>=0 && idx<tfs.length, 
				"TinFace constructor: array index out of bounds, idx = "+idx);
		Assert.isTrue(v0>=0 && v0<vts.length, 
				"TinFace constructor: array index out of bounds, v0 = "+v0);
		Assert.isTrue(v1>=0 && v1<vts.length, 
				"TinFace constructor: array index out of bounds, v1 = "+v1);
		Assert.isTrue(v2>=0 && v2<vts.length, 
				"TinFace constructor: array index out of bounds, v2 = "+v2);
		Assert.isTrue(n0>=-1 && n0<tfs.length, 
				"TinFace constructor: array index out of bounds, n0 = "+n0);
		Assert.isTrue(n1>=-1 && n1<tfs.length, 
				"TinFace constructor: array index out of bounds, n1 = "+n1);
		Assert.isTrue(n2>=-1 && n2<tfs.length, 
				"TinFace constructor: array index out of bounds, n2 = "+n2);
		this.setThisIndex(idx);
		this.setVertex0Index(v0);
		this.setVertex1Index(v1);
		this.setVertex2Index(v2);
		this.setNeighbor0Index(n0);
		this.setNeighbor1Index(n1);
		this.setNeighbor2Index(n2);
		this.tinVertices = vts;
		this.tinTriangles = tfs;
		
		this.normalVector = calculateNormal();
	}
	
	
	/**
	 * Calculates the normal vector of this TinFace
	 * 
	 * @return a vector normal to this TinFace
	 */
	protected Vector3d calculateNormal() {
		Vector3d vec0 = new Vector3d(getVertex0().x, getVertex0().y, getVertex0().z);
		Vector3d vec1 = new Vector3d(getVertex1().x, getVertex1().y, getVertex1().z);
		Vector3d vec2 = new Vector3d(getVertex2().x, getVertex2().y, getVertex2().z);
		
		Vector3d vec0to1 = new Vector3d();
		Vector3d vec1to2 = new Vector3d();
		vec0to1.sub(vec0, vec1);
		vec1to2.sub(vec1, vec2);
		
		if (debug) System.out.println("vec0to1: "+vec0to1+"\tvec1to2"+vec1to2);
		
		Vector3d normal = new Vector3d();
		normal.cross(vec0to1, vec1to2);
		normal.normalize();
		return normal;
	}
	
	
	/**
	 * Return the dot product of <code>lightVector</code> and this face's normal vector
	 * 
	 * @param lightVector	the vector representing the direction of lighting
	 * @return				the dot product of <code>lightVector</code> and 
	 * 						this face's normal vector
	 */
	public double getShadingDotProduct(final Vector3d lightVector) {
		if (debug) System.out.println("Normal vector = "+this.normalVector+"\tlightVector = "+lightVector);
		return this.normalVector.dot(lightVector);
	}
	

	/**
	 * Convert this TinFace to a human readable string.
	 * 
	 * @return a string describing each vertex and the index of each neighbor
	 */
	public String toString() {
		return("v0: " + getVertex0Index() + "\tv1: " + getVertex1Index() +
				"\tv2: " + getVertex2Index() + "\tn0: " + getNeighbor0Index() +
				"\tn1: " + getNeighbor0Index() + "\tn2: " + getNeighbor0Index());
	}
	
	/**
	 * Calculate and return the envelope that fully encloses this face. The
	 * minimum x and y pair are found by finding the smallest x and smallest y
	 * value among the three vertexes. The maximum x,y pair are similarly found
	 * by finding the largest x and largest y value among the three vertexes.
	 * 
	 * @return an envelope that fully encloses this face.
	 */
	public Envelope getEnvelope() {
		double x1, x2, y1, y2;
		
		x1 = getVertex0().x;
		x2 = getVertex0().x;
		if (x1 > getVertex1().x) x1 = getVertex1().x;
		if (x1 > getVertex2().x) x1 = getVertex2().x;
		if (x2 < getVertex1().x) x2 = getVertex1().x;
		if (x2 < getVertex2().x) x2 = getVertex2().x;
		
		y1 = getVertex0().y;
		y2 = getVertex0().y;
		if (y1 > getVertex1().y) y1 = getVertex1().y;
		if (y1 > getVertex2().y) y1 = getVertex2().y;
		if (y2 < getVertex1().y) y2 = getVertex1().y;
		if (y2 < getVertex2().y) y2 = getVertex2().y;
		
		return new Envelope (x1, x2, y1, y2);
	}
	
	
	/**
	 * Converts this TinFace to a java.awt.Shape by using a given viewport's
	 * Java2DConverter
	 * 
	 * @param viewport	The JUMP viewport that will be used  to convert this 
	 * 					TinFace model
	 * @return 			A Shape that represents this triangular TinFace within
	 * 					the given viewport
	 */
	public Shape toShape (final Viewport viewport) {
		Coordinate[] modelShell = { getVertex0(), getVertex1(), getVertex2(), getVertex0() };
		try {
			Coordinate[] viewShell = viewport.getJava2DConverter().toViewCoordinates(modelShell);
			PolygonShape shape = new PolygonShape();
			return shape.toPolygon(viewShell);
		} catch (Exception e) {
			Assert.shouldNeverReachHere("TinFace.toShape("+viewport.toString()+"): "+e.toString());
			return null;
		}
	}
	

	public Coordinate getVertex0() {
		return tinVertices[getVertex0Index()];
	}	
	public Coordinate getVertex1() {
		return tinVertices[getVertex1Index()];
	}	
	public Coordinate getVertex2() {
		return tinVertices[getVertex2Index()];
	}
	
	public TinFace getNeighbor0() {
		return tinTriangles[getNeighbor0Index()];
	}
	public TinFace getNeighbor1() {
		return tinTriangles[getNeighbor1Index()];
	}
	public TinFace getNeighbor2() {
		return tinTriangles[getNeighbor2Index()];
	}
	

	protected int getVertex0Index() {
		return vertex0;
	}
	protected int getVertex1Index() {
		return vertex1;
	}
	protected int getVertex2Index() {
		return vertex2;
	}
	
	protected int getNeighbor0Index() {
		return neighbor0;
	}
	protected int getNeighbor1Index() {
		return neighbor1;
	}
	protected int getNeighbor2Index() {
		return neighbor2;
	}
	protected int getThisIndex() {
		return thisIndex;
	}
	
	protected void setThisIndex(final int thisIndex) {
		this.thisIndex = thisIndex;
	}
	protected void setNeighbor2Index(final int neighbor2) {
		this.neighbor2 = neighbor2;
	}
	protected void setNeighbor1Index(final int neighbor1) {
		this.neighbor1 = neighbor1;
	}
	protected void setNeighbor0Index(final int neighbor0) {
		this.neighbor0 = neighbor0;
	}
	protected void setVertex2Index(final int vertex2) {
		this.vertex2 = vertex2;
	}
	protected void setVertex1Index(final int vertex1) {
		this.vertex1 = vertex1;
	}
	protected void setVertex0Index(final int vertex0) {
		this.vertex0 = vertex0;
	}
	 
}
