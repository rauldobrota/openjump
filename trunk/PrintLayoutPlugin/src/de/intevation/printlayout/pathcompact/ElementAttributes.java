/*
 * ElementAttributes.java
 * ----------------------
 * (c) 2007 by Sascha L. Teichmann (teichmann@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.pathcompact;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.Arrays;

public class ElementAttributes
implements   Comparable
{
	private static final class Pair 
	implements                 Comparable
	{
		String key;
		String value;

		Pair(String key, String value) {
			this.key   = key;
			this.value = value;
		}
		public int compareTo(Object o) {
			Pair other = (Pair)o;
			int diff = key.compareTo(other.key);
			if (diff < 0) return -1;
			if (diff > 0) return +1;
			diff = value.compareTo(other.value);
			if (diff < 0) return -1;
			if (diff > 0) return +1;
			return 0;
		}
	}

	private Pair [] pairs;

	public ElementAttributes(Element group) {
		NamedNodeMap map = group.getAttributes();

		int N = map.getLength();
		pairs = new Pair[N];
		for (int i = 0; i < N; ++i) {
			Node item = map.item(i);
			pairs[i] = new Pair(item.getNodeName(), item.getNodeValue());
		}
		Arrays.sort(pairs);
	}

	public int compareTo(Object o) {
		ElementAttributes other = (ElementAttributes)o;

		int diff = pairs.length - other.pairs.length;

		if (diff < 0) return -1;
		if (diff > 0) return +1;

		for (int i = 0; i < pairs.length; ++i) {
			diff = pairs[i].compareTo(other.pairs[i]);
			if (diff < 0) return -1;
			if (diff > 0) return +1;
		}

		return 0;
	}

	public boolean equals(Object other) {
		return compareTo((ElementAttributes)other) == 0;
	}

	public void attributize(Element element) {
		for (int i = 0; i < pairs.length; ++i) 
			element.setAttributeNS(null, pairs[i].key, pairs[i].value);
	}
}
// end of file
