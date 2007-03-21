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
import com.vividsolutions.jump.workbench.ui.renderer.ThreadQueue;

import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;

import com.vividsolutions.jump.workbench.ui.Viewport;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import java.util.List;

import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.AbstractDocument;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.batik.svggen.CachedImageHandlerBase64Encoder;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGSyntax;

import org.w3c.dom.svg.SVGDocument;

import java.awt.Dimension;

import java.awt.geom.Rectangle2D;

import de.intevation.printlayout.beans.MapData;

import de.intevation.printlayout.batik.PatternExt;
import de.intevation.printlayout.batik.ClippingSVGGraphics2D;
import de.intevation.printlayout.batik.StyleSheetHandler;

import de.intevation.printlayout.pathcompact.PathCompactor;

import de.intevation.printlayout.util.ElementUtils;

import de.intevation.printlayout.jump.PreciseJava2DConverter;
import de.intevation.printlayout.jump.SimplifyingJava2DConverter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.CDATASection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.lang.reflect.Method;

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
	 * If the system property de.intevation.printlayout.no.map.clip
	 * is set to true the generated SVG map is not clipped.
	 */
	public static final boolean NO_MAP_CLIP =
		Boolean.getBoolean("de.intevation.printlayout.no.map.clip");

  /**
	 * If the system property de.intevation.printlayout.optimize.map.svg
	 * is set to true the generated SVG map is optimized.
	 */
	public static final boolean OPTIMIZE_MAP_SVG 
		= Boolean.getBoolean("de.intevation.printlayout.optimize.map.svg");

	/**
	 * The Batik DOM implemenmentation is slow.
	 * Rendering and optimizing with the Java on board DOM is much faster.
	 * Set this property if you want the Batik DOM.
	 */
	public static final boolean USE_BATIK_DOM
		= Boolean.getBoolean("de.intevation.printlayout.optimize.map.batik.dom");

	/**
	 * If the system property de.intevation.printlayout.use.css.map
	 * is set to true the generated SVG style attributes are expressed as CSS.
	 */
	public static final boolean USE_CSS =
		Boolean.getBoolean("de.intevation.printlayout.use.css.map");

	/**
	 * If the system property de.intevation.printlayout.gc.calls
	 * is set to a positive integer N the garbage collection is called
	 * N times after each memory critcal operation. Beware: This may prevent
	 * caching of results. Therefore it is set to 0 by default.
	 */
	public static final int GC_CALLS =
		Math.max(0,
			Integer.getInteger("de.intevation.printlayout.gc.calls", 0)
				.intValue());

	public static final Double SIMPLIFY_TOLERANCE = getToleranceFromProperties();

	private static final Double getToleranceFromProperties() {
		try {
			String s = System.getProperty(
				"de.intevation.printlayout.simplify.tolerance");
			return s != null ? new Double(Math.abs(Double.parseDouble(s))) : null;
		}
		catch (NumberFormatException nfe) {
			return null;
		}
	}

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
	 * Trys to find out if the Viewport class has a
	 * 'setJava2DConverter'.
	 * @return the method, null if not found
	 */
	public static final Method getJava2DConverterSetter() {
		try {
			Method method = Viewport.class.getMethod(
				"setJava2DConverter", new Class[] { Java2DConverter.class });
			System.err.println("Viewport.setJava2DConverter() found");
			return method;
		}
		catch (Exception e) {
			System.err.println("Viewport.setJava2DConverter() not found");
			return null;
		}
	}

	public Element createSVG(
		Document    document, 
		double []   geo2screen,
		Dimension   xenv
	) {
		return createSVG(document, geo2screen, xenv, null);
	}

	public Element createSVG(
		Document    document, 
		double []   geo2screen,
		Dimension   xenv,
		Double      tolerance
	) {
		LayerViewPanel layerViewPanel = pluginContext.getLayerViewPanel();

		Viewport vp = layerViewPanel.getViewport();
		
		xenv = layerViewPanel.getSize(xenv);

		if (geo2screen != null)
			geo2screen[0] = vp.getScale();

		// setup the SVG generator ...
		Document doc = USE_BATIK_DOM ? null : createDocument();

		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(
			doc != null ? doc : document);

		ctx.setPrecision(12);

		ctx.setGenericImageHandler(new CachedImageHandlerBase64Encoder());

		ctx.setExtensionHandler(new PatternExt());

		// use CSS?
    CDATASection styleSheetSection;
		if (USE_CSS) {
    	styleSheetSection = (doc != null ? doc : document).createCDATASection(""); 
			ctx.setStyleHandler(new StyleSheetHandler(styleSheetSection));
		}
		else
			styleSheetSection = null;
		
		SVGGraphics2D svgGenerator;

		// clip the map?
		if (NO_MAP_CLIP)
			svgGenerator = new SVGGraphics2D(ctx, false);
		else {
			svgGenerator = new ClippingSVGGraphics2D(
				ctx, false,
				new Rectangle2D.Double(
					0d, 0d,
					xenv.getWidth(), xenv.getHeight()));
			((ClippingSVGGraphics2D)svgGenerator).setConvertToGeneralPaths(true);
		}

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

		// look if we can use Viewport.getJava2DConverter()
		Method setJava2DConverter = getJava2DConverterSetter();

		Java2DConverter oldConverter = null;

		if (setJava2DConverter != null) {
			oldConverter = vp.getJava2DConverter();
			try {
				Java2DConverter converter = tolerance != null
					? new SimplifyingJava2DConverter(vp, tolerance.doubleValue())
					: new PreciseJava2DConverter(vp);

				setJava2DConverter.invoke(vp, new Object[] { converter });
			}
			catch (Exception e) {
				e.printStackTrace();
				oldConverter = null;
			}
		}

		long renderStartTime = System.currentTimeMillis();

		ThreadQueue queue = rms.getDefaultRendererThreadQueue();
		rms.renderAll();
		final boolean [] locked = { true };

		queue.add(new Runnable() {
			public void run() {
				synchronized (locked) {
					locked[0] = false;
					locked.notify();
				}
			}
		});

		layerViewPanel.paintComponent(svgGenerator);

		try {
			synchronized (locked) {
				int i = 20; // max 2min
				while (locked[0] && i-- > 0)
					locked.wait(6000);
			}
			do {
				Thread.sleep(1000);
			}
			while (queue.getRunningThreads() > 0);
		}
		catch (InterruptedException ie) {
		}

		// restore old converter
		if (setJava2DConverter != null && oldConverter != null) {
			try {
				setJava2DConverter.invoke(vp, new Object [] { oldConverter });
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		long renderStopTime = System.currentTimeMillis();

		// restore the previous image caching behavior
		for (int i = 0; i < N; ++i) {
			Layer    layer    = (Layer)layers.get(i);		
			Renderer renderer = rms.getRenderer(layer);
			if (renderer.isRendering())
				renderer.cancel();
			if (renderer instanceof LayerRenderer) {
				LayerRenderer layerRenderer = (LayerRenderer)renderer;
				layerRenderer.setMaxFeatures(oldMaxFeaures[i]);
			}
		}

		System.err.println("rendering took [secs]: " +
			inSecs(renderStopTime-renderStartTime));

		oldMaxFeaures = null;

		// add the new SVG node to the DOM tree

		Element root = svgGenerator.getRoot();
		svgGenerator.dispose();
		svgGenerator = null;
		ctx = null;

		// adding the style sheet section
		if (styleSheetSection != null) {
			Element defs = ElementUtils.getElementById(root, SVGSyntax.ID_PREFIX_GENERIC_DEFS);
			Element style = (doc != null ? doc : document).createElementNS(
				SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STYLE_TAG);
			style.setAttributeNS(null, SVGSyntax.SVG_TYPE_ATTRIBUTE, "text/css");
			style.appendChild(styleSheetSection);
			defs.appendChild(style);
			styleSheetSection = null;
		}
		
		gc();

		if (OPTIMIZE_MAP_SVG) {
			System.err.println("Reordering paths...");
			long optStartTime = System.currentTimeMillis();
			PathCompactor.reorder(doc != null ? doc : document, root);
			System.err.println("Reordering done.");
			System.err.println("Compact paths...");
			PathCompactor.compactPathElements(root);
			long optStopTime = System.currentTimeMillis();
			System.err.println("Compact paths done.");
			System.err.println("Optimization took [secs]: " + 
				inSecs(optStopTime - optStartTime));

			gc();
		}

		// Uncomment this if you want to see the reason why
		// the bounding box of the map doesn't fit:
		//root.setAttributeNS(null, "overflow", "visible");
		root.setAttributeNS(null, "width",  String.valueOf(xenv.width));
		root.setAttributeNS(null, "height", String.valueOf(xenv.height));

		root.setAttributeNS(null, "x", "0");
		root.setAttributeNS(null, "y", "0");
		
		// import document if needed
		if (doc != null) {
			long importStartTime = System.currentTimeMillis();
			if (document instanceof AbstractDocument) {
				root = (Element)((AbstractDocument)document).importNode(
					root, true, true);
			}
			else 
				root = (Element)document.importNode(root, true);

			doc = null;

			long importStopTime = System.currentTimeMillis();
			System.err.println("importing DOM took [secs]: " +
				inSecs(importStopTime-importStartTime));
			gc();
		}

		return root;
	}

	/**
	 * Creates a new DOM document using the default factory.
	 * @return new DOM document or null if construction failed.
	 */
	public static final Document createDocument() {
		try {
			return DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.newDocument();
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
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

		double [] geo2screen = new double[1];

		Dimension xenv = new Dimension();

		Element root = createSVG(document, geo2screen, xenv, SIMPLIFY_TOLERANCE);

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

		MapData mapData = new MapData(geo2screen[0]);
		documentManager.setData(id, mapData);

		return null;
	}

	/**
	 * Triggers garbage collection once.
	 */
	private static final void gc() {
		if (GC_CALLS > 0) {
			System.err.println("used memory before gc [MB]: " + inMegaBytes(usedMemory()));
			gc(GC_CALLS);
			System.err.println("used memory after gc [MB]: " + inMegaBytes(usedMemory()));
		}
	}

	/**
	 * Triggers garbage collection N times.
	 * @param N number of garbage collections.
	 */
	private static final void gc(int N) {
		while (N-- > 0)
			System.gc();
	}

	/**
	 * Converts milli seconds to seconds.
	 * @param time in milli seconds
	 * @return tim in seconds
	 */
	private static final float inSecs(long time) {
		return (float)time*(1f/1000f);
	}

	/**
	 * Converts number of bytes to same in mega bytes.
	 * @param mem in bytes.
	 * @return mem in mega bytes
	 */
	private static final float inMegaBytes(long mem) {
		return (float)mem*(1f/(1024f*1024f));
	}

	/**
	 * Returns the currently used Java heap space.
	 * @return number of used heap space bytes currently in use.
	 */
	private static final long usedMemory() {
		Runtime rt = Runtime.getRuntime();
		return rt.totalMemory() - rt.freeMemory();
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
