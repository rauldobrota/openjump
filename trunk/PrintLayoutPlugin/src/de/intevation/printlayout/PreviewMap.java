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

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager; 
import com.vividsolutions.jump.workbench.ui.renderer.ThreadQueue;

import com.vividsolutions.jump.workbench.ui.Viewport;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.batik.dom.util.XLinkSupport; 

import org.w3c.dom.svg.SVGDocument;

import java.awt.Dimension;
import java.awt.Graphics2D;

import java.awt.image.BufferedImage;

import de.intevation.printlayout.beans.MapData;

import de.intevation.printlayout.batik.IncoreImageProtocolHandler;

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

		LayerViewPanel layerViewPanel = pluginContext.getLayerViewPanel();

		Viewport vp = layerViewPanel.getViewport();
		
		Dimension xenv = layerViewPanel.getSize(null);

		double geo2screen = vp.getScale();

		RenderingManager rms = layerViewPanel.getRenderingManager();
		ThreadQueue q = rms.getDefaultRendererThreadQueue();

    BufferedImage bitmap = new BufferedImage(
			xenv.width, xenv.height,
			BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = bitmap.createGraphics();

		final int [] lock = { 0 };

		ThreadQueue.Listener l = new ThreadQueue.Listener() {
			public void allRunningThreadsFinished() {
				synchronized (lock) { 
					lock[0] = 1;
					lock.notify(); 
				}
			}
		};

		q.add(l);

		// do the rendering
		//layerViewPanel.repaint();
		layerViewPanel.paintComponent(g2d);

		try {
			synchronized (lock) { 
				int i = 10;
				while (lock[0] == 0 && --i > 0)
					lock.wait(100); 
			} 
		}
		catch (InterruptedException ie) {}
		q.remove(l);

		g2d.dispose();

		IncoreImageProtocolHandler iiph =
			IncoreImageProtocolHandler.getInstance();

		String imageID = iiph.storeImage(bitmap);

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

		/*
		MapData mapData = new MapData(geo2screen);
		documentManager.setData(id, mapData);
		*/

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
