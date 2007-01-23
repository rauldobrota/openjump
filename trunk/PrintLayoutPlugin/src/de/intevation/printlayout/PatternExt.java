/*
 * LayoutFrame.java
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

import java.awt.Paint;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;

import com.vividsolutions.jump.workbench.ui.renderer.style.WKTFillPattern;

public class PatternExt
extends      DefaultExtensionHandler
{
	public SVGPaintDescriptor handlePaint(
		Paint paint,
    SVGGeneratorContext generatorContext
	) {
		if (paint instanceof WKTFillPattern) {
			// TODO: Generate an adequate SVG fill pattern
			return new SVGPaintDescriptor("red", "1");
		}
		return super.handlePaint(paint, generatorContext);
	}
}
// end of file
