/*
 * DrawingAttributes.java
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

import java.awt.Stroke;
import java.awt.Paint;

public class DrawingAttributes
{
	protected Paint  strokeColor;

	protected Paint  fillColor;

	protected Stroke stroke;

	public Paint getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(Paint strokeColor) {
		this.strokeColor = strokeColor;
	}

	public Paint getFillColor() {
		return fillColor;
	}

	public void setFillColor(Paint fillColor) {
		this.fillColor = fillColor;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public Stroke getStroke() {
		return stroke;
	}
}
// end of file
