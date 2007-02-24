/*
 * MatrixTools.java
 * ----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.util;

import org.w3c.dom.svg.SVGMatrix;

import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.ParseException;

import java.awt.geom.AffineTransform;

/**
 * class providing static helper methods to ease
 * the handling of affine transform (3x3 matrices)
 * when interacting with SVG DOM.
 */
public class MatrixTools
{
	/**
	 * class representing a decomposition of a given
	 * affine transform.
	 */
	public static final class Decomposition 
	{
		/**
		 * the translation in x direction
		 */
		public double tx;
		/**
		 * the translation in y direction
		 */
		public double ty;
		/**
		 * the rotation in radians
		 */
		public double ro;
		/**
		 * the scale in x direction
		 */
		public double scx;
		/**
		 * the scale in y direction
		 */
		public double scy;
		/**
		 * the shear (skew) in x direction
		 */
		public double skx;
		/**
		 * the shear (skew) in y direction
		 */
		public double sky; // skew y
		
		/**
		 * Creates a decomposition for a given SVGMatrix
		 */
		public Decomposition(SVGMatrix matrix) {
			this(
				matrix.getA(),
				matrix.getB(),
				matrix.getC(),
				matrix.getD(),
				matrix.getE(),
				matrix.getF());
		}

		/**
		 * extracts the 6 relevant matrix elements out of a given AffineTransform.
		 * @param xform the AffineTransform
		 * @return a six element array with a, b, c, d, e, f (see SVG specs)
		 */
		public static double [] getMatrix(AffineTransform xform) {
			double [] m = new double[6];
			xform.getMatrix(m);
			return m;
		}

		/**
		 * Creates a decomposition from a given AffineTransform
		 * @param xform the AffineTransform
		 */
		public Decomposition(AffineTransform xform) {
			this(getMatrix(xform));
		}

		/**
		 * Creates a decomposition from a given six element array
		 * @param m the array
		 */
		public Decomposition(double [] m) {
			this(m[0], m[1], m[2], m[3], m[4], m[5]);
		}

		/**
		 * Creates a decomposition from a given set of matrix coeffs a to f.
		 * @param a the a coefficient
		 * @param b the b coefficient
		 * @param c the c coefficient
		 * @param d the d coefficient
		 * @param e the e coefficient
		 * @param f the f coefficient
		 */
		public Decomposition(
			double a, double b,
			double c, double d,
			double e, double f
		) {
			double const1 = a*a + b*b;
			double const2 = Math.sqrt(1d/const1);

			if (a < 0d || b < 0d) const2 = -const2;

			double tanskx = (a*c + b*d)/const1;

			skx = Math.atan(tanskx);
			sky = 0d;

			scx = const1*const2;
			scy = (a*d - b*c)*const2;

			tx = e;
			ty = f;

			ro = Math.acos(a*const2);
		}

		/**
		 * reconstructs a string description suitable for use
		 * in SVG DOM.
		 * @return SVG DOM string representation of the decomposed matrix.
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("skewY(").append(sky)
				.append(") translate(").append(tx).append(",")
				.append(ty).append(") rotate(").append(Math.toDegrees(ro))
				.append(") scale(").append(scx).append(",")
				.append(scy).append(") skewX(").append(skx).append(")");
			return sb.toString();
		}
	} // class Decomposition

	private MatrixTools() {
	}

	/**
	 * static helper to create a decomposition of a given matrix.
	 * @param matrix the matrix to decompose.
	 * @return the decomposition
	 */
	public static Decomposition decompose(SVGMatrix matrix) {
		return new Decomposition(matrix);
	}

	/**
	 * static helper to convert an SVGMatrix to Java2D AffineTransform.
	 * @param matrix the SVG matrix
	 * @return the Java2D AffineTransform
	 */
	public static final AffineTransform toJavaTransform(SVGMatrix matrix) {
		return new AffineTransform(
			matrix.getA(),
			matrix.getB(),
			matrix.getC(),
			matrix.getD(),
			matrix.getE(),
			matrix.getF());
	}

	/**
	 * static helper to create a SVG DOM suitable string representation
	 * of a Java2D AffineTransform.
	 * @param matrix the Java2D AffineTransform
	 * @return the SVG DOM string for this matrix.<br>
	 *         The identity matrix is return if matrix is null.
	 */
	public static String toSVGString(AffineTransform matrix) {
		if (matrix == null)
			return "matrix(1 0 0 1 0 0)";

		// m00 m10 m01 m11 m02 m12 
		double [] m = new double[6];
		matrix.getMatrix(m);
		return new StringBuffer("matrix(")
			.append(m[0]).append(' ')
			.append(m[1]).append(' ')
			.append(m[2]).append(' ')
			.append(m[3]).append(' ')
			.append(m[4]).append(' ')
			.append(m[5]).append(')').toString();
	}

	/**
	 * static helper to convert an SVG DOM string representation
	 * of a AffineTransform to the Java2D equivalent.
	 * @param string the SVG DOM string for the matrix
	 * @return the Java2D AffineTransform.<br>
	 *         null if string is null or string is not an SVG matrix string.
	 */
	public static AffineTransform toJavaTransform(String string) {
		if (string == null)
			return null;
		try {
			return AWTTransformProducer.createAffineTransform(string);
		}
		catch (ParseException pe) {
			return null;
		}
	}
}
// end of file
