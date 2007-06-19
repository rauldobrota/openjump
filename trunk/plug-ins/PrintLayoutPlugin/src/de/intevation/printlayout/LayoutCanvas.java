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

import java.awt.Rectangle;

import java.util.ArrayList;

/**
 * A special JSVGCanvas that can handle damaged screen regions.
 * <br>
 * The canvas is able to track damaged region on the screen.
 * Background: Batik tries to minimize the area of regions 
 * to redraw if a modification to the SVG is done. This is
 * a fine idea but has the problem if you have some overlays
 * (e.g. for drawing, selecting, rulers etc.) Batik does not
 * know there extend on screen. Therefore the damaged regions
 * are calculated to small by default and the redraw leeds
 * to artifacts.
 * <br>
 * By implementing the DamagedRegion interface and registerin
 * to this canvas an overlay can  determine its damage in
 * advance before the rendering of the scene is done.
 * On repaint() requests the redraw region is expanded to 
 * the the bounding box of the union of all damaged regions
 * plus the damaged cause during the last rendering. This
 * ensures a correct refresh of the canvas.
 */
public class LayoutCanvas
extends      JSVGCanvas
{
	/**
	 * interface to calculate a damaged region in advance.
	 */
	public interface DamagedRegion {
		/**
		 * calculate a damaged region in advance.
		 * @return area of the damage. maybe null.
		 */
		Rectangle damagedRegion();
	}

	/**
	 * list of all damaged region calculators
	 */
	protected ArrayList damagedRegions;

	/**
	 * damage caused by last rendering
	 */
	protected Rectangle lastDamage;

	/**
	 * creates a standard canvas.
	 * use the three argument version instead.
	 */
	public LayoutCanvas() {
	}

	/**
	 * creates a LayoutCanvas.
	 * @param agent          user agent for screen binding.
	 * @param eventsEnabled  should XML events being processed?
	 * @param selectableText should SVG text elements be selectable?
	 */
	public LayoutCanvas(
		SVGUserAgent agent,
		boolean      eventsEnabled,
		boolean      selectableText
	) {
		super(agent, eventsEnabled, selectableText);
	}

	/**
	 * Access to the user agent used by this canvas.
	 * @return the user agent
	 */
	public UserAgent getUserAgent() {
		return userAgent;
	}

	/**
	 * adds a DamagedRegion to the list of DamagedRegions.
	 * @param region the region to add
	 */
	public synchronized void addDamagedRegion(DamagedRegion region) {
		if (region != null)
			if (damagedRegions == null) {
				damagedRegions = new ArrayList(3);
				damagedRegions.add(region);
			}
			else if (!damagedRegions.contains(region))
				damagedRegions.add(region);
	}

	/**
	 * removes a DamagedRegion from the list of DamagedRegions.
	 * @param region the region to remove
	 */
	public synchronized void removeDamagedRegion(DamagedRegion region) {
		if (region != null
		&&  damagedRegions != null
		&&  damagedRegions.remove(region)
		&&  damagedRegions.isEmpty())
			damagedRegions = null;
	}

	/**
	 * static helper to calculate the enclosing rectangle of
	 * the two given rectangles.
	 * @param r1 first  rectangle
	 * @param r2 second rectangle
	 * @return null if both arguments are null.<br>
	 *         if one is null and one is not null the
	 *         not null instance is returned.<br>
	 *         if both are not null the first rectangle
	 *         is enlarged to the enclosing rectangle of both.
	 */
	public static final Rectangle enlarge(Rectangle r1, Rectangle r2) {
		if (r1 == null) return r2;
		if (r2 == null) return r1;
		r1.add(r2);
		return r1;
	}

	/**
	 * static helper to enlarge a rectangle with a given border
	 * @param rect the rectangle to be enlarged
	 * @param extra the border width and height
	 * @return null if rect is null.<br>
	 *         else a new rectangle with the extra border.
	 */
	public static final Rectangle enlarge(Rectangle rect, int extra) {
		if (rect == null)
			return null;
		int twoExtra = extra << 1;
		return new Rectangle(
			rect.x - extra, rect.y - extra,
			rect.width + twoExtra, rect.height + twoExtra) ;
	}

	/**
	 * calculate the bounding box of the union of all
	 * damaged regions including the damage by the
	 * last rendering. Afterwards this total damage
	 * is remembered as the last damage.
	 */
	protected Rectangle allDamagedRegions() {
		if (damagedRegions == null) {
			Rectangle totalDamage = lastDamage;
			lastDamage = null;
			return totalDamage;
		}

		Rectangle damaged = null;

		for (int i = damagedRegions.size()-1; i >= 0; --i)
			damaged = enlarge(
				damaged, ((DamagedRegion)damagedRegions.get(i)).damagedRegion());

		damaged = enlarge(damaged, 4);

		Rectangle totalDamage = lastDamage != null
			? enlarge(new Rectangle(lastDamage), damaged)
			: damaged;

		lastDamage = damaged;

		return totalDamage;
	}

	/**
	 * overrides paintImmediately() from super class.
	 * enlarge the damage region if needed.
	 * For arguments see base class.
	 */
	public void paintImmediately(int x, int y, int width, int height) {
		Rectangle damagedRegion = allDamagedRegions();
		if (damagedRegion == null)
			super.paintImmediately(x, y, width, height);
		else {
			Rectangle rect = new Rectangle(x, y, width, height);
			rect.add(damagedRegion);
			super.paintImmediately(rect.x, rect.y, rect.width, rect.height);
		}
	}

	/**
	 * overrides paintImmediately() from super class.
	 * enlarge the damage region if needed.
	 * For arguments see base class.
	 */
	public void paintImmediately(Rectangle rect) {
		Rectangle damagedRegion = allDamagedRegions();
		if (damagedRegion != null)
			rect.add(damagedRegion);
		super.paintImmediately(rect);
	}

	/**
	 * overrides paint() from super class.
	 * enlarge the damage region if needed.
	 * For arguments see base class.
	 */
	public void repaint(Rectangle rect) {
		Rectangle damagedRegion = allDamagedRegions();
		if (damagedRegion != null)
			rect.add(damagedRegion);
		super.repaint(rect);
	}

	/**
	 * overrides paint() from super class.
	 * enlarge the damage region if needed.
	 * For arguments see base class.
	 */
	public void repaint(int x, int y, int width, int height) {
		Rectangle damagedRegion = allDamagedRegions();
		if (damagedRegion == null)
			super.repaint(x, y, width, height);
		else {
			Rectangle rect = new Rectangle(x, y, width, height);
			rect.add(damagedRegion);
			super.repaint(rect.x, rect.y, rect.width, rect.height);
		}
	}
	
	/**
	 * overrides paint() from super class.
	 * enlarge the damage region if needed.
	 * For arguments see base class.
	 */
	public void repaint(long tm, int x, int y, int width, int height) {
		Rectangle damagedRegion = allDamagedRegions();
		if (damagedRegion == null)
			super.repaint(tm, x, y, width, height);
		else {
			Rectangle rect = new Rectangle(x, y, width, height);
			rect.add(damagedRegion);
			super.repaint(tm, rect.x, rect.y, rect.width, rect.height);
		}
	}
}
// end of file
