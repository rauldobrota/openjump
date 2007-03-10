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
package de.intevation.printlayout.util;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.apache.batik.dom.AbstractElement;

import de.intevation.printlayout.DocumentManager;

import java.util.Stack;

/**
 * Contains class methods to get elements by tag name. 
 */
public final class ElementUtils {

	private ElementUtils() {
	}
	
	/**
	 * scans the element if it is an object leaf and has a child with tagname
	 * equals tag. 
	 * @param element will be scaned
	 * @param tag     has child this tagname
	 * @return  a child with the correct tagname.
	 */
	public static AbstractElement getIDObjectByTag(
			AbstractElement element, 
			String tag
	) {
		String id = element.getAttributeNS(null, "id");
		if (id != null && id.startsWith(DocumentManager.OBJECT_ID_LEAF)) {
			NodeList children = element.getChildNodes();
			for (int N = children.getLength() - 1; N >= 0; N--) {
				if (checkElementByTag((AbstractElement)children.item(N), tag)) 
					return (AbstractElement)children.item(N);
			}
		}
		return null;
	}
	
  /**
	 * helper methods. Checks the tagname of an element.
	 */	
	private static final boolean checkElementByTag(
			AbstractElement element, 
			String tag
	) {
		return tag.equals(element.getTagName());
	}

	/**
	 * checks if element is a object leaf and has a child with tagname
	 * equals tag.
	 * 
	 * @param element will be checked
	 * @param tag     Has child this tagname?
	 * @return  Is there a child with tagname tag?
	 */
	public static boolean checkIDObjectByTag(
		AbstractElement element, 
		String tag
	) {
		return getIDObjectByTag(element, tag) != null;
	}

	/**
	 * Simulates decument.getElementById(id) without the document
	 * for elements which have no owner.
	 * This is ugly. Is there a better way?
	 * @param element the fake 'root' node
	 * @param idKey   the id to look for
	 * @return if found the element with the given id, else null
	 */
	public static Element getElementById(Element element, String idKey) {
		Stack stack = new Stack();
		for (;;) {
			NodeList children = element.getChildNodes();
			for (int i = children.getLength(); i >= 0; --i) {
				Node node = children.item(i);
				if (node instanceof Element) {
					element = (Element)node;
					String id = element.getAttributeNS(null, "id");
					if (id != null && id.equals(idKey))
						return element;
					stack.push(element);
				}
			}
			if (stack.isEmpty())
				break;
			element = (Element)stack.pop();
		}
		return null;
	}
}
