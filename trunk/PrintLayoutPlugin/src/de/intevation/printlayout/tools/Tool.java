/*
 * Tool.java
 * ---------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.tools;

/**
 * This interface identifies a tool to the LayoutFrame.<br>
 * It can be activated and deactivated and should have
 * a unique name.<br>
 * A tool can also have an overlay for rendering above
 * the SVG drawing. If it wants to do so it has to 
 * implement the org.apache.batik.swing.gvt.Overlay interface.
 * It is checked at runtime (with instanceof) if the tool
 * implements this interface. When it does it is
 * added to the list of rendered overlays.
 */
public interface Tool
{
	/**
	 * the unique identifier of the tool.
	 * @return the unique identifier
	 */
	String getToolIdentifier();

	/**
	 * called on activation and deactivation of the tool.
	 * The tool should be able to handle the case that
	 * it receives the on/off call even if its still
	 * on or off.
	 * @param inUse true if tool should activate itself.<br>
	 *              false if tool should deactivate itself.
	 */
	void setInUse(boolean inUse);
}
// end of file
