/*
 * BoxFactory.java
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

import java.awt.Paint;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.batik.svggen.SVGColor;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.apache.batik.svggen.SVGBasicStroke;
import org.apache.batik.svggen.SVGStrokeDescriptor;

import de.intevation.printlayout.DocumentManager;
import de.intevation.printlayout.MatrixTools;

public class BoxFactory 
implements   BoxInteractor.Factory 
{
	protected DrawingAttributes attributes;
	public BoxFactory() {}

	public void setDrawingAttributes(DrawingAttributes attributes) {
		this.attributes = attributes;
	}

	public DrawingAttributes getDrawingAttributes() {
		return attributes;
	}
	
	public DocumentManager.DocumentModifier createBox(
		final Rectangle2D     rect,
		final AffineTransform xform
	) {
		return new DocumentManager.DocumentModifier() {

			public Object run(DocumentManager documentManager) {
				SVGDocument document = documentManager.getSVGDocument();
				
				String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

				AbstractElement box = 
					(AbstractElement)document.createElementNS(svgNS, "rect");
				
				AbstractElement group = 
					(AbstractElement)document.createElementNS(svgNS, "g");
		
				group.setAttributeNS(null, "transform", MatrixTools.toSVGString(xform)); 	
				setRectAttributes(box, rect);
				
				configureBoxElement(box, document);	

				box.setAttributeNS(null, "pointer-events", "all");

				group.setAttributeNS(null, "id", documentManager.uniqueObjectID());
        group.appendChild(box);
				
				AbstractElement parent =
					(AbstractElement)document.getElementById(
						DocumentManager.DOCUMENT_SHEET);
				parent.appendChild(group);
				return null;
			}
		};
	}

	public void configureBoxElement(AbstractElement box, SVGDocument document) {
		Paint   strokeColor  = attributes.getStrokeColor();
		Stroke  stroke       = attributes.getStroke();
		Paint   fillColor    = attributes.getFillColor();
		
		String[] names = {"stroke", "stroke-opacity"};
		ConsumerUtils.setColorByPaint(strokeColor, names, box, document);
		names = new String[] {"fill", "fill-opacity"};
		if (fillColor != null)
			ConsumerUtils.setColorByPaint(fillColor, names, box, document);
		else
			box.setAttributeNS(null, "fill", "none");
			
		if (stroke != null && stroke instanceof BasicStroke) {
			Map attributeMap = getStrokeAttrMap((BasicStroke) stroke, document);
			ConsumerUtils.setAttributesByMap(box, attributeMap);
		}
	}
	
	protected Map getStrokeAttrMap(BasicStroke stroke, SVGDocument document) {
		
		SVGStrokeDescriptor ssd = 
				new SVGBasicStroke(SVGGeneratorContext.createDefault(document))
				.toSVG((BasicStroke) stroke);
		
		return ssd.getAttributeMap(null);
	}
	
	protected void setRectAttributes(AbstractElement el, Rectangle2D rect) {
		setAttributeByInt(el, "x", rect.getX());
		setAttributeByInt(el, "y", rect.getY());
		setAttributeByInt(el, "width", rect.getWidth());
		setAttributeByInt(el, "height", rect.getHeight());
	}

	protected void setAttributeByInt(AbstractElement el, 
			String attrName, double value) 
	{
		el.setAttributeNS(null, attrName, String.valueOf(value));
	}
}
