/*
 * PickingInteractor.java
 * ------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Component;
import java.awt.Color;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;

import org.apache.batik.swing.gvt.InteractorAdapter;
import org.apache.batik.swing.gvt.Overlay;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SVGGraphicsElement;

import de.intevation.printlayout.MatrixTools;
import de.intevation.printlayout.DocumentManager;

import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGDocument;

import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;

public class PickingInteractor
extends      InteractorAdapter
implements   Overlay, Tool
{
	public static final String IDENTIFIER = "picking-tool";

	protected boolean inUse;
	protected boolean finished;
	
	protected DocumentManager documentManager;

	protected ArrayList       selected;

	public PickingInteractor() {
	}

	public PickingInteractor(DocumentManager documentManager) {
		setDocumentManager(documentManager);
	}
	
	public void setDocumentManager(DocumentManager documentManager) {
		this.documentManager = documentManager;
	}

	public DocumentManager getDocumentManager() {
		return documentManager;
	}

	public String getToolIdentifier() {
		return IDENTIFIER;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public boolean getInUse() {
		return inUse;
	}

	public boolean startInteraction(InputEvent ie) {
		if (!inUse) return false;
		int mods = ie.getModifiers();

		boolean start = (mods & InputEvent.BUTTON1_MASK) != 0;

		if (start)
			finished = false;

		return start;
	}
	
	public boolean endInteraction() {
		return finished;
	}
	
	public void mouseClicked(MouseEvent me) {

		finished = true;
		
		int x = me.getX();
		int y = me.getY();

		SVGDocument document = documentManager.getSVGDocument();

		SVGSVGElement element =
			(SVGSVGElement)document.getElementById(DocumentManager.DOCUMENT_SHEET);
		
		AffineTransform xform;
		try {
			xform	= MatrixTools.toJavaTransform(
				element.getScreenCTM().inverse());
		}
		catch (SVGException se) {
			se.printStackTrace();
			return;
		}

		Point2D screenPoint   = new Point2D.Double(x, y);
		Point2D documentPoint = new Point2D.Double();

		xform.transform(screenPoint, documentPoint);

		SVGRect query = element.createSVGRect();
		query.setX((float)documentPoint.getX());
		query.setY((float)documentPoint.getY());

		query.setWidth(0.5f); // half mm
		query.setHeight(0.5f);

		NodeList result = element.getIntersectionList(query, null);

		int N = result.getLength();

		HashSet   alreadyFound = new HashSet();
		ArrayList ordered      = new ArrayList();

		for (int i = 0; i < N; ++i) {
			AbstractElement obj = (AbstractElement)result.item(i);
			AbstractElement last = null;
			do {
				AbstractElement parent = (AbstractElement)obj.getParentNode();
				if (parent == null)
					break;
				String id = parent.getAttributeNS(null, "id");
				if (id != null && id.startsWith(DocumentManager.OBJECT_ID)) {
					last = parent;
				}
				obj = parent;
			}
			while (obj != null && obj != element);

			if (last != null && alreadyFound.add(last.getAttributeNS(null, "id")))
				ordered.add(last);
		}

		N = ordered.size();

		if (N > 0) {
			selected = ordered;
			/*
			System.out.println("found items:");
			for (int i = 0; i < N; ++i) {
				AbstractElement obj = (AbstractElement)ordered.get(i);
				System.out.println("\t'" +  obj.getAttributeNS(null, "id")+ "'");
			}
			*/
		}
		else
			selected = null;

		((Component)me.getSource()).repaint();
	}
  
	public void paint(Graphics g) {
		if (!inUse || selected == null)
			return;

		Graphics2D g2d = (Graphics2D)g;

		for (int N = selected.size(), i = 0; i < N; ++i) {
			SVGGraphicsElement element =
				(SVGGraphicsElement)selected.get(i);

			SVGRect bbox = element.getBBox();
      SVGMatrix matrix = element.getScreenCTM();

			AffineTransform xform = MatrixTools.toJavaTransform(matrix);

			Rectangle2D rect = new Rectangle2D.Float(
				(float)bbox.getX(), 
				(float)bbox.getY(),
				(float)bbox.getWidth(),
				(float)bbox.getHeight());

			GeneralPath path = new GeneralPath(rect);

			Shape xpath = path.createTransformedShape(xform);

			g2d.setPaint(Color.red);

			g2d.draw(xpath);
		}
	}
}
// end of file
