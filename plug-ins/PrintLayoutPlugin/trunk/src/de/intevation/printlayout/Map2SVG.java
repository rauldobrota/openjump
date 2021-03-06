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
implements   Map2SVGConverter
{
	public static final String EXTRA_WAIT =
		"de.intevation.printlayout.wait.map.svg";

	/**
	 * If the system property de.intevation.printlayout.no.map.clip
	 * is set to true the generated SVG map is not clipped.
	 */
	public static final String NO_MAP_CLIP =
		"de.intevation.printlayout.no.map.clip";
	
	
  /**
	 * If the system property de.intevation.printlayout.optimize.map.svg
	 * is set to true the generated SVG map is optimized.
	 */
	public static final String OPTIMIZE_MAP_SVG =
		"de.intevation.printlayout.optimize.map.svg";
	
	/**
	 * The Batik DOM implemenmentation is slow.
	 * Rendering and optimizing with the Java on board DOM is much faster.
	 * Set this property if you want the Batik DOM.
	 */
	public static final String USE_BATIK_DOM =
		"de.intevation.printlayout.optimize.map.batik.dom";
	
	/**
	 * If the system property de.intevation.printlayout.use.css.map
	 * is set to true the generated SVG style attributes are expressed as CSS.
	 */
	public static final String USE_CSS =
		"de.intevation.printlayout.use.css.map";
	
	/**
	 * If the system property de.intevation.printlayout.gc.calls
	 * is set to a positive integer N the garbage collection is called
	 * N times after each memory critcal operation. Beware: This may prevent
	 * caching of results. Therefore it is set to 0 by default.
	 */
	public static final String GC_CALLS =
		"de.intevation.printlayout.gc.calls";

	/**
	 * If the system property 'de.intevation.printlayout.simplify.tolerance'
	 * is set to an positive value in mm of paper size this is used
	 * as a tolerance value of simplification in map import. Defaults to 0.
	 */
	public static final String SIMPLIFY_TOLERANCE =
		"de.intevation.printlayout.simplify.tolerance";

	/**
	 * If the system property 'de.intevation.printlayout.use.simplification'
	 * is set to true the map is simplified with the tolerance of
	 * 'de.intevation.printlayout.simplify.tolerance'. Defaults to false.
	 */
	public static final String USE_SIMPLIFICATION =
		"de.intevation.printlayout.use.simplification";

	/**
	 * If the system property 'de.intevation.printlayout.simplify.preserve.topology'
	 * is set to true then polygons are simplified with guaranteed preserving
	 * the topology. This is slow and in most case it should be okay
	 * to simplify the shell and the holes with the faster 
	 * Douglas Peucker line simplifier. Defaults to false.
	 */
	public static final String PRESERVE_TOPOLOGY =
		"de.intevation.printlayout.simplify.preserve.topology";

	/**
	 * Implements a little factory for Map2SVGConverters.
	 * @param pluginContext The binding to the GIS.
	 * @return the Map2SVGConverter to be used.
	 */
	public static Map2SVGConverter createMap2SVGConverter(
		PlugInContext pluginContext
	) {
		return new Map2SVG(pluginContext);
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
			return method;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Is the Viewport#setJava2DConverter(Java2DConverter) call supported?
	 * @return true if call is supported, else false.
	 */
	public static final boolean supportsJava2DConverterSetter() {
		return getJava2DConverterSetter() != null;
	}

	public Element createSVG(
		Document  document, 
		double [] geo2screen,
		double [] screen2paper,
		double [] paperSize,
		Double    tolerance
	) {
		LayerViewPanel layerViewPanel = pluginContext.getLayerViewPanel();

		Viewport vp = layerViewPanel.getViewport();

		Dimension xenv = layerViewPanel.getSize(null);

		if (geo2screen == null)
			geo2screen = new double[1];

		geo2screen[0] = vp.getScale();

		if (paperSize != null) {
			if (screen2paper == null)
				screen2paper = new double[1];
			screen2paper[0] = fitToPaper(xenv, paperSize);
		}

		// setup the SVG generator ...
		Document doc = Options.getInstance().getBoolean(USE_BATIK_DOM)
			? null 
			: createDocument();

		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(
			doc != null ? doc : document);

		ctx.setPrecision(12);

		ctx.setGenericImageHandler(new CachedImageHandlerBase64Encoder());

		ctx.setExtensionHandler(new PatternExt());

		// use CSS?
    CDATASection styleSheetSection;
		if (Options.getInstance().getBoolean(USE_CSS)) {
    	styleSheetSection = (doc != null ? doc : document).createCDATASection(""); 
			ctx.setStyleHandler(new StyleSheetHandler(styleSheetSection));
		}
		else
			styleSheetSection = null;
		
		SVGGraphics2D svgGenerator;

		// clip the map?
		if (Options.getInstance().getBoolean(NO_MAP_CLIP))
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
				Java2DConverter converter;
				
				if (tolerance != null && paperSize != null) {
					double invScale = 1d/(geo2screen[0]*screen2paper[0]);

					double t = tolerance.doubleValue()*invScale*(1d/10d);

					System.err.println("simplify tolerance: " + t);

					boolean preserveTopology = Options.getInstance().getBoolean(PRESERVE_TOPOLOGY);
					converter = new SimplifyingJava2DConverter(vp, t, preserveTopology);
				}
				else
					converter = new PreciseJava2DConverter(vp);

				setJava2DConverter.invoke(vp, new Object[] { converter });
			}
			catch (Exception e) {
				e.printStackTrace();
				oldConverter = null;
			}
		}

		long renderStartTime = System.currentTimeMillis();

		final boolean [] locked = { true };

		rms.renderAll();

		rms.getDefaultRendererThreadQueue().add(new Runnable() {
			public void run() {
				synchronized (locked) {
					locked[0] = false;
					locked.notify();
				}
			}
		});

		try {
			synchronized (locked) {
				int i = 20; // max 2min
				while (locked[0] && i-- > 0)
					locked.wait(6000);
			}
		}
		catch (InterruptedException ie) {
		}

		Integer extraWait = Options.getInstance().getInteger(EXTRA_WAIT);

		if (extraWait != null) 
			try {
				Thread.sleep(Math.max(0, extraWait.intValue())*1000l);
			}
			catch (InterruptedException ie) {
			}

		rms.copyTo(svgGenerator);

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
			Element defs = ElementUtils.getElementById(
				root, SVGSyntax.ID_PREFIX_GENERIC_DEFS);
			Element style = (doc != null ? doc : document).createElementNS(
				SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STYLE_TAG);
			style.setAttributeNS(null, SVGSyntax.SVG_TYPE_ATTRIBUTE, "text/css");
			style.appendChild(styleSheetSection);
			defs.appendChild(style);
			styleSheetSection = null;
		}
		
		gc();

		if (Options.getInstance().getBoolean(OPTIMIZE_MAP_SVG)) {
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

	public static final Double getSimplificationTolerance() {
		Options options = Options.getInstance();
		return options.getBoolean(USE_SIMPLIFICATION)
			? options.getDouble(SIMPLIFY_TOLERANCE)
			: null;
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

		double [] geo2screen   = new double[1];
		double [] screen2paper = new double[1];

		double [] paperSize = documentManager.getPaperSize();

		Element root = createSVG(
			document, 
			geo2screen, 
			screen2paper,
			paperSize,
			Options.getInstance().getDouble(SIMPLIFY_TOLERANCE));

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractElement xform =
			(AbstractElement)document.createElementNS(svgNS, "g");

		xform.setAttributeNS(
			null, "transform", "scale(" + screen2paper[0] + ")");

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
		Integer gcCalls = Options.getInstance().getInteger(GC_CALLS);
		int gc_calls = gcCalls == null ? 0 : gcCalls.intValue();
		if (gc_calls > 0) {
			System.err.println("used memory before gc [MB]: " + inMegaBytes(usedMemory()));
			gc(gc_calls);
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
		Dimension env,
		double [] paperSize
	) {
		double s1 = paperSize[0]/env.getWidth();
		double s2 = paperSize[1]/env.getHeight();

		return Math.min(s1, s2);
	}
}
// end of file
