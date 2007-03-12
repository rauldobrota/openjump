/*
 * PreviewMap.java
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

import com.vividsolutions.jts.geom.Envelope;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;

import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager; 
import com.vividsolutions.jump.workbench.ui.renderer.ThreadQueue;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.batik.dom.util.XLinkSupport; 

import org.w3c.dom.svg.SVGDocument;

import java.awt.Dimension;
import java.awt.Graphics2D;

import java.awt.image.BufferedImage;

import de.intevation.printlayout.beans.PreviewData;

import de.intevation.printlayout.batik.IncoreImageProtocolHandler;

import de.intevation.printlayout.util.OpenJumpRenderingSync;

/**
 * Instances of this class are used to convert the
 * content of OJ's LayerViewPanel to an preview image.
 * It's implemented as a DocumentModifier to run
 * in the UpdateManager of the DocumentManager.
 */
public class PreviewMap
implements   DocumentManager.DocumentModifier
{
	/**
	 * This is used as a prefix to identify preview maps
	 * in xlink:href attributes of image elements.
	 */
	public static final String PREVIEW_MAP = "preview-map";

	/**
	 * The plugin context is need to access the LayerViewPanel.
	 */
	protected PlugInContext pluginContext;


	/**
	 * Creates uninitialized PreviewMap
	 */
	protected PreviewMap() {
	}

	/**
	 * Creates a PreviewMap wired to the plugin context.
	 * @param pluginContext the context to access the LayerViewPanel
	 */
	public PreviewMap(PlugInContext pluginContext) {
		this.pluginContext = pluginContext;
	}

	/**
	 * implements the run() method of the DocumentModifier 
	 * interface. The map is rendered to an image
	 * and added to the SVG document afterwards.
	 */
	public Object run(DocumentManager documentManager) {

		SVGDocument document = documentManager.getSVGDocument();

		AbstractElement sheet =
			(AbstractElement)document.getElementById(DocumentManager.DOCUMENT_SHEET);

		if (sheet == null) {
			System.err.println("no sheet found");
			return null;
		}

		final LayerViewPanel layerViewPanel = pluginContext.getLayerViewPanel();

		Viewport vp = layerViewPanel.getViewport();
		
		Dimension xenv = layerViewPanel.getSize(null);

		double geo2screen = vp.getScale();
		Envelope envelope = vp.getEnvelopeInModelCoordinates();

		RenderingManager rms = layerViewPanel.getRenderingManager();
		ThreadQueue q = rms.getDefaultRendererThreadQueue();

    BufferedImage bitmap = new BufferedImage(
			xenv.width, xenv.height,
			BufferedImage.TYPE_INT_RGB);

		final Graphics2D g2d = bitmap.createGraphics();

		OpenJumpRenderingSync sync = new OpenJumpRenderingSync(
			new Runnable() {
				public void run() {
					layerViewPanel.paintComponent(g2d);
				}
			});

		g2d.dispose();

		IncoreImageProtocolHandler iiph =
			IncoreImageProtocolHandler.getInstance();

		String imageID = iiph.storeImage(bitmap, PREVIEW_MAP);

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractElement image =
			(AbstractElement)document.createElementNS(svgNS, "image");

		image.setAttributeNS(null, "width",  String.valueOf(xenv.getWidth()));
		image.setAttributeNS(null, "height", String.valueOf(xenv.getHeight()));

		image.setAttributeNS(
			XLinkSupport.XLINK_NAMESPACE_URI, "xlink:href",
			IncoreImageProtocolHandler.INCORE_IMAGE + ":" + imageID);

		image.setAttributeNS(null, "x", "0");
		image.setAttributeNS(null, "y", "0");
		
		AbstractElement xform =
			(AbstractElement)document.createElementNS(svgNS, "g");

		double scale2paper = fitToPaper(documentManager, xenv);

		xform.setAttributeNS(
			null, "transform", "scale(" + scale2paper + ")");

		String id = documentManager.uniqueObjectID();

		xform.setAttributeNS(null, "id", id);

		xform.appendChild(image);

		sheet.appendChild(xform);

		// add the initial scale to beans

		PreviewData previewData = new PreviewData(
			geo2screen,
			envelope);

		documentManager.setData(id, previewData);

		return null;
	}

	private static double fitToPaper(
		DocumentManager documentManager,
		Dimension       env
	) {
		double [] paper = new double[2];
		documentManager.getPaperSize(paper);

		double s1 = paper[0]/env.getWidth();
		double s2 = paper[1]/env.getHeight();

		return Math.min(s1, s2);
	}
}
// end of file
