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

import de.intevation.printlayout.beans.PreviewData;

import de.intevation.printlayout.batik.IncoreImageProtocolHandler;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import com.vividsolutions.jts.geom.Envelope;

import java.awt.geom.NoninvertibleTransformException;

public class PreviewMapReplacer
implements   DocumentManager.PostProcessor
{
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

		Envelope current = null;

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

			if (current == null)
				current = vp.getEnvelopeInModelCoordinates();

			// zoom to spot
			try {
				vp.zoom(previewData.asEnvelope());
			}
			catch (NoninvertibleTransformException nite) {
				continue;
			}

			// TODO: Do rendering here
		}

		if (current != null) // restore original spot
			try {
				vp.zoom(current);
			}
			catch (NoninvertibleTransformException nite) {
			}
		 
		return document;
	}
}
// end of file
