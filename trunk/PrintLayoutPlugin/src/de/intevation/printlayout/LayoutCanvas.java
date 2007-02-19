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
 */
public class LayoutCanvas
extends      JSVGCanvas
{
	public interface DamagedRegion {
		Rectangle damagedRegion();
	}

	protected ArrayList damagedRegions;

	protected Rectangle lastDamage;

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

	public synchronized void addDamagedRegion(DamagedRegion region) {
		if (region != null)
			if (damagedRegions == null) {
				damagedRegions = new ArrayList(3);
				damagedRegions.add(region);
			}
			else if (!damagedRegions.contains(region))
				damagedRegions.add(region);
	}

	public synchronized void removeDamagedRegion(DamagedRegion region) {
		if (region != null
		&&  damagedRegions != null
		&&  damagedRegions.remove(region)
		&&  damagedRegions.isEmpty())
			damagedRegions = null;
	}

	public static final Rectangle enlarge(Rectangle r1, Rectangle r2) {
		if (r1 == null) return r2;
		if (r2 == null) return r1;
		r1.add(r2);
		return r1;
	}

	public static final Rectangle enlarge(Rectangle rect, int extra) {
		if (rect == null)
			return null;
		int twoExtra = extra << 1;
		return new Rectangle(
			rect.x - extra, rect.y - extra,
			rect.width + twoExtra, rect.height + twoExtra) ;
	}

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

	public void paintImmediately(Rectangle rect) {
		Rectangle damagedRegion = allDamagedRegions();
		if (damagedRegion != null)
			rect.add(damagedRegion);
		super.paintImmediately(rect);
	}

	public void repaint(Rectangle rect) {
		Rectangle damagedRegion = allDamagedRegions();
		if (damagedRegion != null)
			rect.add(damagedRegion);
		super.repaint(rect);
	}

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
