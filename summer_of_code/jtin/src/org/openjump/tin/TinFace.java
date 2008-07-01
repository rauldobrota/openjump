package org.openjump.tin;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.PolygonShape;

import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;



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
	
	/*
	// constructor that creates a new TinFace with default values of -1
	public TinFace() {
		this.setVertex0Index(-1);
		this.setVertex1Index(-1);
		this.setVertex2Index(-1);
		this.setNeighbor0Index(-1);
		this.setNeighbor1Index(-1);
		this.setNeighbor2Index(-1);
		this.setThisIndex(-1);
		this.tinVertices = null;
		this.tinTriangles = null;
	}
	*/
	
	// construct a new TinFace with the given indices
	public TinFace (int idx,
					int v0, int v1, int v2,
					int n0, int n1, int n2,
					Coordinate[] vts, TinFace[] tfs) {
		this.setThisIndex(idx);
		this.setVertex0Index(v0);
		this.setVertex1Index(v1);
		this.setVertex2Index(v2);
		this.setNeighbor0Index(n0);
		this.setNeighbor1Index(n1);
		this.setNeighbor2Index(n2);
		this.tinVertices = vts;
		this.tinTriangles = tfs;
		
	}
	
	
	public String toString() {
		return("v0: " + getVertex0Index() + "\tv1: " + getVertex1Index() + "\tv2: " + getVertex2Index() +
				"\tn0: " + getNeighbor0Index() + "\tn1: " + getNeighbor0Index() + "\tn2: " + getNeighbor0Index());
	}
	
	
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
	
	public Shape toShape (Viewport viewport) {
		Coordinate[] modelShell = { getVertex0(), getVertex1(), getVertex2(), getVertex0() };
		//Assert.isTrue(false, "TinFace.toShape: modelShell = "+modelShell.toString());
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
	
	protected void setThisIndex(int thisIndex) {
		this.thisIndex = thisIndex;
	}
	protected void setNeighbor2Index(int neighbor2) {
		this.neighbor2 = neighbor2;
	}
	protected void setNeighbor1Index(int neighbor1) {
		this.neighbor1 = neighbor1;
	}
	protected void setNeighbor0Index(int neighbor0) {
		this.neighbor0 = neighbor0;
	}
	protected void setVertex2Index(int vertex2) {
		this.vertex2 = vertex2;
	}
	protected void setVertex1Index(int vertex1) {
		this.vertex1 = vertex1;
	}
	protected void setVertex0Index(int vertex0) {
		this.vertex0 = vertex0;
	}
	 
}
