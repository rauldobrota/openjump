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

/**
 * Contains class methods to get elements by tag name. 
 */
public class DocumentManagerUtils {
	
	/**
	 * searchs for the first element with a specific tagname 
	 * in the subtrees of the id elements.
	 *
	 * @param ids      an array of ids, which subtrees are checked.
	 * @param tag      the specific tagname
	 * @param document SVGDocument containing the elements of the ids.
	 * @return         if an element is found , the element else null.
	 */
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
	
	/**
	 * checks all subtrees of the id elements, if an element with a
	 * specific tagname exists.
	 *
	 * @param ids      an array of ids which subtrees are checked.
	 * @param tag      the searched for tagname.
	 * @param document a SVGDocument containing the id elements.
	 * @return         found or not found.
	 */
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
	
	/**
	 * Search a DOM tree for elements with specific tagname 
	 * and collect them in an ArrayList.
	 *
	 * @param element  the root element.
	 * @param tag      tagname which is searched for.
	 * @return         arraylist of the found elements.
	 */
	
	public static ArrayList getElementByTag(AbstractElement element, String tag) {
		ArrayList list = new ArrayList();
		getElementByTag(element, tag, list);

		return list;
	}

	/**
	 * helper method.
	 */
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
