/*
 * OverlapSorter.java
 * ------------------
 * (c) 2007 by Sascha L. Teichmann (teichmann@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.pathcompact;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Collections;

import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;

public final class OverlapSorter
{
	private AABB.Tree spatial;

	private ArrayList entries;

	private static final class Entry 
	extends                    AABB.Box
	implements                 Comparable
	{
		private Element           object;
		private ElementAttributes type;
		private int               number;

		private ArrayList         children;
		private int               numParents;

		Entry(Rectangle2D box, Element object, ElementAttributes type, int number) {
			super(box);
			this.object = object;
			this.type   = type;
			this.number = number;
		}

		void addParent()  {
			++numParents;
		}

		boolean removeParent() {
			return --numParents <= 0;
		}

		void addChild(Entry entry)  {
			if (children == null)
				children = new ArrayList(5);
			children.add(entry);
		}

		boolean hasParents() {
			return numParents > 0;
		}

		void clear() {
			object = null;
			type   = null;
		}

		void releaseChildren(TreeMap free) {
			if (children != null) {
				for (Iterator i = children.iterator(); i.hasNext();) {
					Entry child = (Entry)i.next();
					if (child.removeParent()) {
						TreeSet list = (TreeSet)free.get(child.type);
						if (list == null)
							free.put(child.type, list = new TreeSet());
						list.add(child);
					}
				}
				children.clear();
				children = null;
			}
		}

		public boolean equals(Object other) {
			return number == ((Entry)other).number;
		}

		public int hashCode() {
			return number;
		}

		public int compareTo(Object other) {
			Entry e = (Entry)other;
			int diff = e.number - number;

			if (diff < 0) return -1;
			if (diff > 0) return +1;
			return 0;
		}
	}

	public OverlapSorter() {
		spatial = new AABB.Tree();
	}

	public void ensureCapacity(int N) {
		if (entries == null) entries = new ArrayList(N);
		else                 entries.ensureCapacity(N);
	}

	public void add(Rectangle2D bbox, Element object, ElementAttributes type) {

		Entry entry = new Entry(bbox, object, type, entries.size());
		entries.add(entry);
	}

	public void buildSpatialIndex() {
		Collections.shuffle(entries);
		for (int i = 0, N = entries.size(); i < N; ++i) {
			Entry entry = (Entry)entries.get(i);
			spatial.insert(entry, entry);
			if (((i+1) % 10000) == 0)
				System.err.println((i+1) + " in index");
		}
		entries.clear();
	}

	public void performOverlapTest() {

		// walk all leaves of the tree
		spatial.walkAll(new AABB.Tree.QueryCallback() {

			int count;

			public boolean found(AABB.Tree.Node node, int intersectionTyp) {

				final Entry entry = (Entry)node.getData();

				// find all intersecting leaves
				spatial.query(entry, new AABB.Tree.QueryCallback() {

					public boolean found(AABB.Tree.Node node, int intersectionTyp) {

						Entry other = (Entry)node.getData();

						// if entry was inserted earlier it has to be rendered earlier
						if (entry.number < other.number) {
							entry.addChild(other);
							other.addParent();
						}
						else if (other.number == entry.number) {
							if ((++count % 10000) == 0)
								System.err.println("intersected: " + count);
						}
						return true;
					}
				}); // spatial query
				return true;
			}
		}); // walk all
	}

	public void clear() {
		spatial.clear();
	}

	public interface Visitor {
		void visit(Element object, ElementAttributes type);
	}

	public void topological(Visitor visitor) {

		final TreeMap free = new TreeMap();

		// find all of top level
		spatial.walkAll(new AABB.Tree.QueryCallback() {
			public boolean found(AABB.Tree.Node node, int intersectionTyp) { 
				Entry entry = (Entry)node.getData();
				if (!entry.hasParents()) {
					TreeSet list = (TreeSet)free.get(entry.type);
					if (list == null)
						free.put(entry.type, list = new TreeSet());
					list.add(entry);
				}
				return true;
			}
		});

    // PriorityQueue would be better but its only in Java >= 1.5
		TreeSet current; 

		for (;;) {
			int max = 0; // zero prevents empty list from getting max

			current = null;

			// find longest list
			for (Iterator i = free.values().iterator(); i.hasNext();) {
				TreeSet list = (TreeSet)i.next();
				int L = list.size();
				if (L > max) { max = L; current = list; }
			}

			if (current == null)
				break;

			while (!current.isEmpty()) {
				Entry entry = (Entry)current.first();
				current.remove(entry);
				entry.releaseChildren(free);
				visitor.visit(entry.object, entry.type);
				entry.clear();
			}
		}

		clear();
	}
}
// end of file
