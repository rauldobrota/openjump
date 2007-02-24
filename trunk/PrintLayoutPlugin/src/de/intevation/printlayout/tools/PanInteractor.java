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

/**
 * A simple pan tool to move the viewport of the 
 * SVG canvas around by mouse drag. The only difference
 * between this class and its base class is that
 * the base class needs a pressed shift key to get
 * activated.
 * <br> 
 * This class starts dragging after an unmodified 
 * pressing of the first mouse button.
 */

public class PanInteractor
extends      AbstractPanInteractor
implements   Tool
{
	/**
	 * the identifier of this tool "pan-tool"
	 */
	public static final String IDENTIFIER = "pan-tool";

	/**
	 * is this tool in use?
	 */
	protected boolean inUse;

	public PanInteractor() {
	}

	public String getToolIdentifier() {
		return IDENTIFIER;
	}

	/**
	 * activates/deactivates the tool.
	 * @param inUse true turns in on, false turns it off.
	 */
	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	/**
	 * Get activated if tool in use and left mouse button is down.
	 * @param ie the processed input event
	 */
	public boolean startInteraction(InputEvent ie) {
		return inUse
			&& ie.getID() == MouseEvent.MOUSE_PRESSED 
			&& (ie.getModifiers() & InputEvent.BUTTON1_MASK) != 0;
	}
}
// end of file
