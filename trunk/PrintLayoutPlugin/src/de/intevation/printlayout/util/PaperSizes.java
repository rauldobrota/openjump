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
package de.intevation.printlayout.util;

import java.util.TreeMap;
import java.util.Properties;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.util.XMLResourceDescriptor;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;

/**
 * Class with static methods to deal with the 
 * sizes of the virtual sheets of paper. Also
 * has methods to create an SVG document of
 * a certain size from a template stored 
 * in the resources.
 */
public class PaperSizes
{
	/**
	 * Path to the SVG template in the resources.
	 */
	public static final String PAPER_RESOURCE = 
		"resources/paper-tmpl.svg";

	/**
	 * path to list of known paper sizes in the resources.
	 */
	public static final String PAPER_SIZES_RESOURCE = 
		"resources/paper-sizes.properties";

	/**
	 * a map of the known paper sizes.
	 */
	private static final TreeMap SIZES = new TreeMap();

	/**
	 * a cached instance of the SVG template source.
	 */
	private static String paperTemplate;

	/**
	 * extracts the template of the SVG document from
	 * the resources.
	 * @return the template string
	 */
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

	/**
	 * fills the SVG template to produce a SVG source string
	 * which matches the given paper size.
	 * @param width the width of the paper
	 * @param height the height of the paper
	 * @return string (is XML SVG) which matches the paper size.<br>
	 *         null if template could not be found.
	 */
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
		// A4 should be always known
		SIZES.put("A4", new double [] { 210d,  297d });
		loadDefaultPaperSizes();
	}

	/**
	 * loads the known paper sizes from the resources
	 */
	private static void loadDefaultPaperSizes() {

		InputStream is =
			PaperSizes.class.getResourceAsStream(PAPER_SIZES_RESOURCE);

		if (is == null)
			return;

		Properties props = new Properties();
		try { props.load(is); }
		catch (IOException ioe) { return; }
		finally {
			try { is.close(); }
			catch (IOException ioe) {}
			is = null;
		}

		for (Iterator i = props.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry)i.next();
			StringTokenizer st = new StringTokenizer((String)entry.getValue());
			if (st.countTokens() > 1)
				try {
					double width  = Double.parseDouble(st.nextToken());
					double height = Double.parseDouble(st.nextToken());
					putPageSize((String)entry.getKey(), width, height);
				}
				catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
		}
	}

	/**
	 * Stores a paper size to the map of known paper sizes under
	 * a given id. eg. "A4"
	 * @param id the symbol this paper size refers to.
	 * @param width the width of the paper
	 * @param height the height of the paper
	 */
	public static void putPageSize(String id, double width, double height) {
		SIZES.put(id, new double [] { width, height });
	}

	/**
	 * Fetches a paper size by looking up a given id.
	 * @param id the id of the paper. eg. "A4"
	 * @param v  the array to store the found paper size in.
	 * @return true if paper size was found, false otherwise.
	 */
	public static boolean getPageSize(String id, double [] v) {

		if (id == null || v == null || v.length < 2)
			return false;

		double [] dim = (double [])SIZES.get(id);
		if (dim == null)
			return false;

		v[0] = dim[0];
		v[1] = dim[1];

		return true;
	}

	/**
	 * Gives an iterator over all known paper size keys.
	 * @return the iterator
	 */
	public static Iterator knownPaperSizeKeys() {
		return SIZES.keySet().iterator();
	}

	/**
	 * Creates a SVG XML source text for a given paper sheet id
	 * in portrait mode.
	 * @param id the id of the paper size. e.g. "A4"
	 * @return the SVG XML source text. if id was not
	 *         found the source for "A4" is returned.
	 */
	public static String sheetForPaperSize(String id) {
		return sheetForPaperSize(id, "A4", false);
	}

	/**
	 * Creates a SVG XML source text for a given paper sheet id
	 * in portrait mode.
	 * @param id the id of the paper size. e.g. "A4"
	 * @param def the id of the default paper size.
	 * @return the SVG XML source text. if id was not
	 *         found the source for def is returned.
	 */
	public static String sheetForPaperSize(String id, String def) {
		return sheetForPaperSize(id, def, false);
	}

	/**
	 * Creates a SVG XML source text for a given paper sheet id.
	 * @param id the id of the paper size. e.g. "A4"
	 * @param def the id of the default paper size.
	 * @param landscape should the paper be in landscape mode?
	 * @return the SVG XML source text. if id was not
	 *         found the source for def is returned.
	 */
	public static String sheetForPaperSize(
		String id, String def, 
		boolean landscape
	) {
		double [] dim = (double [])SIZES.get(id);

		if (dim == null
		&& ((def == null || (dim = (double [])SIZES.get(def)) == null)))
			return null;

		return landscape
			? getSheet(dim[1], dim[0])
			: getSheet(dim[0], dim[1]);
	}

	/**
	 * reverse lookup of the paper size symbol given the paper size.
	 * @param size the size of the paper
	 * @return the symbol of the first paper that matches this size.<br>
	 *         null if no adequate paper was found.
	 */
	public static String guessPaperSize(double [] size) {
		if (size == null || size.length < 2)
			return null;

		for (Iterator i = SIZES.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry)i.next();
			double [] current = (double [])entry.getValue();
			if (Math.abs(current[0] - size[0]) < 0.00001d
			&&  Math.abs(current[1] - size[1]) < 0.00001d)
				return (String)entry.getKey();
		}

		return null;
	}

	/**
	 * Creates an SVG document for a given paper id in portrait mode.
	 * If no such paper was found it falls back to "A4".
	 * @param id the id of the paper
	 * @return an SVG document representing this paper size
	 */
	public static SVGDocument createSheet(String id) {
		return createSheet(id, "A4", false);
	}

	/**
	 * Creates an SVGDocument for a given paper id in portrait mode.
	 * If no such paper was found it falls back to def.
	 * @param id the id of the paper
	 * @param def the default if id is not found
	 * @return an SVG document representing this paper size
	 */
	public static SVGDocument createSheet(String id, String def) {
		return createSheet(id, def, false);
	}

	/**
	 * Creates an SVGDocument for a given paper id.
	 * If no such paper was found it falls back to def.
	 * @param id the id of the paper
	 * @param def the default if id is not found
	 * @param landscape should the paper be in landscape mode?
	 * @return an SVG document representing this paper size
	 */
	public static SVGDocument createSheet(
		String id, String def,
		boolean landscape
	) {
		String text = sheetForPaperSize(id, def, landscape);
		if (text == null)
			return null;

		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

		try {
			return (SVGDocument)factory.createDocument(
				null,
				new StringReader(text));
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}
}
// end of file
