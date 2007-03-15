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
import java.util.Iterator;
import java.util.TreeSet;

import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.CubicCurve2D;

import java.awt.Shape;

import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import com.vividsolutions.jts.index.strtree.STRtree;

import com.vividsolutions.jts.index.ItemVisitor;


public final class OverlapSorter
{
	private STRtree spatial;

	private Entry previous;

	private static final class Entry 
	implements                 Comparable
	{
		private Envelope          envelope;
		private Geometry          hull;
		private Entry             previous;


		private Element           object;
		private ElementAttributes type;
		private int               number;

		private int               numChildren;
		private ArrayList         parents;

		Entry(
			Rectangle2D       box, 
			Geometry          hull,
			Element           object, 
			ElementAttributes type, 
			Entry             previous
		) {
			this.envelope = new Envelope(
				box.getMinX(), box.getMaxX(), 
				box.getMinY(), box.getMaxY());

			this.hull     = hull;
			this.object   = object;
			this.type     = type;
			this.previous = previous;

			number = previous != null
				? previous.number + 1
				: 0;
		}

		void addParent(Entry parent)  {
			if (parents == null)
				parents = new ArrayList(20);
			parents.add(parent);
		}

		void addChild()  {
			++numChildren;
		}

		boolean removeChild() {
			return --numChildren <= 0;
		}

		boolean hasParents() {
			return parents != null && !parents.isEmpty();
		}

		int numParents() {
			return parents != null 
				? parents.size()
				: 0;
		}

		void clear() {
			object = null;
			type   = null;
		}

		void releaseParents(TreeMap free) {
			if (parents != null) {
				for (Iterator i = parents.iterator(); i.hasNext();) {
					Entry parent = (Entry)i.next();
					if (parent.removeChild()) {
						TreeSet list = (TreeSet)free.get(parent.type);
						if (list == null)
							free.put(parent.type, list = new TreeSet());
						list.add(parent);
					}
				}
				parents.clear();
				parents = null;
			}
		}

		public int numChildren() {
			return numChildren;
		}

		public boolean hasChildren() {
			return numChildren > 0;
		}

		public boolean equals(Object other) {
			return compareTo(other) == 0;
		}

		public int hashCode() {
			return number;
		}

		public int compareTo(Object other) {

			int diff = number - ((Entry)other).number;
			
			if (diff < 0) return -1;
			if (diff > 0) return +1;
			return 0;
		}
	}

	private static final void addRectangle2D(
		Rectangle2D     r, 
		GeometryFactory factory,
		ArrayList       list
	) {
		list.add(factory.createPoint(new Coordinate(r.getMinX(), r.getMinY())));
		list.add(factory.createPoint(new Coordinate(r.getMinX(), r.getMaxY())));
		list.add(factory.createPoint(new Coordinate(r.getMaxX(), r.getMaxY())));
		list.add(factory.createPoint(new Coordinate(r.getMaxX(), r.getMinY())));
	}

	public static Geometry getConvexHull(Shape shape) {

		ArrayList list = new ArrayList(512);

		float [] data = new float[6];
		PathIterator pi = shape.getPathIterator(null);

		GeometryFactory factory = new GeometryFactory();

		float x = 0f;
		float y = 0f;

		while (!pi.isDone()) {
			switch (pi.currentSegment(data)) {
				case PathIterator.SEG_CLOSE:
					break;
				case PathIterator.SEG_CUBICTO:
					CubicCurve2D cubic = new CubicCurve2D.Float(
						x, y, data[0], data[1], data[2], data[3], data[4], data[5]);
					addRectangle2D(cubic.getBounds2D(), factory, list);
					x = data[4]; y = data[5];
					break;
				case PathIterator.SEG_QUADTO:
					QuadCurve2D quad = new QuadCurve2D.Float(
						x, y, data[0], data[1], data[2], data[3]);
					addRectangle2D(quad.getBounds2D(), factory, list);
					x = data[2]; y = data[3];
					break;
				case PathIterator.SEG_LINETO:
				case PathIterator.SEG_MOVETO:
					Coordinate coord = new Coordinate(x = data[0], y = data[1]);
					list.add(factory.createPoint(coord));
					break;
			}

			pi.next();
		}

		return factory.buildGeometry(list).convexHull();
	}

	public OverlapSorter() {
	}

	public void prepareSpatialIndex(int N) {
		spatial = new STRtree();
	}

	public void add(Shape shape, Element object, ElementAttributes type) {

		Rectangle2D b = shape.getBounds2D();
		Geometry    h = getConvexHull(shape);

		Entry entry = new Entry(b, h, object, type, previous);
		spatial.insert(entry.envelope, entry);
		previous = entry;
	}

	public void buildSpatialIndex() {
		spatial.build();
	}

	public void performOverlapTest() {

		final int [] count = new int[1];

		for (Entry e = previous; e != null; e = e.previous) {

			final Entry entry = e;

			// find all intersecting leaves
			spatial.query(e.envelope, new ItemVisitor() {

				public void visitItem(Object data) {

					Entry other = (Entry)data;

					// if entry was inserted earlier it has to be rendered earlier
					if (other.number < entry.number 
					&& !entry.type.equals(other.type)
					&& entry.hull.intersects(other.hull)) {
						other.addParent(entry);
						entry.addChild();
					}
					else if (other.number == entry.number) {
						if ((++count[0] % 10000) == 0)
							System.err.println("intersected: " + count[0]);
					}
				} // visitItem
			}); // spatial query
		}; // for all entries

		// not needed any more
		spatial = null;
	}

	public interface Visitor {
		void visit(Element object, ElementAttributes type);
	}

	public void topological(Visitor visitor) {

		final TreeMap free = new TreeMap();

		Entry entry = previous;

		// find all of top level
		for (entry = previous; entry != null;) {
			if (!entry.hasChildren()) {
				TreeSet list = (TreeSet)free.get(entry.type);
				if (list == null)
					free.put(entry.type, list = new TreeSet());
				list.add(entry);
			}
			Entry p = entry.previous;
			entry.previous = null;
			entry = p;
		}

		previous = null;

		TreeSet current; 

		for (;;) {
			int max = Integer.MIN_VALUE;
			current = null;

			// find the list covering the most objects
			for (Iterator i = free.values().iterator(); i.hasNext();) {
				TreeSet list = (TreeSet)i.next();

				int sum = 0;
				for (Iterator j = list.iterator(); j.hasNext();)
					sum += ((Entry)j.next()).numParents();
				
				if (sum >= max) { 
					// if same parents count only if new one is longer
					if (sum == max && current != null && current.size() >= list.size())
						continue;
					current = list; 
					max = sum; 
				}
			}

			if (current == null || current.isEmpty())
				break;

			// flush the list
			while (!current.isEmpty()) {
				entry = (Entry)current.first();
				current.remove(entry);
				entry.releaseParents(free);
				visitor.visit(entry.object, entry.type);
				entry.clear();
			}
		}
	} // topological
}
// end of file
