/*
 * DocumentManagerUtils.java
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

import java.util.ArrayList;

import org.w3c.dom.NodeList;

import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.dom.AbstractElement;

public class DocumentManagerUtils {
	public static AbstractElement firstElementByTag(
			String [] ids, 
			String tag,
			SVGDocument document
	) {
		if (!checkIDsByTag(ids, "rect", document))
			return null;
		
		for (int i = 0; i < ids.length; i++) {
			AbstractElement element = 
				(AbstractElement)document.getElementById(ids[i]);
			if(element == null)
				continue;
			ArrayList list = getElementByTag(element, tag);
			if (list != null
			&& list.size() > 0)
				return (AbstractElement) list.get(0);
		}
		return null;
	} 
	
	public static boolean checkIDsByTag(
			String [] ids, 
			String tag,
			SVGDocument document
	) {
		for (int i = 0; i < ids.length; i++) {
			AbstractElement element = 
				(AbstractElement)document.getElementById(ids[i]);
			if(element == null)
				continue;
			if(getElementByTag(element, tag).size() > 0)
				return true;
		}

		return false;
	}
	
	public static ArrayList getElementByTag(AbstractElement element, String tag) {
		ArrayList list = new ArrayList();
		getElementByTag(element, tag, list);

		return list;
	}

	protected static void getElementByTag(
			AbstractElement element,
			String tag, 
			ArrayList list
	) {
		if (element == null)
			return;
		if (tag.equals(element.getTagName()))
			list.add(element);
		
		if (!element.hasChildNodes())
			return;		
		NodeList nodes = element.getChildNodes();
		for (int N = nodes.getLength(); N >= 0; N--) {
			if(nodes.item(N) instanceof AbstractElement)	
				getElementByTag((AbstractElement)nodes.item(N), tag, list);
		}
	}
}
