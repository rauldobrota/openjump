/*
 * PaperSizes.java
 * ---------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import java.util.TreeMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PaperSizes
{
	public  static final String PAPER_RESOURCE = 
		"resources/paper-tmpl.svg";

	private static final TreeMap SIZES = new TreeMap();

	private static String paperTemplate;

	public static synchronized String getPaperTemplate() {
		if (paperTemplate == null) {

			InputStream is = PaperSizes.class.getResourceAsStream(
				PAPER_RESOURCE);
			
			if (is == null) return null;

			try {
				byte [] buf = new byte[512];
				int r;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((r = is.read(buf)) > 0)
					out.write(buf, 0, r);
				paperTemplate = out.toString("UTF-8");
			}
			catch (IOException ioe) {
				return null;
			}
			finally {
				try { is.close(); } catch (IOException ioe) {}
			}
		}
		return paperTemplate;
	}

	public static String getSheet(double width, double height) {
		String src = getPaperTemplate();
		if (src == null)
			return null;

		Template tmpl = new Template();

		double boxWidth  = width  + 40d;
		double boxHeight = height + 40d;

		tmpl.setVariable("WIDTH",     String.valueOf(width));
		tmpl.setVariable("HEIGHT",    String.valueOf(height));
		tmpl.setVariable("BOXWIDTH",  String.valueOf(boxWidth));
		tmpl.setVariable("BOXHEIGHT", String.valueOf(boxHeight));

		return tmpl.toString(src);
	}

	static {
		SIZES.put("DIN A4", new double [] { 210d,  297d });
		SIZES.put("DIN A0", new double [] { 841d, 1189d });
		SIZES.put("DIN A1", new double [] { 594d,  841d });
		SIZES.put("DIN A2", new double [] { 420d,  594d });
		SIZES.put("DIN A3", new double [] { 297d,  420d });
	}

	public static void putPageSize(String id, double width, double height) {
		SIZES.put(id, new double [] { width, height });
	}

	public static boolean getPageSize(String id, double [] v) {
		double [] dim = (double [])SIZES.get(id);
		if (dim == null)
			return false;

		v[0] = dim[0];
		v[1] = dim[1];

		return true;
	}

	public static String sheetForPaperSize(String id) {
		return sheetForPaperSize(id, false);
	}

	public static String sheetForPaperSize(String id, boolean landscape) {

		double [] dim = (double [])SIZES.get(id);

		if (dim == null)
			return null;

		return landscape
			? getSheet(dim[1], dim[0])
			: getSheet(dim[0], dim[1]);
	}
}
// end of file
