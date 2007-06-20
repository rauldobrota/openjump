/*
 * Map2SVG.java
 * ------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementing class produce SVG represenations
 * from the GIS.
 */
public interface Map2SVGConverter
extends          DocumentManager.DocumentModifier
{
	/**
	 * Creates a new SVG representation of the GIS panel.
	 * @param document     The DOM factory used to create the SVG.
	 * @param geo2screen   Stores the scaling factor map to screen
	 *                     after the call. null permitted.
	 * @param screen2paper Stores the scaling factor screen to paper
	 *                     after the call. null permitted.
	 * @param paperSize    The assumed size of the paper. null permitted.
	 * @param tolerance    If not null its value is taken as a simplification tolerance
	 *                     in paper millimetres.
	 */
	Element createSVG(
		Document  document, 
		double [] geo2screen,
		double [] screen2paper,
		double [] paperSize,
		Double    tolerance
	);
}
// end of file
