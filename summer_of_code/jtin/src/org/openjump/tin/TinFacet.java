package org.openjump.tin;

import java.awt.Shape;

import javax.vecmath.Vector3d;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.ui.Viewport;

public interface TinFacet {

	/**
	 * Return the dot product of <code>lightVector</code> and this face's normal vector
	 * 
	 * @param lightVector	the vector representing the direction of lighting
	 * @return				the dot product of <code>lightVector</code> and 
	 * 						this face's normal vector
	 */
	public abstract double getShadingDotProduct(final Vector3d lightVector);

	/**
	 * Return the dot product of <code>lightVector</code> and this face's normal vector
	 * 
	 * @param lightVector	the vector representing the direction of lighting
	 * @return				the dot product of <code>lightVector</code> and 
	 * 						this face's normal vector
	 */
	public abstract double getShadingDotProduct();

	/**
	 * Convert this TinFace to a human readable string.
	 * 
	 * @return a string describing each vertex and the index of each neighbor
	 */
	public abstract String toString();

	/**
	 * Calculate and return the envelope that fully encloses this face. The
	 * minimum x and y pair are found by finding the smallest x and smallest y
	 * value among the three vertexes. The maximum x,y pair are similarly found
	 * by finding the largest x and largest y value among the three vertexes.
	 * 
	 * @return an envelope that fully encloses this face.
	 */
	public abstract Envelope getEnvelope();

	public abstract boolean isLevel();

	/**
	 * Converts this TinFace to a java.awt.Shape by using a given viewport's
	 * Java2DConverter
	 * 
	 * @param viewport	The JUMP viewport that will be used  to convert this 
	 * 					TinFace model
	 * @return 			A Shape that represents this triangular TinFace within
	 * 					the given viewport
	 */
	public abstract Shape toShape(final Viewport viewport);

	public abstract double getZMin();

	public abstract double getZMax();

	public abstract Coordinate getVertex0();

	public abstract Coordinate getVertex1();

	public abstract Coordinate getVertex2();

	public abstract TinFacet getNeighbor0();

	public abstract TinFacet getNeighbor1();

	public abstract TinFacet getNeighbor2();

}