/*
 * ElementUtils.java
 * -----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */  
package de.intevation.printlayout.batik;

import org.apache.batik.svggen.StyleHandler;

import org.w3c.dom.Element;
import org.w3c.dom.CDATASection;

import java.util.Map;
import java.util.TreeMap;

import org.apache.batik.svggen.SVGGeneratorContext;

import de.intevation.printlayout.pathcompact.ElementAttributes;

/**
 * Instances of this class are used to build CSS
 * section in generated SVG documents.
 */
public class StyleSheetHandler
implements   StyleHandler
{
	private CDATASection styleSheets;
	private TreeMap      styles;

	public StyleSheetHandler(CDATASection styleSheets) {
		this.styleSheets = styleSheets;
		styles = new TreeMap();
	}

	public void setStyle(
		Element             element,
		Map                 styleMap,
		SVGGeneratorContext generatorContext
	) {
		ElementAttributes s = new ElementAttributes(styleMap);

		String id = (String)styles.get(s);

		if (id == null) {
			id = generatorContext.getIDGenerator().generateID("C");
			styles.put(s, id);
			styleSheets.appendData("."+ id +"{");
			styleSheets.appendData(s.asStyleSheet());
			styleSheets.appendData("}");
		}
		element.setAttributeNS(null, "class", id);
	}
}
// end of file
