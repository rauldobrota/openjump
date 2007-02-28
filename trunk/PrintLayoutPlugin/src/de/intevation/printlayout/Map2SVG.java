/*
 * Map2SVG.java
 * ------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import com.vividsolutions.jump.workbench.model.Layer;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

import com.vividsolutions.jump.workbench.ui.renderer.LayerRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;   
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager; 

import com.vividsolutions.jump.workbench.ui.Viewport;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import java.util.List;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.batik.svggen.CachedImageHandlerBase64Encoder;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;

import org.w3c.dom.svg.SVGDocument;

import java.awt.Dimension;

import de.intevation.printlayout.beans.MapData;

import de.intevation.printlayout.batik.PatternExt;

/**
 * Instances of this class are used to convert the
 * content of OJ's LayerViewPanel to SVG.
 * It's implemented as a DocumentModifier to run
 * in the UpdateManager of the DocumentManager.
 */
public class Map2SVG
implements   DocumentManager.DocumentModifier
{
	/**
	 * The plugin context is need to access the LayerViewPanel.
	 */
	protected PlugInContext pluginContext;

	/**
	 * Creates uninitialized Map2SVG
	 */
	protected Map2SVG() {
	}

	/**
	 * Creates a Map2SVG wired to the plugin context.
	 * @param pluginContext the context to access the LayerViewPanel
	 */
	public Map2SVG(PlugInContext pluginContext) {
		this.pluginContext = pluginContext;
	}

	/**
	 * implements the run() method of the DocumentModifier 
	 * interface. The map is rendered to an SVG context
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

		// setup the SVG generator ...

		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
		ctx.setPrecision(12);

		ctx.setGenericImageHandler(new CachedImageHandlerBase64Encoder());

		ctx.setExtensionHandler(new PatternExt());

		SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, false);

		RenderingManager rms = layerViewPanel.getRenderingManager();

		List layers = pluginContext.getLayerManager().getVisibleLayers(false);

		int N = layers.size();

		int [] oldMaxFeaures = new int[N];

		// prevent image caching
		for (int i = 0; i < N; ++i) {
			Layer    layer    = (Layer)layers.get(i);		
			Renderer renderer = rms.getRenderer(layer);

			if (renderer instanceof LayerRenderer) {
				LayerRenderer layerRenderer = (LayerRenderer)renderer;
				oldMaxFeaures[i] = layerRenderer.getMaxFeatures();
				layerRenderer.setMaxFeatures(Integer.MAX_VALUE);
			}
			else {
				System.err.println("unknown renderer type: " + renderer.getClass());
			}
		}

		// do the rendering
		layerViewPanel.repaint();
		layerViewPanel.paintComponent(svgGenerator);

		// restore the previous image caching behavior
		for (int i = 0; i < N; ++i) {
			Layer    layer    = (Layer)layers.get(i);		
			Renderer renderer = rms.getRenderer(layer);
			if (renderer instanceof LayerRenderer) {
				LayerRenderer layerRenderer = (LayerRenderer)renderer;
				layerRenderer.setMaxFeatures(oldMaxFeaures[i]);
			}
		}

		// add the new SVG node to the DOM tree

		AbstractElement root = (AbstractElement)svgGenerator.getRoot();
		svgGenerator.dispose();

		// Uncomment this if you want to see the reason why
		// the bounding box of the map doesn't fit:
		// root.setAttributeNS(null, "overflow", "visible");
		root.setAttributeNS(null, "width",  String.valueOf(xenv.getWidth()));
		root.setAttributeNS(null, "height", String.valueOf(xenv.getHeight()));

		root.setAttributeNS(null, "x", "0");
		root.setAttributeNS(null, "y", "0");

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractElement xform =
			(AbstractElement)document.createElementNS(svgNS, "g");

		double scale2paper = fitToPaper(documentManager, xenv);

		xform.setAttributeNS(
			null, "transform", "scale(" + scale2paper + ")");

		String id = documentManager.uniqueObjectID();

		xform.setAttributeNS(null, "id", id);

		xform.appendChild(root);

		sheet.appendChild(xform);

		// add the initial scale to beans


		MapData mapData = new MapData(geo2screen);
		documentManager.setData(id, mapData);

		return null;
	}

	private static double fitToPaper(
		DocumentManager documentManager,
		Dimension        env
	) {
		double [] paper = new double[2];
		documentManager.getPaperSize(paper);

		double s1 = paper[0]/env.getWidth();
		double s2 = paper[1]/env.getHeight();

		return Math.min(s1, s2);
	}
}
// end of file
