/*
 * ScaleUpdater.java
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

import java.io.Serializable;

import org.w3c.dom.NodeList;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGElement;               
import org.w3c.dom.svg.SVGMatrix;               
import org.w3c.dom.svg.SVGTextElement;

import org.apache.batik.dom.AbstractElement;

import java.text.NumberFormat;

public class ScaleUpdater
implements   Serializable, ExtraData.ChangeListener
{
	protected String scaleID;

	protected NumberFormat format;

	public ScaleUpdater() {
		format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(3);
		format.setGroupingUsed(false);
	}

	public ScaleUpdater(String id) {
		this();
		scaleID = id;
	}

	public String getScaleID() {
		return scaleID;
	}

	public void setScaleId(String id) {
		scaleID = id;
	}

	public void elementTransformed(ExtraData.Event evt) {

		SVGLocatable loc = (SVGLocatable)evt.getElement();

		DocumentManager manager = (DocumentManager)evt.getSource();

		elementTransformed(loc, manager);
	}

	public void elementTransformed(
		SVGLocatable    loc,
		DocumentManager manager
	) {
		String id =
			((AbstractElement)loc).getAttributeNS(null, "id");

		if (id == null)
			return;

		Object data = manager.getData(id);

		if (!(data instanceof MapData))
			return;

		MapData map = (MapData)data;

		SVGDocument document = manager.getSVGDocument();

		SVGElement sheet = (SVGElement)document.getElementById(
			DocumentManager.DOCUMENT_SHEET);

		if (sheet == null)
			return;

		SVGMatrix xform = loc.getTransformToElement((SVGElement)sheet);

		MatrixTools.Decomposition d = MatrixTools.decompose(xform);

		double scale = 
			Math.abs(1000d / (map.getInitialScale() * Math.max(d.scx, d.scy)));

		if (scale > 10000d)
			scale = Math.round(scale);

		SVGElement textGroup = (SVGElement)document.getElementById(scaleID);

		if (textGroup == null)
			return;

		NodeList children = textGroup.getChildNodes();

		SVGTextElement text = null;

		for (int i = children.getLength()-1; i >= 0; --i) {
			AbstractElement child = (AbstractElement)children.item(i);
			if (child instanceof SVGTextElement) {
				text = (SVGTextElement)child;
				break;
			}
		}

		if (text == null)
			return;

		text.setTextContent("1:" + format.format(scale));
	}
}
// end of file
