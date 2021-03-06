/*
 * PatternExt.java
 * ---------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.batik;

import java.awt.Paint;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.apache.batik.svggen.SVGTexturePaint;

import java.awt.image.BufferedImage;

import java.awt.geom.Rectangle2D;

import java.awt.TexturePaint;

import com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern;

import com.vividsolutions.jump.util.Blackboard;

/**
 * This converts OpenJump BasicFillPattern to java.awt.Texture
 * so it can be used in svggen renderings.
 */ 
public class PatternExt
extends      DefaultExtensionHandler
{
	public PatternExt() {
	}

	/**
	 * Filters out
	 * com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern 
	 * fill patterns, renders them to a texture and use this
	 * texture to describe the SVG fill.
	 * @param paint incoming Paint object.
	 * @param generatorContext the SVG generator context
	 * @return a SVGPaintDescriptor of a java.awt.TexturePaint 
	 *         if the paint is a BasicFillPattern.<br>
	 *         Else the return value of handlePaint() of the
	 *         super class is return.
	 */
	public SVGPaintDescriptor handlePaint(
		Paint paint,
    SVGGeneratorContext generatorContext
	) {
		if (paint instanceof BasicFillPattern) {

			BasicFillPattern pattern = (BasicFillPattern)paint;

			Blackboard properties = pattern.getProperties();

			BufferedImage image = pattern.createImage(properties);

			TexturePaint texture = new TexturePaint(
				image,
				new Rectangle2D.Double(0, 0, image.getWidth(),
				image.getHeight()));

			SVGTexturePaint stp = new SVGTexturePaint(generatorContext);

			return stp.toSVG(texture);
		}
		return super.handlePaint(paint, generatorContext);
	}
}
// end of file
