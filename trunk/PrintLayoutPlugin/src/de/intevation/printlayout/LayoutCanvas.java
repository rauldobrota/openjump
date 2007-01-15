package de.intevation.printlayout;

import org.apache.batik.swing.svg.SVGUserAgent;

import org.apache.batik.swing.JSVGCanvas;

import org.apache.batik.bridge.UserAgent;

import org.w3c.dom.svg.SVGDocument;

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
}
// end of file
