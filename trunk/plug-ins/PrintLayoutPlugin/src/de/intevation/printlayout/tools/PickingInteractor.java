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

/**
 * Instances of this class are used as tools for
 * selecting and manipulating objects on screen my mouse.
 * Mouse events are traced and several operations are
 * executed depending on the interal state of the PickingInteractor.
 * <br>
 * The first purpose is to select objects. To do so the
 * user has to click on the object he wants to select. If
 * control is pressed the selection can be expaned to more
 * than object.
 * <br>
 * If the user presses the left mouse button inside a selected
 * object and drags the mouse pointer around the selected objects
 * are translated on the sheet accordingly. 
 * <br>
 * If only one object is selected the initial manipulation tool
 * is to scale this object. A set of arrows is drawn around
 * the object to indicated the scale direction.
 * <br>
 * By clicking once more on the selected object the manipulation
 * tool switches to rotation and shearing. Arrows are drawn to
 * indicated the directions of operation in this mode too.
 * <br>
 * Another click brings the manipulation mode back to scaling.
 * <br>
 * The interaction between the PickingInteractor and the rest
 * of the program is done by implementing the PickingListener
 * interface. An implementor can register to a PickingInteractor
 * and will be informed if the selection set has changed.
 * <br>
 * The selection itself is represented by an array of id strings
 * also used in the SVG DOM tree.
 */
public class PickingInteractor
extends      InteractorAdapter
implements   Overlay, Tool, LayoutCanvas.DamagedRegion
{
	/**
	 * the unique name of the tool. Actually "picking-tool"
	 */
	public static final String IDENTIFIER = "picking-tool";


	/**
	 * This event is send when the selection set the PickingInteractor
	 * changed.
	 */
	public static class PickingEvent
	extends             EventObject
	{
		/**
		 * Creates a PickingEvent.
		 * @param source the PickingInteractor sending this event
		 */
		public PickingEvent(Object source) {
			super(source);
		}
	} // class PickingEvent

	/**
	 * Implement this to get informed if the selection set
	 * of the PickingInteractor has changed. The source 
	 * transmitted by the event this the corresponding
	 * PickingInteractor.
	 */
	public interface PickingListener
	extends          EventListener
	{
		/**
		 * called if the selection of the PickingInteractor changed.
		 * @param evt event evt.getSource() gives the sending PickingInteractor.
		 */
		void selectionChanged(PickingEvent evt);

	} // interface PickingListener

	/**
	 * The manipulations of the selected objects are encapsulated
	 * by this interface. An implementor must handle the transform
	 * operation. It recieves the set of selected objects by there
	 * DOM tree ids, the DocumentManager which manages the DOM tree,
	 * the current mouse point on screen and the delta which leads
	 * to this point.
	 */
	public interface TransformOperation {
		/**
		 * the method to actually do the transformation.
		 * @param ids             the DOM tree ids
		 * @param documentManager the DocumentManager
		 * @param delta           the delta that leads to current point
		 * @param orginal         the current point
		 */
		void transform(
			String []       ids, 
			DocumentManager documentManager, 
			Point2D         delta,
			Point2D         orginal);
	} // interface TransformOperation

	/**
	 * This instance moves objects around on a sheet.
	 */
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

	/**
	 * Instances of this class are able to scale objects
	 * uniformly. The scaling process is guided by a
	 * LineProjector which projects mouse motion to the
	 * axis of the scaling.
	 */
	public static class Scale
	implements          TransformOperation
	{
		/**
		 * the LineProjector that ensure stability of the
		 * scaling direction
		 */
		protected LineProjector proj;

		/**
		 * Creates a Scale object with a guiding LineProjector.
		 * @param proj the LineProjector to be used.
		 */
		public Scale(LineProjector proj) {
			this.proj = proj;
		}

		/**
		 * Nearly identical to the transform() method but
		 * the mouse coordinates and and deltas are projected
		 * back to the axis of scaling. This method implements
		 * uniform scaling.
		 * Subclasses can override this method to benefit of
		 * the LineProjector.
		 * @param ids             the DOM tree ids of the selected objects
		 * @param documentManager the DocumentManager managing the DOM tree
		 * @param delta           the reprojected delta
		 * @param orginal         the reprojected current point
		 */
		protected void projectedTransform(
			String []       ids,
			DocumentManager documentManager,
			Point2D         delta,
			Point2D         orginal
		) {
			documentManager.scaleFixedIDs(
				ids, delta, orginal, proj.getOrigin());
		}

		/**
		 * Implements the transform() of the TransformOperation interface.
		 * It reproject the mouse coordinates and deltas back to the axis
		 * of scaling and calls projectedTransform() with the modified
		 * parameters.
		 * @param ids             the DOM tree ids of the selected objects
		 * @param documentManager the DocumentManager managing the DOM tree
		 * @param delta           the delta to current point
		 * @param orginal         the current point
		 */
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

	/**
	 * This subclass of scale implements none uniform scaling.
	 */
	public static class NoneUniformScale
	extends             Scale
	{
		/** 
		 * Create a NoneUniformScale with a given LineProjector.
		 * @param proj the LineProjector to be used
		 */
		public NoneUniformScale(LineProjector proj) {
			super(proj);
		}

		/**
		 * overwrites projectedTransform() from base class.
		 * The only difference is the none uniform scaling
		 * implemented by this method.
		 * @param ids             the DOM tree ids of the selected objects
		 * @param documentManager the DocumentManager managing the DOM tree
		 * @param delta           the reprojected delta
		 * @param orginal         the reprojected current point
		 */
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

	/**
	 * This subclass  of scale implements Shear along the
	 * line of the LineProjector.
	 */
	public static class Shear
	extends             Scale
	{
		/**
		 * the reference point to calculate the shearing angles from.
		 */
		protected Point2D ref;

		/**
		 * Creates a Shear with a given LineProjector and an reference
		 * point. The reference point is used to calculate the current
		 * shear angles. The angle between the origin of the LineProjector
		 * and this point is used as the base angle.
		 * @param proj the LineProjector
		 * @param ref  the reference point
		 */
		public Shear(LineProjector proj, Point2D ref) {
			super(proj);
			this.ref   = ref;
		}

		/**
		 * Implements a none uniform shearing along the line of
		 * the LineProjector.
		 * @param ids             the DOM tree ids of the selected objects
		 * @param documentManager the DocumentManager managing the DOM tree
		 * @param delta           the reprojected delta
		 * @param orginal         the reprojected current point
		 */
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

	/**
	 * This instance implements rotation of the selected objects
	 * around there center.
	 */
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

	/**
	 * constant for showing no decoration
	 */
	public static final int NO_DECORATION     = 0;
	/**
	 * constant for showing the scale decoration
	 */
	public static final int SCALE_DECORATION  = 1;
	/**
	 * constant for showing the rotate and shear decoration
	 */
	public static final int ROTATE_DECORATION = 2;

	/**
	 * minimum decoration value. Used for cycling through
	 * the decorations.
	 */
	public static final int MIN_DECORATION    = 1;
	/**
	 * maximum decoration value. Used for cycling through
	 * the decorations.
	 */
	public static final int MAX_DECORATION    = 2;

	/**
	 * Is the tool in use?
	 */
	protected boolean inUse;
	/**
	 * Is the current mouse operation finished?
	 */
	protected boolean finished;
	
	/**
	 * reference to the DocumentManager.
	 * Used to access the SVG DOM and the LayoutCanvas.
	 */
	protected DocumentManager documentManager;

	/**
	 * the list of the currently selected objects.
	 * Objects in this list are instances of OnScreenBox.
	 */
	protected ArrayList       selected;

	/**
	 * the list of PickingListeners
	 */
	protected ArrayList       listeners;

	/**
	 * start x coordinate of current mouse operation.
	 * Used for applay the TransformOperations.
	 */
	protected int startX;

	/**
	 * start y coordinate of current mouse operation.
	 * Used for applay the TransformOperations.
	 */
	protected int startY;

	/**
	 * the overlay renderer implements the DamagedRegion
	 * interface. This interface allows the calculation
	 * of the damange caused by the the overlay before
	 * the rendering is done. To double calculations of
	 * the overlay geometries each geometry (OnScreenBox
	 * actually) is assign a rendering time. When the drawing
	 * is done the renderer looks if the rendering time
	 * is equal to the damage calculation time. If this is
	 * the case the recalculation is ommited. After the
	 * rendering is complete this counter is increased.
	 */
	protected int renderTime = -1;

	/**
	 * the current TransformOperation (scaling, rotation, etc.)
	 */
	protected TransformOperation transformOperation;

	/**
	 * the type of the decoration to draw if one object
	 * is selected.
	 */
	protected int decoration = SCALE_DECORATION;

	/**
	 * Creates a PickingInteractor(). Use the 
	 * PickingInteractor(DocumentManager) constructor for
	 * easier wiring.
	 */
	public PickingInteractor() {
	}

	/**
	 * Creates a PickingInteractor.
	 * @param documentManager the DocumentManager to wire with
	 */
	public PickingInteractor(DocumentManager documentManager) {
		setDocumentManager(documentManager);
	}
	
	/**
	 * Sets the current DocumentManager.
	 * @param documentManager the DocumentManager to be used
	 */
	public void setDocumentManager(DocumentManager documentManager) {
		this.documentManager = documentManager;
	}

	/**
	 * Gets the current DocumentManager.
	 * @return the DocumentManager currently in use
	 */
	public DocumentManager getDocumentManager() {
		return documentManager;
	}

	/**
	 * Adds a PickingListener to the list of PickingListeners.
	 * Implementors get informed if the selection has changed.
	 * @param listener the listener to add
	 */
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

	/**
	 * Removes a PickingListener from the list of PickingListeners.
	 * @param listener the listener to be removed
	 */
	public void removePickingListener(PickingListener listener) {
		if (listener  != null
		&&  listeners != null
		&&  listeners.remove(listener) 
		&&  listeners.isEmpty());
			listeners = null;
	}

	/**
	 * Implements the getToolIdentifier() of the Tool interface.
	 * @return the unique identifier of this tool
	 */
	public String getToolIdentifier() {
		return IDENTIFIER;
	}

	/**
	 * Implements the setInUse() method of the Tool interface.
	 * @param flag true activates this tool, false deactives it.
	 */
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

	/**
	 * Tells if this tool is in use.
	 * @return true if tool is in use else false
	 */
	public boolean getInUse() {
		return inUse;
	}

	/**
	 * Checks if the current InputEvent should start an operation
	 * with this tool.
	 * @param ie the InputEvent to evaluate.
	 */
	public boolean startInteraction(InputEvent ie) {
		if (!inUse) return false;

		boolean start = ie instanceof MouseEvent
			&& (ie.getModifiers() & InputEvent.BUTTON1_MASK)
				== InputEvent.BUTTON1_MASK;

		if (start)
			finished = false;

		return start;
	}
	
	/**
	 * Is the current operation over?
	 * @return true if operation is over else false
	 */
	public boolean endInteraction() {
		return finished;
	}

	/**
	 * Marco to test if shift button is pressed.
	 * @param modifiers the modifiers of an InputEvent
	 * @return true if shift is down else false
	 */
	protected static final boolean isShiftDown(int modifiers) {
		return (modifiers & InputEvent.SHIFT_MASK) != 0;
	}

	/**
	 * Marco to test if control button is pressed.
	 * @param modifiers the modifiers of an InputEvent
	 * @return true if control is down else false
	 */
	protected static final boolean isCtrlDown(int modifiers) {
		return (modifiers & InputEvent.CTRL_MASK) != 0;
	}

	/**
	 * Handles the mouse clicked event. Mainly used to change
	 * the selection state.
	 * @param me the MouseEvent to evaluate
	 */
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

	/**
	 * Fires a PickingEvent (selectionChanged) to the listeners.
	 */
	protected void fireSelectionChanged() {
		if (listeners != null) {
			PickingEvent evt = new PickingEvent(this);
			for (int i = listeners.size()-1; i >= 0; --i)
				((PickingListener)listeners.get(i)).selectionChanged(evt);
		}
	}

	/**
	 * Does a spatial query for all objects around (x, y) in screen coordinates.
	 * Calls query(x, y, true).
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	protected ArrayList query(int x, int y) {
		return query(x, y, true);
	}

	/**
	 * Does a spatial query for all objects around (x, y) in screen coordinates.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param directlyInSheet should only objects be reported which are
	 *                        directly in the paper sheet?
	 */
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

	/**
	 * Stroke to draw the rubber band of the selected objects.
	 */
	private static final BasicStroke RUBBER =
		new BasicStroke(
			3f,
			BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER,
      10f,
      new float [] { 5 },
      0f);


	/**
	 * Implements the damagedRegion() method of the DamagedRegion
	 * interface. The bounding box of all selected objects and
	 * there decorations are caluculated and returned.
	 * @return the over all bounding box.
	 */
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

			SVGRect bbox = element.getBBox();
			SVGMatrix matrix = element.getScreenCTM();

			AffineTransform xform = MatrixTools.toJavaTransform(matrix);

			box.setTransformTime(renderTime);
			Rectangle r = box.bbox2shape(bbox, xform);
			if (r != null) {
				damaged = LayoutCanvas.enlarge(damaged, r);
				++i;
			}
			else { // some trouble with the bounding box
				selected.remove(i);
			}
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
  
	/**
	 * Implements the paint() method of the Overlay interface.
	 * All bounding boxes of the selected objects are drawn.
	 * If there is only one the current transform decoration is
	 * drawn also.
	 * @param g the graphics context to draw into.
	 */
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

			try {
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
				++i;
			}
			catch (Exception e) {
				// this one causes trouble -> remove it
				selected.remove(i);
			}
		} // for all selected

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


	/**
	 * Processes the mouse drag event. If a TransformOperation
	 * is active the coordinates and deltas are calculated and
	 * the TransformOperation.transform() is called.
	 * @param e the mouse event to evaluate
	 */
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

	/**
	 * Just finishes the current operation.
	 * @param e the mouse event to evaluate
	 */
	public void mouseEntered(MouseEvent e) {
		finished = true;
	}

	/**
	 * Just finishes the current operation.
	 * @param e the mouse event to evaluate
	 */
	public void mouseExited(MouseEvent e) {
		transformOperation = null;
		finished = true;
	}

	/**
	 * Just finishes the current operation.
	 * @param e the mouse event to evaluate
	 */
	public void mouseMoved(MouseEvent e) {
		finished = true;
	}

	/**
	 * Checks if the mouse is pressed inside one
	 * of the selected objects. If this is the
	 * case the TRANSLATE operation will be
	 * applied to this object. If a decoration arrow
	 * is hit the current will be applied instead.
	 * @param evt the mouse event to evaluate
	 */
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

	/**
	 * Depending on the decoration style cycle the 
	 * accordant TransformOperation will be triggered.
	 * Called by mousePressed().
	 * @param box the object that was hit on screen
	 * @param point the number of the decoration (which arrow actually)
	 * @param evt   the mouse event to evaluate
	 * @return the TransformOperation that should be applied to
	 * the selected object
	 */
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

	/**
	 * Cycles through the decoration styles from MIN_DECORATION
	 * to MAX_DECORATION.
	 */
	protected void advanceDecoration() {
		if (++decoration > MAX_DECORATION)
			decoration = MIN_DECORATION;
	}

	/**
	 * Just finish the current operation.
	 * @param e the mouse event to evaluate
	 */
	public void mouseReleased(MouseEvent e) {
		mouseExited(e);
	}

	/**
	 * How many objects are currently selected?
	 * @return number of selected objects.
	 */
	public int numSelections() {
		return selected == null ? 0 : selected.size();
	}

	/**
	 * Are there any selected objects?
	 * @return true if they are selected objects, else false.
	 */
	public boolean hasSelection() {
		return selected != null && !selected.isEmpty();
	}

	/**
	 * Get the DOM tree ids of the selected objects.
	 * @return an array of strings the selected ids.
	 *         null is returned if there are no selected objects.
	 */
	public String [] getSelectedIDs() {
		if (!hasSelection())
			return null;

		String [] ids = new String[selected.size()];

		for (int i = 0; i < ids.length; ++i)
			ids[i] = ((OnScreenBox)selected.get(i)).id;

		return ids;
	}

	/**
	 * Resets the selection and restart the decoration style cycle.
	 */
	protected void resetSelection() {
		selected = null;
		decoration = MIN_DECORATION;
	}

	/**
	 * Clears the current selection and fires a selectionChanged()
	 * event to the PickingListeners.
	 */
	public void clearSelection() {
		if (selected != null) {
			resetSelection();
			fireSelectionChanged();
		}
	}
}
// end of file
