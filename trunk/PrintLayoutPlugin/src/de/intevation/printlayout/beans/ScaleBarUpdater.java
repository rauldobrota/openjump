/*
 * ScaleBarUpdater.java
 * --------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout.beans;

import com.vividsolutions.jump.workbench.ui.plugin.scalebar.RoundQuantity;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.IncrementChooser;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.MetricSystem;

import java.io.Serializable;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGElement;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SVGDOMImplementation; 

import de.intevation.printlayout.util.MatrixTools;

import de.intevation.printlayout.DocumentManager;

/**
 * Constructs and maintains a scale bar for a given
 * map and given scale.
 */
public class ScaleBarUpdater
implements   Serializable, DocumentManager.ElementGenerator
{
	/** mainly copied out of ScaleBarRenderer */

  /**
   *  Height of the increment boxes, in view-space units.
   */
	protected final static int BAR_HEIGHT = 5;
	protected final static String FILL2 = "#FFCCCC";
	protected final static String FILL1 = "white";

	/**
	 *  Distance from the right edge, in view-space units.
	 */
	protected final static int HORIZONTAL_MARGIN = 3;

	/**
	 *  In view-space units; the actual increment may be a bit larger or smaller
	 *  than this amount.
	 */
	protected final static String LINE_COLOR            = "black";
	protected final static String TEXT_COLOR            = "black";
	protected final static String UNIT_TEXT_COLOR       = "blue";

	/**
	 *  Distance from the bottom edge, in view-space units.
	 */
	private final static int VERTICAL_MARGIN = 3;

  private final static int INCREMENT_COUNT = 5;

	protected double scale;

	protected String mapID;

	public ScaleBarUpdater() {
	}

	public ScaleBarUpdater(double scale, String mapID) {
		this.scale = scale;
		this.mapID = mapID;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public String getMapID() {
		return mapID;
	}

	public void setMapID(String mapID) {
		this.mapID = mapID;
	}

	public AbstractElement generateElement(DocumentManager manager) {
    SVGDocument document = manager.getSVGDocument();

		SVGLocatable map = (SVGLocatable)document.getElementById(mapID); 
		SVGLocatable sheet =
			(SVGLocatable)document.getElementById(DocumentManager.DOCUMENT_SHEET); 

		if (map == null || sheet == null)
			return null;

 		SVGMatrix xform = map.getTransformToElement((SVGElement)sheet);

		MatrixTools.Decomposition d = MatrixTools.decompose(xform);

		double gscale = Math.abs(scale * Math.max(d.scx, d.scy));

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		double mapWidth = map.getBBox().getWidth();

		RoundQuantity increment =
			new IncrementChooser().chooseGoodIncrement(
				new MetricSystem(1).createUnits(),
        (0.33d/5d * mapWidth ) / gscale);

		AbstractElement group = 
			(AbstractElement)document.createElementNS(svgNS, "g");

		generateIncrements(document, increment, gscale, INCREMENT_COUNT, group);
			
		return group;
	}

	protected void generateIncrements(
		SVGDocument     document,
		RoundQuantity   increment,
		double          gscale,
		int             incrementCount,
		AbstractElement group
	) {
		for (int i = 0; i < incrementCount; ++i) {
			group.appendChild(
				generateIncrement(document, i, increment, gscale));
			group.appendChild(
				generateLabel(document, i, incrementCount, increment, gscale));
		}
	}

	protected static int barBottom() {
		return 0 - VERTICAL_MARGIN;
	}

	protected static int barTop() {
		return barBottom() - BAR_HEIGHT;
	}
       
	protected AbstractElement generateIncrement(
		SVGDocument   document,
		int           i, 
		RoundQuantity increment,
		double        gscale
	) {
		double x = x(i, increment, gscale);
		double y = barTop();
		double width = x(i+1, increment, gscale) - x(i, increment, gscale);
		double height = barBottom() - barTop();

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractElement rect = 
			(AbstractElement)document.createElementNS(svgNS, "rect");

		rect.setAttributeNS(null, "x",      String.valueOf(x));
		rect.setAttributeNS(null, "y",      String.valueOf(y));
		rect.setAttributeNS(null, "width",  String.valueOf(width));
		rect.setAttributeNS(null, "height", String.valueOf(height));
		rect.setAttributeNS(null, "stroke-width", "0.5");
		rect.setAttributeNS(null, "stroke", LINE_COLOR);

		String color = (i % 2) == 0 ? FILL1 : FILL2;

		rect.setAttributeNS(null, "fill", color);

		return rect;
	}

	protected AbstractElement generateLabel(
		SVGDocument   document,
		int           i, 
		int           incrementCount,
		RoundQuantity increment,
		double        gscale
	) {
		String text;
		String color;
		String fontWeight;
		
		if (i == incrementCount-1) {
			text       = increment.getUnit().getName();
			color      = UNIT_TEXT_COLOR;
			fontWeight = "bold";
		}
		else {
			text = new RoundQuantity(
				increment.getMantissa() * (i + 1),
				increment.getExponent(),
				increment.getUnit()).getAmountString();
			color      = TEXT_COLOR;
			fontWeight = null;
		}

		double x = 0.5d * (x(i+1, increment, gscale) + x(i, increment, gscale));

		double y = -3.75d;

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractElement t =
			(AbstractElement)document.createElementNS(svgNS, "text");

		t.setAttributeNS(null, "x",           String.valueOf(x));
		t.setAttributeNS(null, "y",           String.valueOf(y));
		t.setAttributeNS(null, "text-anchor", "middle");
		t.setAttributeNS(null, "fill",        color);
		t.setAttributeNS(null, "font-family", "Dialog");
		t.setAttributeNS(null, "font-size",   "5");
		if (fontWeight != null)
			t.setAttributeNS(null, "font-weight", fontWeight);
		t.setTextContent(text);
		return t;
	}

	protected static double x(
		int           i, 
		RoundQuantity increment, 
		double        gscale
	) {
    return HORIZONTAL_MARGIN + i * increment.getModelValue() * gscale;
	}
}
// end of file
