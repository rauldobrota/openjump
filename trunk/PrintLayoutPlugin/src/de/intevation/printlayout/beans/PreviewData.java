/*
 * PreviewData.java
 * ----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.beans;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Bean that stores map data extracted from OpenJump
 * for a given map (e.g. the initial scale of the map).
 */
public class PreviewData
extends      MapData
{
	protected double minX;
	protected double minY;

	protected double maxX;
	protected double maxY;

	public PreviewData() {
	}

	public PreviewData(double initialScale, Envelope envelope) {
		super(initialScale);
		minX = envelope.getMinX();
		minY = envelope.getMinY();
		maxX = envelope.getMaxX();
		maxY = envelope.getMaxY();
	}

	public Envelope asEnvelope() {
		return new Envelope(
			new Coordinate(minX, minY),
			new Coordinate(maxX, maxY));
	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}
}
// end of file
