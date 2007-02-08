/*
 * ConsumerUtils.java
 * ---------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import java.awt.Color;
import java.awt.Paint;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.svggen.SVGColor;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;

/**
 * helper class for converting GUI parts to svg strings.
 */
public class ConsumerUtils {
	
	public static String getSVGColor(
			Color c, 
			String attrName, 
			SVGDocument document) 
	{
		SVGPaintDescriptor desc = SVGColor.toSVG(c,
					SVGGeneratorContext.createDefault(document));
		Map attrMap = desc.getAttributeMap(null);

		return (String)attrMap.get(attrName);
	}

	public static void setColor(Color color, 
			String[] attributeNames,
			AbstractElement element,
			SVGDocument document)
	{
		if (attributeNames == null
		|| color == null
		|| document == null)
			return;
		
		for (int i = 0; i < attributeNames.length; i++) {
			element.setAttributeNS(null, attributeNames[i],
					getSVGColor(color, attributeNames[i], document));
		}
	}

	public static void setColorByPaint(Paint paint,
			String[] attributeNames,
			AbstractElement element,
			SVGDocument document)
	{
		if (paint instanceof Color)
			setColor((Color) paint, attributeNames, element, document);
	}

	public static void setAttributesByMap(AbstractElement element, Map map) {
		if (map == null)
			return;
		
		for(Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry)iter.next();
				element.setAttributeNS(null, (String) entry.getKey(), 
					(String) entry.getValue());
		}		
	}
}
// end of file
