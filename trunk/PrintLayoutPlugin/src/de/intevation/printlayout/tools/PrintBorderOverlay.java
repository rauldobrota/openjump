/*
 * PrintBorderOverlay.java
 * -----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import org.apache.batik.swing.gvt.Overlay;

import java.text.NumberFormat;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Color;
import java.awt.BasicStroke;

import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;

import java.util.ArrayList;

import de.intevation.printlayout.util.MatrixTools;

import de.intevation.printlayout.DocumentManager;

public class PrintBorderOverlay
implements   Overlay
{
	private Double[] border;
	
	/**
	 * the stroke to draw the ruler lines with.
	 */
	private static final float[] dash = {5f, 5f};
	private static final BasicStroke STROKE =
		new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
		1f, dash, 0f );
	
	/**
	 * reference to the DocumentManager
	 * used to access the canvas.
	 */
	protected DocumentManager documentManager;

	public PrintBorderOverlay() {
	}

	public PrintBorderOverlay(DocumentManager documentManager) {
		this();
		this.documentManager = documentManager;
	}

	public void setPrintBorder(Double[] borderInCm) {
		if (borderInCm == null
				|| borderInCm.length != 4
				|| borderInCm[0] == null
				|| borderInCm[0].doubleValue() < 0
				|| borderInCm[1] == null
				|| borderInCm[1].doubleValue() < 0
				|| borderInCm[2] == null
				|| borderInCm[2].doubleValue() < 0
				|| borderInCm[3] == null
				|| borderInCm[3].doubleValue() < 0)
			border = null;
		else
			border = borderInCm;

		documentManager.getCanvas().repaint();
	}
	
	public Double[] getPrintBorder() {
		return border;
	}
	
	/**
	 * paint method to fullfill import org.apache.batik.swing.gvt.Overlay
	 * interface. This actually draws the rulers.
	 * @param g the graphics context of the canvas.
	 */
	public void paint(Graphics g) {
		if (border == null || documentManager == null)
			return;

		SVGDocument document = documentManager.getSVGDocument();

		if (document == null)
			return;

		SVGLocatable sheet =
			(SVGLocatable)document.getElementById(
				DocumentManager.DOCUMENT_SHEET);

		if (sheet == null)
			return;

		SVGMatrix matrix = sheet.getScreenCTM();
		if (matrix == null)
			return;

		AffineTransform CTM, invCTM;
		try {
			CTM = MatrixTools.toJavaTransform(matrix);
			invCTM = CTM.createInverse();
		}
		catch (Exception e) {
			return;
		}
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setPaint(Color.black);
		g2d.setStroke(STROKE);	
		
		double [] size = documentManager.getPaperSize();
		
		double leftBorder = border[0].doubleValue() * 10;
		double bottomBorder = size[1] - border[1].doubleValue() * 10;
		double rightBorder = size[0] - border[2].doubleValue() * 10;
		double topBorder =  border[3].doubleValue() * 10;
	
		Point2D.Double p1 = new Point2D.Double(leftBorder, topBorder);
		Point2D.Double p2 = new Point2D.Double(leftBorder, bottomBorder);
		transformAndDraw(g2d, CTM, p1, p2);

		p1 = new Point2D.Double(leftBorder, bottomBorder);
		p2 = new Point2D.Double(rightBorder , bottomBorder);
		transformAndDraw(g2d, CTM, p1, p2);
	
		p1 = new Point2D.Double(rightBorder, topBorder);
		p2 = new Point2D.Double(rightBorder, bottomBorder);
		transformAndDraw(g2d, CTM, p1, p2);

		p1 = new Point2D.Double(leftBorder, topBorder);
		p2 = new Point2D.Double(rightBorder, topBorder);
		transformAndDraw(g2d, CTM, p1, p2);
	} // paint()

	protected void transformAndDraw(
			Graphics2D g2d, 
			AffineTransform CTM, 
			Point2D p1,
			Point2D p2
	) {
			Point2D.Double p1s = new Point2D.Double();
			Point2D.Double p2s = new Point2D.Double();

			CTM.transform(p1, p1s);
			CTM.transform(p2, p2s);
			
      Line2D line = new Line2D.Double(p1s, p2s);
			
			g2d.draw(line);
	}
}
// end of file
