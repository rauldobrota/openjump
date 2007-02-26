/*
 * PickingInteractor.java
 * ----------------------
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
import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import org.apache.batik.swing.gvt.InteractorAdapter;
import org.apache.batik.swing.gvt.Overlay;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SVGGraphicsElement;

import de.intevation.printlayout.util.MatrixTools;
import de.intevation.printlayout.DocumentManager;
import de.intevation.printlayout.LayoutCanvas;

import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGDocument;

import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.EventListener;

import de.intevation.printlayout.util.LineProjector;
import de.intevation.printlayout.util.GeometricMath;

public class PickingInteractor
extends      InteractorAdapter
implements   Overlay, Tool, LayoutCanvas.DamagedRegion
{
	public static final String IDENTIFIER = "picking-tool";


	public static class PickingEvent
	extends             EventObject
	{
		public PickingEvent(Object source) {
			super(source);
		}
	} // class PickingEvent

	public interface PickingListener
	extends          EventListener
	{
		void selectionChanged(PickingEvent evt);

	} // interface PickingListener

	public interface TransformOperation {
		void transform(
			String []       ids, 
			DocumentManager documentManager, 
			Point2D         delta,
			Point2D         orginal);
	} // interface TransformOperation

	public static final TransformOperation TRANSLATE =
		new TransformOperation() {
			public void transform(
				String []       ids,
				DocumentManager documentManager,
				Point2D         delta,
				Point2D         orginal
			) {
				documentManager.translateIDs(ids, delta);
			}
		}; // TRANSLATE

	public static class Scale
	implements          TransformOperation
	{
		protected LineProjector proj;

		public Scale(LineProjector proj) {
			this.proj = proj;
		}

		protected void projectedTransform(
			String []       ids,
			DocumentManager documentManager,
			Point2D         delta,
			Point2D         orginal
		) {
			documentManager.scaleFixedIDs(
				ids, delta, orginal, proj.getOrigin());
		}

		public void transform(
			String []       ids,
			DocumentManager documentManager,
			Point2D         delta,
			Point2D         orginal
		) {
			orginal = proj.nearestPointOnLine(orginal);
			Point2D ndelta = GeometricMath.add(orginal, delta);
			ndelta = proj.nearestPointOnLine(ndelta);
			ndelta = GeometricMath.sub(ndelta, orginal);
			projectedTransform(ids, documentManager, ndelta, orginal);
		}
	} // Scale

	public static class NoneUniformScale
	extends             Scale
	{
		public NoneUniformScale(LineProjector proj) {
			super(proj);
		}

		protected void projectedTransform(
			String []       ids,
			DocumentManager documentManager,
			Point2D         delta,
			Point2D         orginal
		) {
			documentManager.scaleNoneUniformFixedIDs(
				ids, delta, orginal, proj.getOrigin());
		}
	} // NoneUniformScale

	public static class Shear
	extends             Scale
	{
		Point2D ref;

		public Shear(LineProjector proj, Point2D ref) {
			super(proj);
			this.ref   = ref;
		}

		protected void projectedTransform(
			String []       ids,
			DocumentManager documentManager,
			Point2D         delta,
			Point2D         orginal
		) {
			documentManager.shearNoneUniformFixedIDs(
				ids, delta, orginal, proj.getOrigin(), ref);
		}
	} // Shear

	public static final TransformOperation ROTATE =
		new TransformOperation() {
			public void transform(
				String []       ids,
				DocumentManager documentManager,
				Point2D         delta,
				Point2D         orginal
			) {
				documentManager.rotateIDs(ids, delta, orginal);
			}
		}; // ROTATE

	public static final int NO_DECORATION     = 0;
	public static final int SCALE_DECORATION  = 1;
	public static final int ROTATE_DECORATION = 2;

	public static final int MIN_DECORATION    = 1;
	public static final int MAX_DECORATION    = 2;

	protected boolean inUse;
	protected boolean finished;
	
	protected DocumentManager documentManager;

	protected ArrayList       selected;

	protected ArrayList       listeners;

	// used for dragging selected items
	protected int startX;
	protected int startY;

	protected int renderTime = -1;

	protected TransformOperation transformOperation;

	protected int decoration = SCALE_DECORATION;

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

	public void addPickingListener(PickingListener listener) {
		if (listener != null) {
			if (listeners == null) {
				listeners = new ArrayList(5);
				listeners.add(listener);
			}
			else if (!listeners.contains(listeners))
				listeners.add(listener);
		}
	}

	public void removePickingListener(PickingListener listener) {
		if (listener  != null
		&&  listeners != null
		&&  listeners.remove(listener) 
		&&  listeners.isEmpty());
			listeners = null;
	}

	public String getToolIdentifier() {
		return IDENTIFIER;
	}

	public void setInUse(boolean flag) {
		if (inUse != flag) {
			inUse = flag;
			clearSelection();
			LayoutCanvas canvas = documentManager.getCanvas();
			if (inUse)
				canvas.addDamagedRegion(this);
			else
				canvas.removeDamagedRegion(this);
			canvas.repaint();
		}
	}

	public boolean getInUse() {
		return inUse;
	}

	public boolean startInteraction(InputEvent ie) {
		if (!inUse) return false;

		boolean start = ie instanceof MouseEvent
			&& (ie.getModifiers() & InputEvent.BUTTON1_MASK)
				== InputEvent.BUTTON1_MASK;

		if (start)
			finished = false;

		return start;
	}
	
	public boolean endInteraction() {
		return finished;
	}

	protected static final boolean isShiftDown(int modifiers) {
		return (modifiers & InputEvent.SHIFT_MASK) != 0;
	}

	protected static final boolean isCtrlDown(int modifiers) {
		return (modifiers & InputEvent.CTRL_MASK) != 0;
	}
	
	public void mouseClicked(MouseEvent me) {

		finished = true;

		int modifiers = me.getModifiers();

		if ((modifiers & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)
			return;

		int x = me.getX();
		int y = me.getY();

		// remember id if only one was selected before
		String singleSelection = selected != null && selected.size() == 1
			? ((OnScreenBox)selected.get(0)).getID()
			: null;

		ArrayList result = query(x, y);

		int N = result.size();

		boolean changed = false;

		if (isCtrlDown(modifiers)) { // add to selected
			if (N > 0) {
				Object last = result.get(N-1);
				if (selected == null) { // empty list
					selected = new ArrayList();
					selected.add(last);
					changed = true;
				}
				else {
					if (!selected.contains(last)) {
						selected.add(last);
						changed = true;
					}
				}
			}
		}
		else if (isShiftDown(modifiers)) { // remove from selection
			if (N > 0 && selected != null) {
				Object last = result.get(N-1);

				if (selected.remove(last)) {
					changed = true;
					if (selected.isEmpty())
						resetSelection();
				}
			}
		}
		else { // select only one. if missed target deselect all
			if (N > 0) {
				if (selected != null) selected.clear();
				else                  selected = new ArrayList();
				selected.add(result.get(N-1));
			}
			else
				resetSelection();

			changed = true;
		}

		// clicked on a already select object changes decoration
		if (singleSelection != null 
		&& selected != null
		&& selected.size() == 1
		&& ((OnScreenBox)selected.get(0)).getID().equals(singleSelection))
			advanceDecoration();

		((Component)me.getSource()).repaint();

		if (changed)
			fireSelectionChanged();
	}

	protected void fireSelectionChanged() {
		if (listeners != null) {
			PickingEvent evt = new PickingEvent(this);
			for (int i = listeners.size()-1; i >= 0; --i)
				((PickingListener)listeners.get(i)).selectionChanged(evt);
		}
	}

	/** spatial query all for all objects around (x, y) in screen coordinate */
	protected ArrayList query(int x, int y) {
		return query(x, y, true);
	}

	protected ArrayList query(int x, int y, boolean directlyInSheet) {

		ArrayList ordered = new ArrayList();

		SVGDocument document = documentManager.getSVGDocument();

		SVGSVGElement sheet =
			(SVGSVGElement)document.getElementById(DocumentManager.DOCUMENT_SHEET);
		
		AffineTransform xform;
		try {
			xform	= MatrixTools.toJavaTransform(
				sheet.getScreenCTM().inverse());
		}
		catch (SVGException se) {
			se.printStackTrace();
			return ordered;
		}

		Point2D screenPoint   = new Point2D.Double(x, y);
		Point2D documentPoint = new Point2D.Double();

		xform.transform(screenPoint, documentPoint);

		SVGRect query = sheet.createSVGRect();
		query.setX((float)documentPoint.getX() - 0.25f);
		query.setY((float)documentPoint.getY() - 0.25f);

		query.setWidth(0.5f); // half mm
		query.setHeight(0.5f);

		NodeList result = sheet.getIntersectionList(query, null);

		int N = result.getLength();

		for (int i = 0; i < N; ++i) {
			AbstractElement obj = (AbstractElement)result.item(i);
			String last = null;
			AbstractElement lastElement = null;
			do {
				AbstractElement parent = (AbstractElement)obj.getParentNode();
				if (parent == null)
					break;
				String id = parent.getAttributeNS(null, "id");
				if (id != null && id.startsWith(DocumentManager.OBJECT_ID)) {
					last = id;
					lastElement = parent;
				}
				obj = parent;
			}
			while (obj != null && obj != sheet);

			if (last != null) { 
				if (directlyInSheet 
				&& (lastElement == null || lastElement.getParentNode() != sheet))
					continue;

				OnScreenBox box = new OnScreenBox(last);

				if (!ordered.contains(box))
					ordered.add(box);
			}
		}

		return ordered;
	}

	private static final BasicStroke RUBBER =
		new BasicStroke(
			3f,
			BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER,
      10f,
      new float [] { 5 },
      0f);


	public Rectangle damagedRegion() {
		if (!inUse || selected == null)
			return null;

		int BEFORE = selected.size();

		Rectangle damaged = null;

		SVGDocument document = documentManager.getSVGDocument();

		for (int i = 0; i < selected.size();) {
			OnScreenBox box = (OnScreenBox)selected.get(i);

			SVGGraphicsElement element =
				(SVGGraphicsElement)document.getElementById(box.getID());

			if (element == null) { // not available any longer
				selected.remove(i);
				continue;
			}

			++i;

			SVGRect bbox = element.getBBox();
			SVGMatrix matrix = element.getScreenCTM();

			AffineTransform xform = MatrixTools.toJavaTransform(matrix);

			box.setTransformTime(renderTime);
			box.bbox2shape(bbox, xform);
			damaged = LayoutCanvas.enlarge(damaged, box.getShape().getBounds());
		}

		int NOW = selected.size();

		// if only one is selected draw decoration
		if (NOW == 1) {
			OnScreenBox box = (OnScreenBox)selected.get(0);
			damaged = LayoutCanvas.enlarge(damaged, box.buildDecoration(decoration));
		}

		// some one has removed selected objects from DOM
		// inform listeners that selection is no longer valid
		if (BEFORE != NOW) {
			if (NOW == 0) resetSelection();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireSelectionChanged();
				}
			});
		}

		return damaged;
	}
  
	public void paint(Graphics g) {
		if (!inUse || selected == null)
			return;

		Graphics2D g2d = (Graphics2D)g;

		Object oldAntialias =
			g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

		g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setPaint(Color.red);
		g2d.setStroke(RUBBER);

		SVGDocument document = documentManager.getSVGDocument();

		int BEFORE = selected.size();

		for (int i = 0; i < selected.size();) {

			OnScreenBox box = (OnScreenBox)selected.get(i);

			SVGGraphicsElement element =
				(SVGGraphicsElement)document.getElementById(box.getID());

			if (element == null) { // not available any longer
				selected.remove(i);
				continue;
			}

			++i;

			if (box.getTransformTime() != renderTime) {
				box.setTransformTime(renderTime);
				SVGMatrix matrix = element.getScreenCTM();
				AffineTransform xform = MatrixTools.toJavaTransform(matrix);
				SVGRect bbox = element.getBBox();
				box.bbox2shape(bbox, xform);
			}
			Shape shape = box.getShape();
			if (shape != null)
				g2d.draw(shape);
		}

		int NOW = selected.size();

		// if only one is selected draw decoration
		if (NOW == 1) {
			OnScreenBox box = (OnScreenBox)selected.get(0);
			if (!box.hasDecoration())
				box.buildDecoration(decoration);
			box.drawDecoration(g2d);
		}

		++renderTime;

		g2d.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			oldAntialias);

		// some one has removed selected objects from DOM
		// inform listeners that selection is no longer valid
		if (BEFORE != NOW) {
			if (NOW == 0) resetSelection();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireSelectionChanged();
				}
			});
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (transformOperation != null) {
			int x = e.getX();
			int y = e.getY();

			int dx = x - startX;
			int dy = y - startY;
			startX = x;
			startY = y;

			transformOperation.transform(
				getSelectedIDs(),
				documentManager,
				new Point2D.Double(dx, dy),
				new Point2D.Double(x, y));
		}
		else
			finished = true;
	}

	public void mouseEntered(MouseEvent e) {
		finished = true;
	}

	public void mouseExited(MouseEvent e) {
		transformOperation = null;
		finished = true;
	}

	public void mouseMoved(MouseEvent e) {
		finished = true;
	}

	public void mousePressed(MouseEvent evt) {

		Integer inside = OnScreenBox.OUTSIDE;

		int x = evt.getX();
		int y = evt.getY();

		OnScreenBox box = null;

		for (int i = numSelections()-1; i >= 0; --i) {
			box = (OnScreenBox)selected.get(i);
			if ((inside = box.inside(x, y)) != OnScreenBox.OUTSIDE)
				break;
		}

		if (inside != OnScreenBox.OUTSIDE) {

			transformOperation = inside == OnScreenBox.INSIDE
				? TRANSLATE
				: getTransformOperation(box, inside, evt);

			startX = evt.getX();
			startY = evt.getY();
		}
		else
			finished = true;
	}

	// map decoration -> operation
	protected TransformOperation getTransformOperation(
		OnScreenBox box,
		Integer     point,
		MouseEvent  evt
	) {
		int thisSide;
		int otherSide;
		int N;
		Point2D p1, p2;
		LineProjector proj;
		switch (decoration) {
			case SCALE_DECORATION:
				N = box.numPoints();
				thisSide  = point.intValue();
				otherSide = (thisSide + (N >> 1)) % N;
				p1 = box.getPoint(otherSide);
				p2 = box.getPoint(thisSide);
				proj = new LineProjector(p1, p2);
				return isShiftDown(evt.getModifiers())
					? new Scale(proj)
					: new NoneUniformScale(proj);

			case ROTATE_DECORATION:
				thisSide  = point.intValue();
				if ((thisSide & 1) == 1) {
					N = box.numPoints();
					otherSide = (thisSide + (N>>1)) % N;

					p1 = box.getPoint(otherSide);
					p2 = box.getPoint((otherSide+1) % N);

					proj = new LineProjector(p1, p2);
					return new Shear(proj, box.getPoint(thisSide));
				}
				return ROTATE;

			default:
				return ROTATE;
		}
	}

	// cycle thru decorations 
	protected void advanceDecoration() {
		if (++decoration > MAX_DECORATION)
			decoration = MIN_DECORATION;
	}

	public void mouseReleased(MouseEvent e) {
		mouseExited(e);
	}

	public int numSelections() {
		return selected == null ? 0 : selected.size();
	}

	public boolean hasSelection() {
		return selected != null && !selected.isEmpty();
	}

	public String [] getSelectedIDs() {
		if (!hasSelection())
			return null;

		String [] ids = new String[selected.size()];

		for (int i = 0; i < ids.length; ++i)
			ids[i] = ((OnScreenBox)selected.get(i)).id;

		return ids;
	}

	protected void resetSelection() {
		selected = null;
		decoration = SCALE_DECORATION;
	}

	public void clearSelection() {
		if (selected != null) {
			resetSelection();
			fireSelectionChanged();
		}
	}
}
// end of file
