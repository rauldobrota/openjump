/*
 * TypoUnits.java
 * --------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.util;

/**
 * helper class to extract paper units out of
 * SVG attributes and convert them.
 */
public class TypoUnits
{
	/**
   * in (inches; 1in=2.54cm)
   * cm (centimeters; 1cm=10mm)
   * mm (millimeters)
   * pt (points; 1pt=1/72 in)
   * pc (picas; 1pc=12pt)
	 */

	/**
	 * type is in milimeter
	 */
	public static final int TYPE_MM   = 0;
	/**
	 * type is in inch
	 */
	public static final int TYPE_IN   = 1;
	/**
	 * type is in point
	 */
	public static final int TYPE_PT   = 2;
	/**
	 * type is in pica
	 */
	public static final int TYPE_PC   = 3;
	/**
	 * type is in centimeter
	 */
	public static final int TYPE_CM   = 4;
	/**
	 * type is in percent
	 */
	public static final int TYPE_PERC = 5;
	/**
	 * type is in pixel
	 */
	public static final int TYPE_PX   = 6;
	/**
	 * type is in em
	 */
	public static final int TYPE_EM   = 7;
	/**
	 * type is in ex
	 */
	public static final int TYPE_EX   = 8;

	/**
	 * converts mm to inch
	 * @param mm value in mm
	 * @return   value in inch
	 */
	public static final double mm2in(double mm) {
		return mm * 0.039370079d;
	}

	/**
	 * converts inch to mm
	 * @param in value in inch
	 * @return   value in mm
	 */
	public static final double in2mm(double in) {
		return in * 25.4d;
	}

	/**
	 * converts points to mm
	 * @param pt value in points
	 * @return   value in mm
	 */
	public static final double pt2mm(double pt) {
		return pt * 0.3514598;
	}

	/**
	 * converts pica to mm
	 * @param pc value in pica
	 * @return   value in mm
	 */
	public static final double pc2mm(double pc) {
		return pc * 4.2175176;
	}

	/**
	 * converts cm to mm
	 * @param cm value in cm
	 * @return   value in mm
	 */
	public static final double cm2mm(double cm) {
		return cm * 10d;
	}

	/**
	 * Takes a string, determine this unit type,
	 * converts the parsed string to mm, stores
	 * the result in v.
	 * @param s     the string to parse
	 * @param px2mm how to convert from px to mm (user agent value)
	 * @param max   if in percent what is 100
	 * @param v     to store the result in mm
	 * @return      the unit type of the string.
	 */
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
