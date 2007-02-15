/*
 * ZoomIconLoader.java
 * ----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.resources;

import javax.swing.ImageIcon;

public class ZoomIconLoader {
	public static ImageIcon icon(String filename) {
		return new ImageIcon(ZoomIconLoader.class.getResource(filename));
	}
}
