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

import javax.swing.RepaintManager;

import java.util.ArrayList;

public class LayoutCanvas
extends      JSVGCanvas
{
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

	public void damageRegions(ArrayList regions) {
		RepaintManager manager = RepaintManager.currentManager(this);
		for (int i = regions.size()-1; i >= 0; --i) {
			Rectangle rect = (Rectangle)regions.get(i);
			if (!rect.isEmpty()) {
				manager.addDirtyRegion(
					this, 
					rect.x, rect.y,
					rect.width, rect.height);
			}
		}
	}
}
// end of file
