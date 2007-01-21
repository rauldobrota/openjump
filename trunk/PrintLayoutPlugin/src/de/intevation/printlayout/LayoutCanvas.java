/*
 * LayoutCanvas.java
 * -----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import org.apache.batik.swing.svg.SVGUserAgent;

import org.apache.batik.swing.JSVGCanvas;

import org.apache.batik.bridge.UserAgent;

import org.w3c.dom.svg.SVGDocument;

import java.awt.Rectangle;

public class LayoutCanvas
extends      JSVGCanvas
{
	protected Rectangle damagedRegion;

	public LayoutCanvas() {
	}

	public LayoutCanvas(
		SVGUserAgent agent,
		boolean      eventsEnabled,
		boolean      selectableText
	) {
		super(agent, eventsEnabled, selectableText);
	}

	public UserAgent getUserAgent() {
		return userAgent;
	}

	public void installDocument(SVGDocument document) {
		installSVGDocument(document);
	}

	public void paintImmediately(Rectangle rect) {
		if (damagedRegion != null)
			rect.add(damagedRegion);
		super.paintImmediately(rect);
	}

	public void repaint(Rectangle rect) {
		if (damagedRegion != null)
			rect.add(damagedRegion);
		super.repaint(rect);
	}

	public void repaint(int x, int y, int width, int height) {
		Rectangle rect = new Rectangle(x, y, width, height);
		if (damagedRegion != null)
			rect.add(damagedRegion);
		super.repaint(rect.x, rect.y, rect.width, rect.height);
	}

	public void damagedRegion(Rectangle region) {

		if (region == null || region.isEmpty())
			damagedRegion = null;
		else {
			// enlarge a bit to avoid rendering artifacts
			region = new Rectangle(
				region.x-2, region.y-2,
				region.width+4, region.height+4);
			damagedRegion = region;
		}
	}
}
// end of file
