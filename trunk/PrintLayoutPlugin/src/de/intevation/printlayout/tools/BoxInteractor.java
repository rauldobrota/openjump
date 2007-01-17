/*
 * BoxInteractor.java
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

import java.awt.Shape;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import org.apache.batik.swing.gvt.InteractorAdapter;
import org.apache.batik.swing.gvt.JGVTComponent;
import org.apache.batik.swing.gvt.Overlay;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGMatrix;


import org.apache.batik.dom.svg.SVGOMSVGElement;

import de.intevation.printlayout.MatrixTools;
import de.intevation.printlayout.DocumentManager;

public class BoxInteractor 
extends      InteractorAdapter 
implements   Overlay, Tool
{
	public static final String IDENTIFIER = "box-tool";

	protected boolean inUse;

	protected boolean finished = true;

	protected DocumentManager documentManager;

	/**
	 * The mouse x start position.
	 */
	protected int xStart;

	/**
	 * The mouse y start position.
	 */
	protected int yStart;

	/**
	 * The mouse x current position.
	 */
	protected int xCurrent;

	/**
	 * The mouse y current position.
	 */
	protected int yCurrent;

	protected Paint  strokeColor;
	protected Stroke stroke;
	protected Paint  fillColor;

	/** state of overlay */
	protected float[] screenPoints;

	protected Factory factory;

	
	public interface Factory {
		DocumentManager.DocumentModifier createBox(
			Rectangle2D     rect,
			AffineTransform xform);

		DrawingAttributes getDrawingAttributes();
	}

	public String getToolIdentifier() {
		return IDENTIFIER;
	}


	public void setFactory(Factory factory) {
		this.factory = factory;
	}

	public Factory getFactory() {
		return factory;
	}
	
	public void setScreenPoints(float x1, float y1, float x2, float y2) {
		float minX = Math.min(x1, x2);
		float maxX = Math.max(x1, x2);
		float minY = Math.min(y1, y2);
		float maxY = Math.max(y1, y2);

		screenPoints = new float[] { minX, minY, maxX, maxY };
	}

	/*
	public void setScreenPoints(float[] screenPoints) {
		if (screenPoints.length == 4)
			this.screenPoints = screenPoints;
	}
	*/
	
	public void clear() {
		screenPoints = null;
	}

	public void setDocumentManager(DocumentManager documentManager) {
		this.documentManager = documentManager;
	}

	public DocumentManager getDocumentManager() {
		return documentManager;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public boolean getInUse() {
		return inUse;
	}
	
	public void paint(Graphics g) {
		if (!inUse || factory == null)
			return;

		DrawingAttributes attributes = factory.getDrawingAttributes();

		Paint   strokeColor = attributes.getStrokeColor();
		Stroke  stroke      = attributes.getStroke();
		Paint   fillColor   = attributes.getFillColor();

		SVGDocument document = documentManager.getSVGDocument();
		if (document != null 
				&& screenPoints != null 
				&& screenPoints.length == 4) {
			Graphics2D g2d = (Graphics2D)g;
							
			Rectangle2D rect = new Rectangle2D.Float(
				screenPoints[0],
				screenPoints[1],
				screenPoints[2] - screenPoints[0],
				screenPoints[3] - screenPoints[1]);
			//System.err.println(rect);

			Shape shape = getXForm(document).createTransformedShape(rect);

			if(fillColor != null) {
				g2d.setPaint(fillColor);
				g2d.fill(shape);
			}

			g2d.setStroke(stroke);
			g2d.setPaint(strokeColor);
			g2d.draw(shape);
		}
	}
	
	protected static AffineTransform getXForm(SVGDocument document) {
		SVGOMSVGElement element 
				= (SVGOMSVGElement)document.getElementById("viewer-layout-sheet-svg");
		SVGMatrix matrix = element.getCTM();
	
		return MatrixTools.toJavaTransform(matrix);
	}

	protected static AffineTransform getScreenXForm(SVGDocument document) {
		SVGOMSVGElement element 
				= (SVGOMSVGElement)document.getElementById("viewer-layout-sheet-svg");
		SVGMatrix matrix = element.getScreenCTM();
	
		return MatrixTools.toJavaTransform(matrix);
	}

	
	public boolean startInteraction(InputEvent ie) {
		int mods = ie.getModifiers();
		return
			inUse &&
			ie.getID() == MouseEvent.MOUSE_PRESSED &&
			(mods & InputEvent.BUTTON1_MASK) != 0; 
	}

	public boolean endInteraction() {
		return finished;
	}

		/**
		 * Invoked when a mouse button has been pressed on a component.
		 */
	public void mousePressed(MouseEvent e) {
		if (!finished) {
			mouseExited(e);
			return;
		}

		finished = false;

		xStart = e.getX();
		yStart = e.getY();
		//System.err.println("xStart=" + xStart + " yStart=" + yStart);
	}
	/**
	 * Invoked when a mouse button has been released on a component.
	 */
	public void mouseReleased(MouseEvent e) {
		if (finished) {
			return;
		}
		finished = true;

		xCurrent = e.getX();
		yCurrent = e.getY();

		clear();
	
		JGVTComponent c = (JGVTComponent)e.getSource();

		consume();

		//System.err.println("Mouse released with");
		//System.err.println("\txStart=" + xStart +" yStart=" + yStart);
		//System.err.println("\txCurrent=" + xCurrent + " yCurrent=" + yCurrent);
		
		c.repaint();
	}

	public void mouseDragged(MouseEvent e) {
		xCurrent = e.getX();
		yCurrent = e.getY();

		setScreenPoints(xStart, yStart, xCurrent, yCurrent);
		
		JGVTComponent c = (JGVTComponent)e.getSource();
		c.repaint();
	}
	/**
	 * Invoked when the mouse exits a component.
	 */
	public void mouseExited(MouseEvent e) {
		finished = true;

		clear();
		
		JGVTComponent c = (JGVTComponent)e.getSource();
		c.repaint();
	}

	protected void consume() {
		if (factory == null)
			return;

		SVGDocument document = documentManager.getSVGDocument();

		float minX = Math.min(xStart, xCurrent);
		float maxX = Math.max(xStart, xCurrent);
		float minY = Math.min(yStart, yCurrent);
		float maxY = Math.max(yStart, yCurrent);

		Rectangle2D rect = new Rectangle2D.Float(
			minX,
			minY,
			maxX - minX,
			maxY - minY);

		AffineTransform xform;

		try {
			xform = getScreenXForm(document).createInverse();
		}
		catch(NoninvertibleTransformException nte) {
			nte.printStackTrace();
			return;
		}

		documentManager.modifyDocumentLater(
			factory.createBox(rect, xform));
	}
}
