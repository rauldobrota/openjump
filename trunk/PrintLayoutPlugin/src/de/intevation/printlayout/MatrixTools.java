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
package de.intevation.printlayout;

import org.w3c.dom.svg.SVGMatrix;

import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.ParseException;

import java.awt.geom.AffineTransform;

public class MatrixTools
{
	public static final class Decomposition 
	{
		public double tx; // translate x
		public double ty; // translate y

		public double ro; // rotation

		public double scx; // scale x
		public double scy; // scale y

		public double skx; // skew x
		public double sky; // skew y
		
		public Decomposition(SVGMatrix matrix) {
			this(
				matrix.getA(),
				matrix.getB(),
				matrix.getC(),
				matrix.getD(),
				matrix.getE(),
				matrix.getF());
		}

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

	public static Decomposition decompose(SVGMatrix matrix) {
		return new Decomposition(matrix);
	}

	public static final AffineTransform toJavaTransform(SVGMatrix matrix) {
		return new AffineTransform(
			matrix.getA(),
			matrix.getB(),
			matrix.getC(),
			matrix.getD(),
			matrix.getE(),
			matrix.getF());
	}

	public static String toSVGString(AffineTransform matrix) {
		if (matrix == null)
			return "matrix(1 0 0 1 0 0)";

		StringBuffer sb = new StringBuffer("matrix(");
		double [] m = new double[6];
		// m00 m10 m01 m11 m02 m12 
		matrix.getMatrix(m);
		for (int i = 0; i < m.length; ++i) {
			if (i > 0) sb.append(' ');
			sb.append(m[i]);
		}
		sb.append(')');
		return sb.toString();
	}

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

	/*
	public static void main(String [] args) {
		for (int i = 0; i < args.length; ++i) {
			System.out.println("'" + args[i] + "'");
			AffineTransform xform = toJavaTransform(args[i]);
			System.out.println(xform);
		}
	}
	*/
}
// end of file
