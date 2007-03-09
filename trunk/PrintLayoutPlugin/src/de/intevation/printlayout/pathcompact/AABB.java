/*
 * PathCompactor.java
 * ------------------
 * (c) 2007 by Sascha L. Teichmann (teichmann@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.pathcompact;

import java.util.Stack;

import java.awt.geom.Rectangle2D;

public final class AABB
{
	private AABB() {
	}

	public static class Box {

		public static final int DISJOINT       = 0;
		public static final int COVERS         = 1;
		public static final int IS_COVERED     = 2;
		public static final int INTERSECTS     = 4;
		public static final int INTERSECT_BITS = 4;
		public static final int INTERSECT_MASK = ~(~0 << INTERSECT_BITS);

		public static final int IS_COVERED_BOTH = 
			IS_COVERED | (IS_COVERED <<INTERSECT_BITS);

		public static final int COVERS_BOTH = 
			COVERS | (COVERS <<INTERSECT_BITS);

		protected double x1;
		protected double y1;
		protected double x2;
		protected double y2;

		public Box() {
		}

		public Box(Rectangle2D rect) {
			x1 = rect.getX();
			y1 = rect.getY();
			x2 = x1 + rect.getWidth();
			y2 = y1 + rect.getHeight();
		}

		public Box(double x1, double y1, double x2, double y2) {
			if (x1 < x2) { this.x1 = x1; this.x2 = x2; }
			else         { this.x1 = x2; this.x2 = x1; }
			if (y1 < y2) { this.y1 = y1; this.y2 = y2; }
			else         { this.y1 = y2; this.y2 = y1; }
		}


		public final void set(Box other) {
			x1 = other.x1;
			y1 = other.y1;
			x2 = other.x2;
			y2 = other.y2;
		}

		public final double volume() {
			return width()*height();
		}

		public final double width() {
			return x2-x1;
		}

		public final double height() {
			return y2-y1;
		}

		public final Box union(Box other) {
			return union(other, new Box());
		}

		public final Box union(Box other, Box result) {
			result.x1 = Math.min(x1, other.x1);
			result.y1 = Math.min(y1, other.y1);
			result.x2 = Math.max(x2, other.x2);
			result.y2 = Math.max(y2, other.y2);
			return result;
		}

		public final int intersects(Box other) {

			if (x1 > other.x2 || other.x1 > x2 || y1 > other.y2 || other.y1 > y2)
				return DISJOINT;

			if (x1 <= other.x1 && x2 >= other.x2 && y1 <= other.y1 && y2 >= other.y2)
				return COVERS;

			if (x1 > other.x1 && x2 < other.x2 && y1 > other.y1 && y2 < other.y2)
				return IS_COVERED;

			return INTERSECTS;
		}

		public boolean equalsBox(Box box) {
			return x1 == box.x1 && x2 == box.x2 && y1 == box.y1 && y2 == box.y2;
		}
	} // class Box

	public static class Tree
	{
		public interface QueryCallback {

			boolean found(Node node, int intersectionTyp);
		}

		public static final class Node 
		extends                   Box
		{
			protected Node   left;
			protected Node   right;
			protected Node   parent;

			protected Object data;

			public Node() {
			}

			public Node(Box box) {
				set(box);
			}

			public Object getData() {
				return data;
			}

			public Node(Box box, Object data) {
				set(box);
				this.data = data;
			}

			public boolean recalcBox() {
				Box box = left != null
					? (right != null ? left.union(right) : left)
					: (right != null ? right : this);
				if (!equalsBox(box)) {
					set(box);
					return true;
				}
				return false;
			}

			public final void replaceChild(Node oldChild, Node newChild) {
				     if (left  == oldChild) left  = newChild;
				else if (right == oldChild) right = newChild;
				else
					throw new IllegalArgumentException("child not found");
			}

			public Node sibling(Node node) {
				if (node == left ) return right;
				if (node == right) return left;
				throw new IllegalArgumentException("child not found");
			}

			public final boolean isLeaf() {
				return data != null;
			}

			public void clear() {
				left   = null;
				right  = null;
				parent = null;
				data   = null;
			}
		} // class Node

		protected Node root;

		public Tree() {
			root = new Node();
		}

		public Node insert(Box box, Object data) {

			if (root.left == null) { // no child
				root.left = new Node(box, data);
				root.left.parent = root;
				return root.left;
			}

			// root has a child
			switch (box.intersects(root.left)) {

				case Box.COVERS:
					return replaceRootNode(box, data, box);

				case Box.IS_COVERED:
					if (root.left.isLeaf())
						return replaceRootNode(box, data, root.left);
					return recursiveInsert(root.left, box, data);

				case Box.DISJOINT:
				case Box.INTERSECTS:
					if (root.left.isLeaf())
						return replaceRootNode(box, data, box.union(root.left));
					root.left.set(box.union(root.left));
					return recursiveInsert(root.left, box, data);
			}
			// never reached
			return null;
		}

		protected final Node replaceRootNode(
			Box box, Object data,
			Box volume
		) {
			Node dataNode = new Node(box, data);
			Node nroot = new Node(volume);
			nroot.right = dataNode;
			nroot.left  = root.left;
			dataNode.parent  = nroot;
			root.left.parent = nroot;
			root.left = nroot;
			return dataNode;
		}

		protected static final Node createNode(
			Box box, Object data,
			Box volume, 
			Node sibling, Node parent
		) {
			Node dataNode = new Node(box, data);
			Node node = new Node(volume);
			node.left  = dataNode;
			node.right = sibling;
			dataNode.parent = node;
			sibling .parent = node;
			node.parent     = parent;
			parent.replaceChild(sibling, node);
			return dataNode;
		}


		protected final Node recursiveInsert(Node parent, Box box, Object data) {

			Box a = new Box(); // for union volume calculations
			Box b = new Box();

			for (;;) {
				Node left  = parent.left;
				Node right = parent.right;
				// each inner node has two children
				int intersections = 
						 box.intersects(left)
					| (box.intersects(right) << Box.INTERSECT_BITS);

				if ((intersections & Box.IS_COVERED_BOTH) != 0) {
					// box inside something
					Node n;
					if ((intersections & Box.IS_COVERED_BOTH) == Box.IS_COVERED_BOTH)
						// inside both
						// -> prefer one with smaller volume
						n =  left.volume() <  right.volume()
							? left
							: right;
					else // inside only one
						n = (intersections & Box.IS_COVERED) == Box.IS_COVERED
							? left
							: right;

					if (n.isLeaf())
						return createNode(box, data, n, n, parent);

					// send down next level
					parent = n;
					continue;
				}

				if ((intersections & Box.COVERS_BOTH) != 0) {
					// box covers one or both children?
					Node n;
					if ((intersections & Box.COVERS_BOTH) == Box.COVERS_BOTH)
						// box equals bounding box
						// -> prefer one with bigger volume (XXX: heuristic okay?)
						n =  left.volume() >  right.volume()
							? left
							: right;
					else
						// covers only one
						n = (intersections & Box.COVERS) == Box.COVERS
							? left
							: right;

					return createNode(box, data, box, n, parent);
				}

				// disjoint and intersect cases
				
				box.union(left,  a);
				box.union(right, b);

				Node n;
				Box volume;

				// prefer lower volume
				if (a.volume() < b.volume()) { volume = a; n = left;  }
				else                         { volume = b; n = right; }

				if (n.isLeaf())
					return createNode(box, data, volume, n, parent);
				// widen and down
				n.set(volume);
				parent = n;
			} // for (;;)
		}

		public void remove(Node node) {
			Node p  = node.parent;
			Node pp = p.parent;
			if (pp == null) {// last node
				root.left = null;
				node.clear();
				return;
			}

			pp.replaceChild(p, p.sibling(node));
			p.clear();
			node.clear();

			// reduce the size of the bboxes uptree
			for (;pp != root && pp.recalcBox(); pp = pp.parent);
		}

		public boolean walkAll(QueryCallback qc) {
			Node node = root.left;
			return node != null
				? dumpAll(node, qc)
				: true;
		}

		public void clear() {
			Node node = root.left;

			if (node == null)
				return;

			root.left = null;

			Stack stack = new Stack();

			for (;;) {
				do {
					Node left = node.left;
					Node right = node.right;
					node.clear();
					if (right != null)
						stack.push(right);
					node = left;
				}
				while (node != null);

				if (stack.empty())
					break;
				node = (Node)stack.pop();
			}
		}


		protected static boolean dumpAll(Node node, QueryCallback qc) {

			if (node.isLeaf())
				return qc.found(node, Box.COVERS);

			Stack stack = new Stack();

			for (;;) {
				if (node.left.isLeaf()) {
					if (!qc.found(node.left, Box.COVERS))
						return false;
				}
				else
					stack.push(node.left);

				if (node.right.isLeaf()) {
					if (!qc.found(node.right, Box.COVERS))
						return false;
				}
				else {
					node = node.right;
					continue;
				}

				if (stack.empty())
					break;
				node = (Node)stack.pop();
			}
			return true;
		}

		public boolean query(Box box, QueryCallback qc) {

			Node node = root.left;

			if (node == null)
				return true;

			Stack stack = new Stack();

			for (;;) {
				int intersectionType = box.intersects(node);

				switch (intersectionType) {
					case Box.DISJOINT:
						break;

					case Box.COVERS:
						if (!dumpAll(node, qc))
							return false;
						break;

					default:
						if (node.isLeaf()) {
							if (!qc.found(node, intersectionType))
								return false;
							break;
						}
						stack.push(node.left);
						node = node.right;
						continue;
				}

				if (stack.empty())
					break;

				node = (Node)stack.pop();
			}
			return true;
		}
	} // class Tree

}
// end of file
