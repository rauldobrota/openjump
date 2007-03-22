/*
 * PathCompactor.java
 * ------------------
 * (c) 2007 by Sascha L. Teichmann (teichmann@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.pathcompact;

import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.DocumentFragment;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;  

import javax.xml.transform.dom.DOMSource;
  
import javax.xml.transform.stream.StreamResult;

import java.util.Stack;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Iterator;

import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.apache.batik.parser.AWTTransformProducer;
import org.apache.batik.parser.ParseException;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;



public class PathCompactor
{
	/**
	 * If the system property "de.intevation.printlayout.max.joined.paths"
	 * is set to an integer value, the value is used to control the number
	 * of paths that are joined into one. If the value lesser or equals 1
	 * no limit is set and all possible joins are done. Else the
	 * value determines the maximal number of joined paths. It defaults
	 * to 250.
	 */
	public static final int MAX_JOINED_PATHS =
		Integer.getInteger("de.intevation.printlayout.max.joined.paths", 250).intValue();

	private static final Element pathGroup(Element group) {
		int howMany = 0;
		Element pathElement = null;
		NodeList children = group.getChildNodes();
		for (int i = children.getLength()-1; i >= 0; --i) {
			Node child = children.item(i);
			if (child instanceof Element) {
				Element element = (Element)child;
				if (element.getTagName().equals("path")) {
					if (++howMany > 1)
						return null;
					pathElement = element;
				}
				else return null;
			} // it's an element
		} // for all children

		return pathElement;
	}


	private static Shape elementToShape(Element path) {
		AWTPathProducer handler = new AWTPathProducer();
		PathParser parser = new PathParser();
		parser.setPathHandler(handler);
		parser.parse(path.getAttributeNS(null, "d"));
		Shape shape = handler.getShape();

		Element parent = (Element)path.getParentNode();

		if (parent != null) {
			String xform = parent.getAttributeNS(null, "transform");
			if (xform != null && xform.length() > 0)
				try {
					AffineTransform matrix =
						AWTTransformProducer.createAffineTransform(xform);

					GeneralPath gp = new GeneralPath(shape);
					gp.transform(matrix);
					shape = gp;
				}
				catch (ParseException pe) {
					pe.printStackTrace();
				}
		}
		return shape;
	}

	public static void reorder(final Document document) {
		reorder(document, document.getDocumentElement());
	}

	public static void reorder(final Document document, final Element root) {

		Stack stack = new Stack();
		stack.push(root);

		ArrayList paths = new ArrayList();

		TreeMap       types         = new TreeMap();
		OverlapSorter overlapSorter = new OverlapSorter();
		ArrayList     remove        = new ArrayList();

		while (!stack.empty()) {
			Element current = (Element)stack.pop();
			NodeList children = current.getChildNodes();

			for (int N = children.getLength(), i = 0; i < N; ++i) {
				Node child = children.item(i);
        // ignore Texts, Comments, etc.
				if (!(child instanceof Element))
					continue;

				Element element = (Element)child;
				String  tagName = element.getTagName();

				// look if its a path in group
				if (tagName.equals("g")) {

					Element path = pathGroup(element);

					if (path != null) {
						paths.add(path);
						continue;
					}

					stack.push(element);
				} // group found
				else {
					if (tagName.equals("svg"))
						stack.push(element);
				}

				int P = paths.size();
				if (P > 0) {
					System.err.println("Paths found: "+ P);
					overlapSorter.prepareSpatialIndex(P);
					System.err.println("Figure out number of types");
					for (int j = 0; j < P; ++j) {
						if (((j+1) % 10000) == 0)
							System.err.println((j+1) + " classified");
						Element path  = (Element)paths.get(j);
						Element group = (Element)path.getParentNode();

						remove.add(group);

						ElementAttributes typeKey = new ElementAttributes(group);

						ElementAttributes type = (ElementAttributes)types.get(typeKey);
						if (type == null)
							types.put(type = typeKey, typeKey);

						overlapSorter.add(elementToShape(path), path, type);
					}
					System.err.println("Sorting in done. types: " + types.size());
					System.err.println("Building spatial index");
					overlapSorter.buildSpatialIndex();
					System.err.println("Building spatial index done");
					overlapSorter.performOverlapTest();
					System.err.println("Overlap test done");

					final DocumentFragment fragment =
						document.createDocumentFragment();

					final int [] newGroups = new int[1];

					overlapSorter.topological(new OverlapSorter.Visitor() {

						ElementAttributes  lastElementAttributes;
						Element            lastGroup;

						public void visit(Element path, ElementAttributes type) {

							Element group;
							
							if (lastElementAttributes == null 
							|| !lastElementAttributes.equals(type)) {
								group = document.createElementNS(
									SVGDOMImplementation.SVG_NAMESPACE_URI, "g");
								type.attributize(group);
								fragment.appendChild(group);
								lastElementAttributes  = type;
								lastGroup = group;
								++newGroups[0];
							}
							else
								group = lastGroup;

							path.getParentNode().removeChild(path);
							group.appendChild(path);
						}
					});

					System.err.println(
						"Built new groups: " + 
						newGroups[0] + " avg: " + ((float)P/newGroups[0]));

					int R = remove.size();
					if (R > 0) {
						Element g = (Element)remove.get(0);
						g.getParentNode().replaceChild(fragment, g);
						for (int j = 1; j < R; ++j) {
							g = (Element)remove.get(j);
							g.getParentNode().removeChild(g);
						}
						remove.clear();
					}

					types.clear();
					paths.clear();
				}
			} // for all children
		} // while stack not empty
	}

	public static void compactPathElements(Document document) {
		compactPathElements(document.getDocumentElement());
	}

	public static void compactPathElements(Element root) {

		Stack stack = new Stack();
		stack.push(root);

		ArrayList ranges = new ArrayList();
		ArrayList range  = new ArrayList();
		ArrayList chunks = new ArrayList();

		ArrayList remove = new ArrayList();

		PathOptimizer optimizer = new PathOptimizer();

		int total    = 0;
		int numPaths = 0;

		while (!stack.empty()) {
			Element current = (Element)stack.pop();
			NodeList children = current.getChildNodes();

			for (int N = children.getLength(), i = 0; i < N; ++i) {
				Node child = children.item(i);
        // ignore Texts, Comments, etc.
				if (!(child instanceof Element)) {
					remove.add(child);
					continue;
				}

				Element element = (Element)child;
				String tagName = element.getTagName();

				if (!tagName.equals("path")) {
					if (range.size() > 1) {
						ranges.add(range);
						range = new ArrayList();
					}
					else
						optimizeOneElementRange(range, optimizer);

					if (tagName.equals("g") || tagName.equals("svg"))
						stack.push(child);

					continue;
				}
				if ((++numPaths % 10000) == 0)
					System.err.println("Paths compacted: " + numPaths);

				range.add(element);
			} // for all children

			for (int i = remove.size()-1; i >= 0; --i)
				current.removeChild((Node)remove.get(i));
			remove.clear();
			

			if (range.size() > 1) {
				ranges.add(range);
				range = new ArrayList();
			}
			else
				optimizeOneElementRange(range, optimizer);

			children = null;

			for (int i = ranges.size()-1; i >= 0; --i) {
				ArrayList subRanges = (ArrayList)ranges.get(i);
				for (Iterator s = split(subRanges, MAX_JOINED_PATHS, 2); s.hasNext();) {
					range = (ArrayList)s.next();
					int R = range.size();
					Element last = (Element)range.get(R-1);
					for (int j = R-2; j >= 0; --j) {

						Element before = (Element)range.get(j);

						if (match(last, before)) {
							chunks.add(last.getAttributeNS(null, "d").trim());
							current.removeChild(last);
						}
						else
							total += flushChunks(last, chunks, optimizer);

						last = before;
					}
					total += flushChunks(last, chunks, optimizer);
					range.clear();
				} // for all sub ranges
				subRanges.clear();
			} // for all ranges

			ranges.clear();
			chunks.clear();
		} // while stack not empty

		System.err.println("Paths total: " + numPaths);
		System.err.println("Paths compacted: " + total);
		System.err.println("Paths left: " + (numPaths - total));
	}

	private static final void optimizeOneElementRange(
		ArrayList     range,
		PathOptimizer optimizer
	) {
		if (!range.isEmpty()) {
			Element element = (Element)range.get(0);
			range.clear();

			PathParser parser = new PathParser();
			parser.setPathHandler(optimizer);
			parser.parse(element.getAttributeNS(null, "d"));
			element.setAttributeNS(null, "d", optimizer.generate());
			optimizer.clear();
		}
	}

	/**
	 * Splits list into a sequence of lists with
	 * each list length at least 'atLeast' and maximum 'max'
	 * elements.
	 * @param list    the list to split
	 * @param max     the maximum
	 * @param atLeast them minimum size
	 * @return an iterator over the splitted list.
	 */
	private static Iterator split(
		final ArrayList list, 
		final int       max, 
		final int       atLeast
	) {
		if (max <= 1 || list.size() <= max) // do not split if small enough
			return new Iterator() {
				boolean send;

				public boolean hasNext() {
					return !send;
				}

				public Object next() {
					send = true;
					return list;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};

		return new Iterator() {

			int       count;
			ArrayList current;

			{ advance(); }

			private void advance() {
				int N = list.size();
				if (count >= N)
					current = null;
				else {
					int R = N-count;
					int T = Math.min(R, Math.max(atLeast, Math.min(max, R)));
					current = new ArrayList(T);
					for (T += count; count < T; ++count)
						current.add(list.get(count));
				}
			}

			public boolean hasNext() {
				return current != null;
			}

			public Object next() {
				Object x = current;
				advance();
				return x;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static final int flushChunks(
		Element       last, 
		ArrayList     chunks,
		PathOptimizer optimizer
	) {
		int C = chunks.size();
		if (C == 0)
			return 0;

		// pre calculate capacity to avoid reallocations
		int total = ((String)chunks.get(0)).length() + C;
		for (int k = 1; k < C; ++k)
			total += ((String)chunks.get(k)).length(); 

		String dLast = last.getAttributeNS(null, "d");

		total += dLast.length();

		StringBuffer sb = new StringBuffer(total);

		sb.append(dLast);

		dLast = null;

		for (int k = C-1; k >= 0; --k)
			sb.append(' ').append((String)chunks.get(k));

		chunks.clear();

		String str = sb.toString();

		sb = null;

		PathParser parser = new PathParser();
		parser.setPathHandler(optimizer);
		parser.parse(str);

		last.setAttributeNS(null, "d", optimizer.generate());
		optimizer.clear();

		return C;
	}

	private static final boolean match(Element a, Element b) {
		NamedNodeMap aa = a.getAttributes();
		NamedNodeMap ab = b.getAttributes();

		if (aa == null || ab == null)
			return false;

		int A = aa.getLength();
		int B = ab.getLength();

		if (A != B)
			return false;

		for (int i = 0; i < A; ++i) {
			Node na = aa.item(i);
			String aname = na.getNodeName();
			if (aname.equals("d"))
				continue;

			Node nb = aa.getNamedItem(aname);
			if (nb == null)
				return false;

			String avalue = na.getNodeValue();
			String bvalue = nb.getNodeValue();

			if (avalue == null) {
				if (bvalue != null)
					return false;
			}
			else
				if (bvalue == null || !avalue.equals(bvalue))
					return false;
		}

		return true;
	}

	public static void main(String [] args) {

		for (int i = 0; i < args.length; ++i) {
			File file = new File(args[i]);
			try {
				DocumentBuilderFactory factory =
					DocumentBuilderFactory.newInstance();

				DocumentBuilder builder =
					factory.newDocumentBuilder();

				System.err.println("Parsing");
				Document document =
					builder.parse(file);

				System.err.println("Parsing done");
				reorder(document);
				System.err.println("Reorder done");
				System.err.println("Compact paths");
				compactPathElements(document);
				System.err.println("Compact done");

				TransformerFactory tFactory =
					TransformerFactory.newInstance();

				Transformer transformer = tFactory.newTransformer();

				DOMSource source = new DOMSource(document);

				BufferedOutputStream out = null;

				try {
					File parent = file.getParentFile();

					String name = "compacted-" + file.getName();

					File outFile = parent != null
						? new File(parent, name)
						: new File(name);

					out =
						new BufferedOutputStream(
						new FileOutputStream(outFile));

					StreamResult result = new StreamResult(out);
					transformer.transform(source, result);
				}
				finally {
					if (out != null) {
						try { out.flush(); } catch (IOException ioe) {}
						try { out.close(); } catch (IOException ioe) {}
						out = null;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
// end of file
