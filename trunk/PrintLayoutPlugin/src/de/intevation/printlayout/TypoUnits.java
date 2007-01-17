/*
 * TypoUnits.java
 * --------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 */
package de.intevation.printlayout;

public class TypoUnits
{
	/**
   * in (inches; 1in=2.54cm)
   * cm (centimeters; 1cm=10mm)
   * mm (millimeters)
   * pt (points; 1pt=1/72 in)
   * pc (picas; 1pc=12pt)
	 */

	public static final int TYPE_MM   = 0;
	public static final int TYPE_IN   = 1;
	public static final int TYPE_PT   = 2;
	public static final int TYPE_PC   = 3;
	public static final int TYPE_CM   = 4;
	public static final int TYPE_PERC = 5;
	public static final int TYPE_PX   = 6;
	public static final int TYPE_EM   = 7;
	public static final int TYPE_EX   = 8;

	public static final double mm2in(double in) {
		return in * 0.039370079d;
	}

	public static final double in2mm(double in) {
		return in * 25.4d;
	}

	public static final double pt2mm(double pt) {
		return pt * 0.3514598;
	}

	public static final double pc2mm(double pc) {
		return pc * 4.2175176;
	}

	public static final double cm2mm(double cm) {
		return cm * 10d;
	}

	public static int stringToMM(
		String s, 
		double px2mm, 
		double max, 
		double [] v
	) {
		if (s == null || s.length() == 0)
			throw new NumberFormatException();

		s = s.trim().toLowerCase();

		if (s.endsWith("mm")) {
			s = s.substring(0, s.length()-2).trim();
			v[0] = Double.parseDouble(s);
			return TYPE_MM;
		}
		else if (s.endsWith("in")) {
			s = s.substring(0, s.length()-2).trim();
			v[0] = in2mm(Double.parseDouble(s));
			return TYPE_IN;
		}
		else if (s.endsWith("cm")) {
			s = s.substring(0, s.length()-2).trim();
			v[0] = cm2mm(Double.parseDouble(s));
			return TYPE_CM;
		}
		else if (s.endsWith("pt")) {
			s = s.substring(0, s.length()-2).trim();
			v[0] = pt2mm(Double.parseDouble(s));
			return TYPE_PT;
		}
		else if (s.endsWith("pc")) {
			s = s.substring(0, s.length()-2).trim();
			v[0] = pc2mm(Double.parseDouble(s));
			return TYPE_PC;
		}
		else if (s.endsWith("%")) {
			s = s.substring(0, s.length()-1).trim();
			double p = Double.parseDouble(s);
			v[0] = p * (1d/100) * max;
			return TYPE_PERC;
		}
		else if (s.endsWith("em") || s.endsWith("ex")) {
			// TODO/FIXME: use respective units
			s = s.substring(0, s.length()-2).trim();
		}

		v[0] = Double.parseDouble(s) * px2mm;
		return TYPE_PX;
	}
}
// end of file
