/*
 * MapData.java
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

import java.io.Serializable;

public class MapData
implements   Serializable
{
	protected double initialScale;

	public MapData() {
	}

	public MapData(double initialScale) {
		this.initialScale = initialScale;
	}

	public double getInitialScale() {
		return initialScale;
	}

	public void setInitialScale(double initialScale) {
		this.initialScale = initialScale;
	}
}
// end of file
