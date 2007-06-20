/*
 * DrawingAttributes.java
 * ----------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import java.awt.Stroke;
import java.awt.Paint;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;

import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.ViewCSS;

import org.w3c.dom.svg.SVGPaint;

public class DrawingAttributes
{
	protected Paint  strokeColor;

	protected Paint  fillColor;

	protected Stroke stroke;

	public Paint getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(Paint strokeColor) {
		this.strokeColor = strokeColor;
	}

	public Paint getFillColor() {
		return fillColor;
	}

	public void setFillColor(Paint fillColor) {
		this.fillColor = fillColor;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public Stroke getStroke() {
		return stroke;
	}
		
	/**
	 * extract the information to create a DrawingAttributes object
	 * from an element.
	 * 
	 * @param element the element the color and the stroke is extracted from.
	 * @return        a new filled DrawingAttributes.
	 */
	public static DrawingAttributes getRectDrawingAttributs(Element element) {
		if (element == null)
			return null;

		DrawingAttributes attributes = new DrawingAttributes();
		attributes.setStrokeColor(getColor(element, "stroke"));
		attributes.setFillColor(getColor(element, "fill"));
		attributes.setStroke(getStroke(element));
		
		return attributes;
	}
	
	/**
	 * extracts a color out of an SVGColor attribute of an element.
	 *
	 * @param element   the element which has a color attribute.
	 * @param attribute the color attribute
	 * @return          a color.
	 */
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
	
	/**
	 *	extracts stroke-width and stroke-dasharray out of an SVGStylable
	 *	element and constructs a new BasicStroke.
	 *
	 *	@param element an SVGStylable element.
	 *	@return        the constructed stroke.
	 */
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

	public static String getText(Element el) {
		if (!el.hasChildNodes())	
			return el.getTextContent();
			
		NodeList children = el.getChildNodes();
		StringBuffer sb = new StringBuffer();

		for (int N = children.getLength(), i = 0; i < N; i++) {
			Element tspan = (Element) children.item(i);
			if ("tspan".equals(tspan.getTagName())) {
				sb.append(tspan.getTextContent());
				sb.append("\n");
			}
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() -1);
		
		return sb.toString();
	}

	public static Font getFont(Element el) {
		String fontSizeStr   = el.getAttributeNS(null, "font-size");
		String fontFamilyStr = el.getAttributeNS(null, "font-family");
		String fontWeightStr = el.getAttributeNS(null, "font-weight");
		String fontStyleStr  = el.getAttributeNS(null, "font-style");

		return Font.decode(fontFamilyStr.replaceAll("\'" , "") + "-" +
				getFontStyle(fontWeightStr, fontStyleStr) + "-" + fontSizeStr);
	}
	
	private static String getFontStyle(String weight, String style) {
		if (weight.equals("bold") && style.equals("italic"))
			return "bolditalic";
		else if (weight.equals("bold"))
			return "bold";
		else if (style.equals("italic"))
			return "italic";
		else 
			return "plain";
	}
}
// end of file
