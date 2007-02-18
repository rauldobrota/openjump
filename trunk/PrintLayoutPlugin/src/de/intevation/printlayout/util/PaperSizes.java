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

public class PaperSizes
{
	public static final String PAPER_RESOURCE = 
		"resources/paper-tmpl.svg";

	public static final String PAPER_SIZES_RESOURCE = 
		"resources/paper-sizes.properties";

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
		SIZES.put("A4", new double [] { 210d,  297d });
		loadDefaultPaperSizes();
	}

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

	public static Iterator knownPaperSizeKeys() {
		return SIZES.keySet().iterator();
	}

	public static String sheetForPaperSize(String id) {
		return sheetForPaperSize(id, "A4", false);
	}

	public static String sheetForPaperSize(String id, String def) {
		return sheetForPaperSize(id, def, false);
	}

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

	public static String guessPaperSize(double [] size) {
		if (size == null)
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

	public static SVGDocument createSheet(String id) {
		return createSheet(id, "A4", false);
	}

	public static SVGDocument createSheet(String id, String def) {
		return createSheet(id, def, false);
	}

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
