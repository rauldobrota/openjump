/*
 * PreviewMapReplacer.java
 * -----------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.util.XLinkSupport; 

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import de.intevation.printlayout.beans.PreviewData;

import de.intevation.printlayout.batik.IncoreImageProtocolHandler;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import com.vividsolutions.jts.geom.Envelope;

import java.awt.geom.NoninvertibleTransformException;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import java.util.ArrayList;

public class PreviewMapReplacer
implements   DocumentManager.Processor
{
	public static final String EXTRA_ZOOM_WAIT =
		"de.intevation.printlayout.extra.zoom.wait";

	/**
	 * If the system property de.intevation.no.preview.caching
	 * is set to true the generated SVG map is not stored for caching.
	 */
	public static final boolean NO_PREVIEW_CACHING =
		Boolean.getBoolean("de.intevation.printlayout.no.preview.caching");

	public static final String PREFIX =
		IncoreImageProtocolHandler.INCORE_IMAGE + 
		':' + 
		PreviewMap.PREVIEW_MAP;

	/**
	 * The plugin context is need to access the LayerViewPanel.
	 */
	protected PlugInContext pluginContext;

	public PreviewMapReplacer() {
	}

	public PreviewMapReplacer(PlugInContext pluginContext) {
		this.pluginContext = pluginContext;
	}

	public AbstractDocument postProcess(
		AbstractDocument document,
		DocumentManager  documentManager
	) {
		NodeList images = document.getElementsByTagName("image");

		LayerViewPanel layerViewPanel = pluginContext.getLayerViewPanel();
		Viewport vp = layerViewPanel.getViewport();

		Envelope first = null;

		ArrayList replacements = new ArrayList();

		double [] paperSize = documentManager.getPaperSize();

		Integer zoomWait = Options.getInstance().getInteger(EXTRA_ZOOM_WAIT);

		for (int N = images.getLength(), i = 0; i < N; ++i) {
			AbstractElement image = (AbstractElement)images.item(i);

			String href = XLinkSupport.getXLinkHref(image);

			if (href == null || !href.startsWith(PREFIX))
				continue;

			AbstractElement parent = (AbstractElement)image.getParentNode();

			if (parent == null)
				continue;

			String id = parent.getAttributeNS(null, "id");
			if (id == null || !id.startsWith(DocumentManager.OBJECT_ID))
				continue;

			Object data = documentManager.getData(id);

			if (!(data instanceof PreviewData))
				continue;

			PreviewData previewData = (PreviewData)data;

			Reference cache = previewData.fetchCache();

			Element root = cache != null
				? (Element)cache.get()
				: null;

			if (root == null) { // not in cache

				Envelope current = vp.getEnvelopeInModelCoordinates();

				if (first == null)
					first = current;

				Envelope env = previewData.asEnvelope();

				if (!env.equals(current)) // zoom to spot
					try {
						vp.zoom(env);
						if (zoomWait != null)
							try {
								Thread.sleep(Math.max(0, zoomWait.intValue())*1000l);
							}
							catch (InterruptedException ie) {
							}
					}
					catch (NoninvertibleTransformException nite) {
						continue;
					}

				Map2SVGConverter map2svg = Map2SVG.createMap2SVGConverter(pluginContext);

				root = map2svg.createSVG(
					document, 
					null, 
					null, 
					paperSize,
					Map2SVG.getSimplificationTolerance());

				if (!NO_PREVIEW_CACHING)
					previewData.storeCache(new SoftReference(root));
			}
			else { // found in cache
				System.err.println("found in cache");
				root = (Element)document.importNode(root, true, true);
			}
			replacements.add(new Object[] { parent, root, image });
		}

		images = null;

		if (first != null) // restore original spot
			try {
				vp.zoom(first);
				// no need to wait here
			}
			catch (NoninvertibleTransformException nite) {
			}

		for (int i = replacements.size()-1; i >= 0; --i) {
			Object [] what = (Object [])replacements.get(i);
			Element p = (Element)what[0];
			Element n = (Element)what[1];
			Element o = (Element)what[2];
			p.replaceChild(n, o);
		}
		replacements.clear();
		 
		return document;
	}
}
// end of file
