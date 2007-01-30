/*
 * PanInteractor.java
 * ------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

import org.apache.batik.swing.gvt.AbstractPanInteractor;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class PanInteractor
extends      AbstractPanInteractor
implements   Tool
{
	public static final String IDENTIFIER = "pan-tool";

	protected boolean inUse;

	public PanInteractor() {
	}

	public String getToolIdentifier() {
		return IDENTIFIER;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public boolean startInteraction(InputEvent ie) {
		return inUse
			&& ie.getID() == MouseEvent.MOUSE_PRESSED 
			&& (ie.getModifiers() & InputEvent.BUTTON1_MASK) != 0;
	}
}
// end of file
