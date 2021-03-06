/*
 * RulerOverlay.java
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


/**
 * Instances of this class paints a pair of rulers
 * on top of an JSVGCanvas.<br>
 * Each mm a tick is drawn.<br>
 * Each 5 mm a longer tick is drawn.<br>
 * Each 10 mm a even longer tick is drawn.<br>
 * The 10 mm ticks are labeled with text of there
 * value in cm.<br>
 * It is checked if a label to draw would cause a
 * collision. If so it is suppressed. If the distances
 * between the ticks (depending on the current zoom)
 * are to small, the 1 mm ticks are suppressed.
 * If the 5 mm ticks a standing to close they are
 * suppressed also. The 1 cm ticks are alsways drawn.
 * If the viewport is rotated the labels are given
 * as a pair of values in the 2D plane.
 */
public class RulerOverlay
implements   Overlay
{
	/**
	 * the font used to draw the labels
	 */
	public static final Font FONT =
		new Font("Monospaced", Font.BOLD, 11);

	/**
	 * x start coordinate in pixels from where the rulers
	 * are drawn on the canvas.
	 */
	public static final int X_START = 35;
	/**
	 * y start coordinate in pixels from where the rulers
	 * are drawn on the canvas.
	 */
	public static final int Y_START = 20;

	/**
	 * for numerical stability
	 */
	private static final double FORMAT_EPS = 1e-4d;
	/**
	 * for numerical stability
	 */
	private static final double EPS        = 1d;
	/**
	 * for numerical stability
	 */
	private static final double MAX        = 10000d;

	/**
	 * the stroke to draw the ruler lines with.
	 */
	private static final BasicStroke STROKE =
		new BasicStroke(1f);

	/**
	 * should the ruler been drawn?
	 */
	protected boolean inUse;

	/**
	 * reference to the DocumentManager.
	 * used to access the canvas.
	 */
	protected DocumentManager documentManager;

	/**
	 * format for the label text
	 */
	protected NumberFormat format;

	/**
	 * inner class to help to differ between the directions
	 * of the ruler lines and draw the labels accordingly.
	 * The base class formats '(x, y)'.
	 */
	private class Formatter {
		public String toString(Point2D p) {
			StringBuffer sb = new StringBuffer("(");
			sb.append(format.format(p.getX() * 0.1d))
				.append("; ")
				.append(format.format(p.getY() * 0.1d))
				.append(')');
			return sb.toString();
		}
	}

	/**
	 * this class formats 'x'.
	 */
	private final class XFormatter extends Formatter {
		public String toString(Point2D p) {
			return format.format(p.getX() * 0.1d);
		}
	}

	/**
	 * this class formats 'y'.
	 */
	private final class YFormatter extends Formatter {
		public String toString(Point2D p) {
			return format.format(p.getY() * 0.1d);
		}
	}

	/**
	 * inner class to ease solution of linear equations.
	 */
	private static final class Linear
	{
		private double m;
		private double b;

		public Linear(
			double x1, double y1,
			double x2, double y2
		) {
			m = (y1 - y2)/(x1 - x2);
			b = y1 - m*x1;
		}

		public double solve(double x) {
			return m*x + b;
		}
	} // class Linear

	/**
	 * Creates a Ruler. Use the RulerOverlay(DocumentManager)
	 * constructor instead for easier wiring.
	 */
	public RulerOverlay() {
		format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(2);
	}

	/**
	 * Creates a Ruler and connects itself to a DocumentManager.
	 * @param documentManager the DocumentManager to connect with
	 */
	public RulerOverlay(DocumentManager documentManager) {
		this();
		this.documentManager = documentManager;
	}

	/**
	 * activated/deactive the drawing of the rulers.
	 * @param inUse true if the rulers should be drawn else false.
	 */
	public void setInUse(boolean inUse) {
		this.inUse = inUse;
		documentManager.getCanvas().repaint();
	}

	/**
	 * paint method to fullfill import org.apache.batik.swing.gvt.Overlay
	 * interface. This actually draws the rulers.
	 * @param g the graphics context of the canvas.
	 */
	public void paint(Graphics g) {
		if (!inUse || documentManager == null)
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

		Rectangle bounds = documentManager.getCanvas().getBounds();

		Point2D.Double p0 = new Point2D.Double(
			bounds.x + X_START, 
			bounds.y + Y_START);

		Point2D.Double p0i = new Point2D.Double();
		invCTM.transform(p0, p0i);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setPaint(Color.black);
		g2d.setStroke(STROKE);

		FontRenderContext frc = g2d.getFontRenderContext();

		// to avoid overlaping text
		ArrayList bboxes = new ArrayList(100);

		Point2D.Double p1i = new Point2D.Double();

		// handle x
    if (bounds.width > X_START) {

			int xEnd = bounds.x + bounds.width - 5;

			Point2D.Double p1 = new Point2D.Double(xEnd, Y_START);

			Line2D line = new Line2D.Double(p0, p1);

			g2d.draw(line);

			invCTM.transform(p1, p1i);

			double xLength = Math.abs(p0i.x - p1i.x);

			if (xLength > EPS && xLength < MAX) {
				Formatter formatter = Math.abs(p0i.y - p1i.y) < FORMAT_EPS
					? new XFormatter()
					: new Formatter();

				// count direction
				double dx = p1i.x > p0i.x ? +1 : -1;

				double ratio = xLength/bounds.width;

				boolean draw1mm = ratio < 0.7d;
				boolean draw5mm = ratio < 0.85d;

				double nextX = dx < 0d
					? Math.floor(p0i.x)
					: Math.ceil (p0i.x);

				Linear fx = new Linear(p0i.x, p0i.y, p1i.x, p1i.y);

				double nextY = fx.solve(nextX);

				Point2D.Double nxi = new Point2D.Double(nextX, nextY);
				Point2D.Double nx  = new Point2D.Double();

				CTM.transform(nxi, nx);

				int markX = (int)Math.round(nx.x);

				while (markX <= xEnd) {
					int nxir = (int)Math.round(nextX);
					int length = -1;
					if ((nxir % 10) == 0) {
						length = 10;

						TextLayout layout = new TextLayout(
							formatter.toString(nxi),
							FONT,
							frc);

						Rectangle2D bbox = layout.getBounds();

						float px = (float)(markX-bbox.getWidth()*0.5d);
						float py = Y_START - (float)bbox.getHeight();

						Rectangle2D posBBox =
							new Rectangle2D.Double(
								px-4, py-4,
								bbox.getWidth()+8, bbox.getHeight()+8);

						boolean collide = false;
						for (int i = bboxes.size()-1; i >= 0; --i)
							if (posBBox.intersects((Rectangle2D)bboxes.get(i))) {
								collide = true;
								break;
							}
						if (!collide) {
							layout.draw(g2d, px, py);
							bboxes.add(posBBox);
						}
					}
					else if ((nxir % 5) == 0) {
						if (draw5mm) length = 7;
					}
					else {
						if (draw1mm) length = 3;
					}

					if (length > 0)
						g2d.drawLine(markX, Y_START, markX, Y_START-length);

					nxi.x = nextX += dx;
					nxi.y = fx.solve(nextX);
					CTM.transform(nxi, nx);
					markX = (int)Math.round(nx.x);
				} // for all x ticks
			}
		} // handle x axis

		bboxes.clear();

		// handle y
    if (bounds.height > Y_START) {

			int yEnd = bounds.y + bounds.height - 5;

			Point2D.Double p1 = new Point2D.Double(X_START, yEnd);

			Line2D line = new Line2D.Double(p0, p1);

			g2d.draw(line);

			invCTM.transform(p1, p1i);

			double yLength = Math.abs(p0i.y - p1i.y);

			if (yLength > EPS && yLength < MAX) {
				Formatter formatter = Math.abs(p0i.x - p1i.x) < FORMAT_EPS
					? new YFormatter()
					: new Formatter();

				// count direction
				double dy = p1i.y > p0i.y ? +1 : -1;

				double ratio = yLength/bounds.height;

				boolean draw1mm = ratio < 0.7d;
				boolean draw5mm = ratio < 0.85d;

				double nextY = dy < 0d
					? Math.floor(p0i.y)
					: Math.ceil (p0i.y);

				Linear fx = new Linear(p0i.y, p0i.x, p1i.y, p1i.x);

				double nextX = fx.solve(nextY);

				Point2D.Double nyi = new Point2D.Double(nextX, nextY);
				Point2D.Double ny  = new Point2D.Double();

				CTM.transform(nyi, ny);

				int markY = (int)Math.round(ny.y);

				while (markY <= yEnd) {
					int nyir = (int)Math.round(nextY);
					int length = -1;
					if ((nyir % 10) == 0) {
						length = 10;

						TextLayout layout = new TextLayout(
							formatter.toString(nyi),
							FONT,
							frc);

						Rectangle2D bbox = layout.getBounds();

						float py = (float)(markY+bbox.getHeight()*0.5d);
						float px = X_START - 8 - (float)bbox.getWidth();

						Rectangle2D posBBox =
							new Rectangle2D.Double(
								px-4, py-4,
								bbox.getWidth()+8, bbox.getHeight()+8);

						boolean collide = false;
						for (int i = bboxes.size()-1; i >= 0; --i)
							if (posBBox.intersects((Rectangle2D)bboxes.get(i))) {
								collide = true;
								break;
							}
						if (!collide) {
							layout.draw(g2d, px, py);
							bboxes.add(posBBox);
						}
					}
					else if ((nyir % 5) == 0) {
						if (draw5mm) length = 7;
					}
					else {
						if (draw1mm) length = 3;
					}

					if (length > 0)
						g2d.drawLine(X_START-length, markY, X_START, markY);

					nyi.y = nextY += dy;
					nyi.x = fx.solve(nextY);
					CTM.transform(nyi, ny);
					markY = (int)Math.round(ny.y);
				} // for all y ticks
			}
		} // handle x axis
	} // paint()
}
// end of file
