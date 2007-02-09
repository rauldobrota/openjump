/*
 * TextConsumer.java
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
import java.awt.Font;

import java.awt.font.FontRenderContext;

import java.awt.geom.AffineTransform;

import java.util.Map;

import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGFontDescriptor;
import org.apache.batik.svggen.SVGFont;

import org.apache.batik.util.XMLConstants;

import de.intevation.printlayout.DocumentManager;
import de.intevation.printlayout.DocumentManagerUtils;
import de.intevation.printlayout.MatrixTools;

public class TextConsumer 
implements TextInteractor.Consumer {
	public DocumentManager.DocumentModifier createNewText(
			final String text,
			final AffineTransform trans,
			final Color color,
			final Font font) {
		return new DocumentManager.DocumentModifier() {
			public Object run(DocumentManager documentManager) {
				SVGDocument document = documentManager.getSVGDocument();

				String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

				AbstractElement textElement = 
					(AbstractElement)document.createElementNS(svgNS, "text");

				textElement.setAttributeNS(XMLConstants.XML_NAMESPACE_URI,
						"space", "preserve");

				textElement.appendChild(document.createTextNode(text));
				
				String[] names = {"stroke", "stroke-opacity", 
				                  "fill",  "fill-opacity"};
				ConsumerUtils.setColor(color, names, textElement, document);
		

				ConsumerUtils.setAttributesByMap(textElement, 
						getFontAttrMap(font, document));
						
				AbstractElement xform =
					(AbstractElement)document.createElementNS(svgNS, "g");

				xform.setAttributeNS(null, "id", documentManager.uniqueObjectID());

				AbstractElement sheet =
					(AbstractElement)document.getElementById(
					documentManager.DOCUMENT_SHEET);

				xform.setAttributeNS(
					null, "transform", MatrixTools.toSVGString(trans));

				xform.appendChild(textElement);

				sheet.appendChild(xform);

				return null;
			}
		};
	}

	public DocumentManager.DocumentModifier createUpdateModifier(
		final String id,
		final String text,
		final Color color,
		final Font font
	) {
		return new DocumentManager.DocumentModifier() {
			public Object run(DocumentManager documentManager) {
				SVGDocument document = documentManager.getSVGDocument();
				AbstractElement textElement = 
					DocumentManagerUtils.firstElementByTag(new String[]{id}, "text",
							document);
				
				textElement.setTextContent(text); 
			 	
				String[] names = {"stroke", "stroke-opacity", 
				                  "fill",  "fill-opacity"};
				ConsumerUtils.setColor(color, names, textElement, document);
		
				ConsumerUtils.setAttributesByMap(textElement, 
						getFontAttrMap(font, document));
				return null;
			}
		};
	}

	protected Map getFontAttrMap(Font font, SVGDocument document) {
	
		if( font == null)
			return null;
		
		SVGFontDescriptor sfd =
				new SVGFont(SVGGeneratorContext.createDefault(document))
				.toSVG(font, new FontRenderContext(
				null, true, true));
		
		return sfd.getAttributeMap(null);
	}

}
