package de.intevation.printlayout;

import java.util.TreeMap;

public class PaperSizes
{
	private static final TreeMap SIZES = new TreeMap();

	public static String getSheet(double width, double height) {
		StringBuffer sb = new StringBuffer(512);
		sb.append("<?xml version=\"1.0\" standalone=\"no\"?>\n")
			.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" ")
			.append("\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">")
			.append("<svg width=\"")
			.append(width + 40).append("mm\" height=\"")
			.append(height + 40).append("mm\"")
			.append(" version=\"1.1\"")
			.append(" viewBox=\"0 0 250 337\"")
		  .append(" xmlns=\"http://www.w3.org/2000/svg\">")
			.append("	<rect x=\"20\" y=\"20\"")
			.append(" width=\"")
			.append(width).append("\" height=\"")
			.append(height).append("\"")
			.append(" style=\"fill:white\"/>")
			.append(" <svg width=\"")
			.append(width).append("\" height=\"")
			.append(height).append("\"")
			.append(" x=\"20\" y=\"20\"")
			.append(" id=\"viewer-layout-sheet-svg\"")
			.append(" overflow=\"visible\">")
			.append("</svg>")
			.append("</svg>");
		return sb.toString();
	}

	static {
		SIZES.put("DIN A4", new double [] { 210d, 297d });
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
