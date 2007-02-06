/*
 * GUIUtils.java
 * --------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;

import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.ViewCSS;

import org.w3c.dom.svg.SVGPaint;

import de.intevation.printlayout.tools.DrawingAttributes;

public class GUIUtils {
	
	public static DrawingAttributes getRectDrawingAttributs(Element element) {
		if (element == null)
			return null;

		DrawingAttributes attributes = new DrawingAttributes();
		attributes.setStrokeColor(getColor(element, "stroke"));
		attributes.setFillColor(getColor(element, "fill"));
		attributes.setStroke(getStroke(element));
		
		return attributes;
	}

	public static Color getColor(Element element, String attribute) {
		Color returnColor = null;
		Document document = element.getOwnerDocument();
		ViewCSS viewCSS = (ViewCSS) document.getDocumentElement();
		CSSStyleDeclaration computedStyle =
				viewCSS.getComputedStyle(element, null);
		SVGPaint svgPaint =
				(SVGPaint) computedStyle.getPropertyCSSValue(attribute);

		if (svgPaint.getPaintType() == SVGPaint.SVG_PAINTTYPE_RGBCOLOR) {
			RGBColor rgb = svgPaint.getRGBColor();
			float red 
				= rgb.getRed().getFloatValue(CSSValue.CSS_PRIMITIVE_VALUE);
			float green 
				= rgb.getGreen().getFloatValue(CSSValue.CSS_PRIMITIVE_VALUE);
			float blue 
						= rgb.getBlue().getFloatValue(CSSValue.CSS_PRIMITIVE_VALUE);
			returnColor = new Color(red / 255, green / 255, blue / 255);
		}

		return returnColor;
	}
	
	
	public static Stroke getStroke(Element element)
	throws NumberFormatException
	{
		if(getAttribute(element, "stroke-width") == null
    || getAttribute(element, "stroke-width").equals("none"))
			return null;
		
		int strokeWidth 
			= (int)Float.parseFloat(getAttribute(element, "stroke-width"));

		if (strokeWidth == 0 
		|| getAttribute(element, "stroke-dasharray") == null
		|| getAttribute(element, "stroke-dasharray").equals("none"))
			return new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL);
		
		StringTokenizer tokens 
			= new StringTokenizer(getAttribute(element, "stroke-dasharray"), ",");
		
		float[] dash = new float[tokens.countTokens()];
		for (int i = 0; tokens.hasMoreTokens(); i++) {
			dash[i] = Float.parseFloat(tokens.nextToken());
		}
	
		return new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL, 1.0f, dash, 0);

	}

	private static String getAttribute(Element el, String attrName) {
		return el.getAttributeNS(null, attrName);
	}

}
