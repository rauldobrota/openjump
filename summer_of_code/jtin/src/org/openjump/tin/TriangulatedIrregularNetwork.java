/**
 * 
 */
package org.openjump.tin;

import java.util.List;

import org.openjump.tin.io.JTFLayout;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * @author paradox
 *
 */
public interface TriangulatedIrregularNetwork {
	
	/**
	 * Returns the number of vertices/points that make up this TIN.
	 * 
	 * @return	number of points in the TIN
	 */
	public int getNumVertices();
	
	/**
	 * Returns the number of triangular faces that make up this TIN.
	 * 
	 * @return a count of triangular faces that make up this TIN.
	 */
	public int getNumTriangles();
	
	/**
	 * Returns the OpenJUMP compatible spatial reference ID of this TIN.
	 * 
	 *  @return This tin's spatial reference identification
	 */
	public int getSRID();
	
	/**
	 * Returns the number of breaklines in this TIN.
	 * 
	 * @return a count of breaklines present in this TIN.
	 */
	public int getNumBreaklines();
	
	/**
	 * Returns the number of boundaries in this TIN.
	 * 
	 * @return a count of boundaries present in this TIN.
	 */
	public int getNumBoundaries();
	
	/**
	 * Returns the x,y,z coordinate of point number N of this TIN
	 * 
	 * @param n		the index of the requested face between 0..getNumVerticies()
	 * @return		the x,y,z coordinate of the requested point number
	 */
	public Coordinate getVertexN (int n);
	
	/**
	 * Return an array containing all the points of this TIN. The returned
	 * value is not a copy.
	 * 
	 * @return all the vertices that compose this TIN.
	 */
	public Coordinate[] getVertices();
	
	/**
	 * Creates an array compatible with JTFLayout that describes the triangular
	 * TIN face #N
	 * 
	 * @param n		the face number to be translated into a triangle array, 
	 * 				should be between 0..getNumTriangles()
	 * @return		a JTFLayout compatible triangle array
	 * @see			JTFLayout
	 */
	public int[] getTriangleArrayN (int n);
	
	/**
	 * Returns a list of int arrays, each representing an ordered list of 
	 * indices to the point array, that each describe a single breakline.
	 * 
	 * @return a list of array indices that each represent a breakline
	 */
	public List<int[]> getBreaklines();
	
	/**
	 * Returns a list of int arrays, each representing an ordered list of 
	 * indices to the point array, that each describe a single boundary.
	 * 
	 * @return a list of array indices that each represent a boundary
	 */
	public List<int[]> getBoundaries();
	
	/**
	 * Translates the breaklines into a JTS MultiLineString collection.
	 * 
	 * @return a JTS MultiLineString representation of the breaklines.
	 */
	public MultiLineString getBreaklinesAsMultiLineString();
	
	/**
	 * Translates the boundaries into a JTS MultiLineString collection.
	 * 
	 * @return a JTS MultiLineString representation of the boundaries.
	 */
	public MultiLineString getBoundariesAsMultiLineString();
	
	/**
	 * Returns the TinFace that describes face #N. Returned value is not a copy
	 * 
	 * @param n		the face to be returned, should be between 
	 * 				0..getNumTriangles()
	 * @return		the TinFace that represents face #N in the TIN.
	 * @see			ImmutableArrayTinFacet
	 */
	public TinFacet getTriangleN(int n);
	
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
	public TriangulatedIrregularNetwork subset(Envelope envelope);
	
	/**
	 * Returns a list of the TinFaces who's envelope lie within the given 
	 * envelope. The returned faces are not copies.
	 * 
	 * @param envelope	the bounding box that the faces are tested against
	 * @return			a list of TinFaces that have an envelope that overlap
	 * 					with the given envelope.
	 */
	public List<ImmutableArrayTinFacet> getSubsetTriangles(Envelope envelope);
	
	/**
	 * Returns a list of the TinFaces that have all three vertexes within the
	 * envelope. The returned faces are not copies.
	 * 
	 * @param envelope	the bounding box that the faces are tested against
	 * @return			a list of TinFaces that have an envelope that overlap
	 * 					with the given envelope.
	 */
	public List<ImmutableArrayTinFacet> getStrictSubsetTriangles(Envelope envelope);
	
	/**
	 * Returns a list of the TinFaces that intersect the given value along
	 * their z axis.
	 * 
	 * @param z		the height band that all returned triangles will intersect
	 * @return		a list of TinFaces that intersect the given height
	 */
	public List<ImmutableArrayTinFacet> getTrianglesAtHeight(final double z);
	
	/**
	 * Returns a list of the TinFaces that lie within the given height range
	 * at some point
	 * 
	 * @param z1	one end of the height band that all returned triangles will intersect
	 * @param z2	the other end the height band that all returned triangles will intersect
	 * @return		a list of TinFaces that intersect the given height range
	 */
	public List<ImmutableArrayTinFacet> getTrianglesAtHeight(final double z1, final double z2);
	
	/**
	 * Returns a collection of LineStrings that represents all the contour 
	 * lines of this TIN at the given height. The lines are equivalent to
	 * the intersection of the surface this TIN models with the (x, y, height)
	 * plane.
	 *  
	 * @param height	the height of the intersection plane
	 * @return			all contour lines at the given height packaged as a 
	 * 					MultiLineString. Closed contour lines should be 
	 * 					represented as LinearRings.
	 */
	public Geometry getContourLinesAtHeight(final double height);
	
	/**
	 * Returns a Coordinate with x, y, and z all set to the maximum value of 
	 * each among this TIN's point set.
	 * 
	 * @return 	a Coordinate representing the maximum corner of a bounding cube
	 * 			that encompasses this TIN's point set 
	 */
	public Coordinate getMaxBoundingCoordinate();
	
	/**
	 * Returns a Coordinate with x, y, and z all set to the minimum value of 
	 * each among this TIN's point set.
	 * 
	 * @return 	a Coordinate representing the minimum corner of a bounding cube
	 * 			that encompasses this TIN's point set 
	 */
	public Coordinate getMinBoundingCoordinate();
}
