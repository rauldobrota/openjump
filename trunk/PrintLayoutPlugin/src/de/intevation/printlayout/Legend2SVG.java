/*
 * Legend2SVG.java
 * ---------------
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

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import java.util.List;
import java.util.Iterator;
import java.util.Map;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.batik.svggen.CachedImageHandlerBase64Encoder;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;

import org.w3c.dom.svg.SVGDocument;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Font;

import de.intevation.printlayout.batik.PatternExt;

import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;

import java.awt.geom.Rectangle2D;

import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;

/**
 * Instances of this class are used to convert the
 * create a map legend.
 * It's implemented as a DocumentModifier to run
 * in the UpdateManager of the DocumentManager.
 */
public class Legend2SVG
implements   DocumentManager.DocumentModifier
{
	/**
	 * Font used for rendering sub themes
	 */
	public static final Font FONT_PLAIN =
		new Font("Dialog", Font.PLAIN, 10);

	/**
	 * Font used for rendering top level themes
	 */
	public static final Font FONT_BOLD =
		new Font("Dialog", Font.BOLD, 10);

	/**
	 * Gap between symbol an layer name
	 */
	public static final int X_GAP = 3;

	/**
	 * Gap between two layer entries
	 */
	public static final int Y_GAP = 3;

	/**
	 * Size of the symbol
	 */
	public static final int SYMBOL_SIZE = 10;

	/**
	 * initial scale of legend relativly to paper size
	 */
	public static final double PERCENT_PAPER = 1d/3d;

	/**
	 * The plugin context is need to access the LayerViewPanel.
	 */
	protected PlugInContext pluginContext;

	/**
	 * Creates uninitialized Legend2SVG
	 */
	protected Legend2SVG() {
	}

	/**
	 * Creates a Legend2SVG wired to the plugin context.
	 * @param pluginContext the context to access the LayerViewPanel
	 */
	public Legend2SVG(PlugInContext pluginContext) {
		this.pluginContext = pluginContext;
	}

	private static final Color addAlpha(Color c, int alpha) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	private static Rectangle2D drawThemedRect(
		Graphics2D g2d,
		BasicStyle basicStyle,
		ColorThemingStyle themeStyles,
		double x, double y,
		double width, double height
	) {
		Map themes = themeStyles.getAttributeValueToBasicStyleMap();
		double w2 = width*0.5d;
		double h2 = height*0.5d;
		int c = 0;
		for (Iterator i = themes.values().iterator(); i.hasNext() && c < 4; ++c) {
			int iy = c >> 1;
			int ix = c &  1;
			drawRect(
				g2d,
				c == 0 ? basicStyle : (BasicStyle)i.next(),
				x + ix*w2, y + iy*h2,
				w2, h2);
		}
		return new Rectangle2D.Double(
			x, y,
			width, height);
	}

	private static final Stroke ensure(Stroke stroke) {
		return stroke != null ? stroke : new BasicStroke();
	}


	private static Rectangle2D drawRect(
		Graphics2D g2d,
		BasicStyle basicStyle,
		double x,     double y,
		double width, double height
	) {
		int alpha = basicStyle.getAlpha();

		Rectangle2D rect = new Rectangle2D.Double(
			x, y,
			width, height);

		if (basicStyle.isRenderingFill()) {
			if (basicStyle.isRenderingFillPattern()) 
				g2d.setPaint(basicStyle.getFillPattern());
			else
				g2d.setPaint(addAlpha(basicStyle.getFillColor(), alpha));
			g2d.fill(rect);
		}

		if (basicStyle.isRenderingLine()) {
			g2d.setPaint(addAlpha(basicStyle.getLineColor(), alpha));
			g2d.setStroke(ensure(basicStyle.getLineStroke()));
			g2d.draw(rect);
		}
		return rect;
	}

	private static final class Offset {
		Rectangle2D bbox;
		float       y;

		void enlarge(Rectangle2D r1) {
			if (bbox == null) {
				bbox = new Rectangle2D.Double();
				bbox.setRect(r1);
			}
			else
				bbox.add(r1);
		}

		void enlarge(int extra) {
			if (bbox != null)
				bbox.setRect(
					bbox.getX() - extra,
					bbox.getY() - extra,
					bbox.getWidth() + (extra << 1),
					bbox.getHeight() + (extra << 1));
		}

		private static final Rectangle2D translate(
			Rectangle2D r, 
			double     dx,
			double     dy
		) {
			r.setRect(
				r.getX() + dx,
				r.getY() + dy,
				r.getWidth(),
				r.getHeight());
			return r;
		}

		void drawText(
			Graphics2D g2d,
			TextLayout tl,
			float      px
		) {
			float py = y + SYMBOL_SIZE;
			g2d.setPaint(Color.black);
			tl.draw(g2d, px, py);
			Rectangle2D bounds = translate(tl.getBounds(), px, py);
			enlarge(bounds);
			y = (float)(bounds.getY() + bounds.getHeight() + Y_GAP);
		}

		void nextRow() {
			y += Y_GAP + SYMBOL_SIZE;
		}
	} // class Offset

	private static final String trim(String s) {
		return s == null || (s = s.trim()).length() == 0
			? null
			: s;
	}

	private static Rectangle2D drawLegend(Graphics2D g2d, List layers) {

    FontRenderContext frc = g2d.getFontRenderContext();

		Offset ofs = new Offset();
		
		for (int N = layers.size(), i = 0; i < N; ++i) {
			Layer layer = (Layer)layers.get(i);		

			ColorThemingStyle themeStyle =
				(ColorThemingStyle)layer.getStyle(ColorThemingStyle.class);

			BasicStyle basicStyle = layer.getBasicStyle();

			if (themeStyle.isEnabled()) { // layer with extra styles

				ofs.enlarge(drawThemedRect(
					g2d, 
					basicStyle, 
					themeStyle,
					0d, ofs.y, 
					SYMBOL_SIZE, SYMBOL_SIZE));

				String name = trim(layer.getName());

				if (name == null)
					ofs.nextRow();
				else
					ofs.drawText(
						g2d,
						new TextLayout(name, FONT_BOLD, frc),
						SYMBOL_SIZE + X_GAP);

				Map themes = themeStyle.getAttributeValueToBasicStyleMap();

				for (Iterator t = themes.entrySet().iterator(); t.hasNext();) {

					Map.Entry entry = (Map.Entry)t.next();

					ofs.enlarge(drawRect(
						g2d,
						(BasicStyle)entry.getValue(), 
						2*X_GAP, ofs.y,
						SYMBOL_SIZE, SYMBOL_SIZE));

					name = trim(entry.getKey() != null 
						? entry.getKey().toString()
						: null);

					if (name == null)
						ofs.nextRow();
					else 
						ofs.drawText(
							g2d,
							new TextLayout(name, FONT_PLAIN, frc),
							3*X_GAP + SYMBOL_SIZE);
				} // for all sub styles
			}
			else { // Layer with no extra styles
				ofs.enlarge(drawRect(
					g2d,
					basicStyle, 
					0, ofs.y,
					SYMBOL_SIZE, SYMBOL_SIZE));

				String name = trim(layer.getName());

				if (name == null)
					ofs.nextRow();
				else
					ofs.drawText(
						g2d,
						new TextLayout(name, FONT_BOLD, frc),
            SYMBOL_SIZE + X_GAP);
			}
		}
		ofs.enlarge(3);
		return ofs.bbox;
	}

	/**
	 * implements the run() method of the DocumentModifier 
	 * interface. The legend is rendered to an SVG context
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

		// setup the SVG generator ...

		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
		ctx.setPrecision(12);

		ctx.setGenericImageHandler(new CachedImageHandlerBase64Encoder());

		ctx.setExtensionHandler(new PatternExt());

		SVGGraphics2D svgGenerator = new SVGGraphics2D(ctx, false);

		List layers = pluginContext.getLayerManager().getVisibleLayers(false);

		Rectangle2D bbox = drawLegend(svgGenerator, layers);

		if (bbox == null)
			return null;

		// add the new SVG node to the DOM tree

		AbstractElement root = (AbstractElement)svgGenerator.getRoot();
		svgGenerator.dispose();

		root.setAttributeNS(null, "viewBox",
			String.valueOf(bbox.getX()) + " " + String.valueOf(bbox.getY()) + " " +
			String.valueOf(bbox.getWidth()) + " " + String.valueOf(bbox.getHeight()));
			
		root.setAttributeNS(null, "width",  String.valueOf(bbox.getWidth()));
		root.setAttributeNS(null, "height", String.valueOf(bbox.getHeight()));

		root.setAttributeNS(null, "x", "0");
		root.setAttributeNS(null, "y", "0");

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractElement xform =
			(AbstractElement)document.createElementNS(svgNS, "g");

		double scale2paper = fitToPaper(documentManager, bbox, PERCENT_PAPER);

		xform.setAttributeNS(
			null, "transform", "scale(" + scale2paper + ")");

		String id = documentManager.uniqueObjectID();

		xform.setAttributeNS(null, "id", id);

		xform.appendChild(root);

		sheet.appendChild(xform);

		return null;
	}

	private static double fitToPaper(
		DocumentManager documentManager,
		Rectangle2D     env,
		double          scale
	) {
		double [] paper = new double[2];
		documentManager.getPaperSize(paper);

		double s1 = (scale*paper[0])/env.getWidth();
		double s2 = (scale*paper[1])/env.getHeight();

		return Math.min(s1, s2);
	}
}
// end of file
