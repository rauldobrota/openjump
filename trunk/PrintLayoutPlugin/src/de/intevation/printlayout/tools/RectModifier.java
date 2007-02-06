/*
 * RectModifier.java
 * --------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.dom.AbstractElement;

import de.intevation.printlayout.DocumentManager;
import de.intevation.printlayout.DocumentManagerUtils;

public class RectModifier 
implements DocumentManager.DocumentModifier 
{
	protected String[] ids;
	protected DrawingAttributes attributes;
	
	public RectModifier(
			final String[] ids,
			final DrawingAttributes attributes
	) {
		this.ids = ids;
		this.attributes = attributes;
	}
	
	public Object run(DocumentManager documentManager) {
		
		SVGDocument document = documentManager.getSVGDocument();
		for (int i = 0; i < ids.length; i++) {
			AbstractElement element =
				(AbstractElement)document.getElementById(ids[i]);
			
			if (element == null)
				return null;
			
			BoxFactory factory = new 	BoxFactory();
			factory.setDrawingAttributes(attributes);
			
			ArrayList nodes 
				= DocumentManagerUtils.getElementByTag(element, "rect");
		
			for (Iterator iter = nodes.iterator(); iter.hasNext();) {
				AbstractElement rect = (AbstractElement) iter.next();
				factory.configureBoxElement(rect , document);
			}
		}
		
		return null;
	}
		
}
// end of file
