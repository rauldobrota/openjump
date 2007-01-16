package de.intevation.printlayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.RepaintManager;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.AbstractAction;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Shape;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.net.URL;

import java.util.List;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.AbstractDocumentFragment;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;

import org.apache.batik.util.XMLResourceDescriptor;

import org.apache.batik.bridge.UpdateManager;

import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGMatrix;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;

import org.apache.batik.swing.gvt.AbstractZoomInteractor;
import org.apache.batik.swing.gvt.Overlay;

import org.apache.batik.bridge.UserAgent;

import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;

import org.apache.batik.dom.svg.SVGGraphicsElement;

import org.apache.batik.svggen.SVGGraphics2D;

import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;


// import scenegraph.boxtool.BoxInteractor;
// import scenegraph.boxtool.BoxFactory;
//
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager; 

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.renderer.LayerRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;    

import com.vividsolutions.jts.geom.Envelope;

public class LayoutFrame 
extends      JFrame
//implements BoxFactory.Consumer
{
	public static final String A4_SHEET = "resources/a4.svg";

	public static final int MODE_ZOOM = 0;

  protected DocumentManager docManager;

	protected PlugInContext   pluginContext;

	// protected int transformId;

	//protected SVGGraphicsElement selectedElement;

	/*
	 protected BoxInteractor boxInteractor = new BoxInteractor();
	
	{
		boxInteractor.setStrokeColor(Color.YELLOW);
		boxInteractor.setStroke(new BasicStroke(
						5.0f, 
						BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_MITER,
						1.0f,
						new float[]{ 3f, 3f, 3f, 3f},
						0.0f));
		boxInteractor.setFillColor(Color.BLUE);
	}

	*/
	
	public LayoutFrame() {
	}

	public LayoutFrame(PlugInContext pluginContext) {
		super("Print/Layout plugin");
		this.pluginContext = pluginContext;
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(createComponents());
	}

	/*
	public static class MyJSVGCanvas
	extends             JSVGCanvas
	{
		public MyJSVGCanvas(
			SVGUserAgent agent,  
			boolean      eventsEnabled, 
			boolean      selectableText
		) {
			super(agent, eventsEnabled, selectableText);
		}
		
		public MyJSVGCanvas() {
		}

		public UserAgent getUserAgent() {
			return userAgent;
		}

		public void installDocument(SVGDocument document) {
			installSVGDocument(document);
		}
	} // class MyJSVGCanvas
	*/

	/*
	public static final AffineTransform toJavaTransform(SVGMatrix matrix) {
		return new AffineTransform(
			matrix.getA(),
			matrix.getB(),
			matrix.getC(),
			matrix.getD(),
			matrix.getE(),
			matrix.getF());
	}
	*/

	/*
	private class OnMouseClick implements EventListener {
		public void handleEvent(Event evt) {
			//selectedElement = (SVGGraphicsElement)evt.getTarget();
			selectedElement = (SVGGraphicsElement)evt.getCurrentTarget();
			SVGRect bbox = selectedElement.getBBox();
			evt.stopPropagation();
			svgCanvas.repaint();
		}
	} // class OnMouseClick

	*/

	/*

	protected class MyOverlay implements Overlay
	{
		public void paint(Graphics g) {
			if (selectedElement != null) {
				Graphics2D g2d = (Graphics2D)g;
				SVGMatrix matrix = selectedElement.getScreenCTM();

				SVGMatrix ctm = selectedElement.getCTM();

				DecomposeTransform dt =
					new DecomposeTransform(
						ctm.getA(),
						ctm.getB(),
						ctm.getC(),
						ctm.getD(),
						ctm.getE(),
						ctm.getF());

				System.err.println(dt);

				SVGRect bbox = selectedElement.getBBox();

				System.err.println(bbox);
				System.err.println(matrix);
				AffineTransform xform = toJavaTransform(matrix);

				Rectangle2D rect = new Rectangle2D.Float(
					(float)bbox.getX(), 
					(float)bbox.getY(),
					(float)bbox.getWidth(),
					(float)bbox.getHeight());

				System.err.println(rect);

				GeneralPath path = new GeneralPath(rect);

				Shape selected = path.createTransformedShape(xform);

				g2d.draw(selected);
			}
		}
	}
	*/

	public JComponent createComponents() {
		JPanel panel = new JPanel(new BorderLayout());

		LayoutCanvas svgCanvas = new LayoutCanvas(
			new SVGUserAgentGUIAdapter(panel), true, true);

		docManager = new DocumentManager(svgCanvas);

		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

		svgCanvas.setBackground(Color.gray);

		svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
			public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
				System.err.println("started");
			}

			public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
				System.err.println("completed");
				SVGDocument document = e.getSVGDocument();
				AbstractElement svgRoot = (AbstractElement)document.getRootElement();
				
				//boxInteractor.setDocument(svgCanvas.getSVGDocument());
				/*
				EventTarget target = (EventTarget)svgRoot;
				target.addEventListener("click", new OnMouseClick(), false);
				*/
			}
		});

		/*
		BoxFactory factory = new BoxFactory(this);
		boxInteractor.setConsumer(factory);

		List overlays = svgCanvas.getOverlays();
		//overlays.add(new MyOverlay());
		overlays.add(boxInteractor.getOverlay());

		List interactors = svgCanvas.getInteractors();
		interactors.add(boxInteractor);
		*/
		

		/*
		SVGDocument doc = createSheet("DIN A4");

		if (doc == null) {
		*/
			System.err.println("cannot create DIN A4");
			URL url = getClass().getResource(A4_SHEET);
			if (url == null) {
				System.err.println("sheet not found");
				return null;
			}
			svgCanvas.setURI(url.toString());
		/*
		}
		else {
			svgCanvas.installDocument(doc);
		}
		*/

		JSVGScrollPane scroller = new JSVGScrollPane(svgCanvas);

		JMenu fileMenu = new JMenu("File");
		JMenu editMenu = new JMenu("Edit");

		PrintAction     printAction     = new PrintAction();
		PDFAction       pdfAction       = new PDFAction();
		ImportSVGAction svgImportAction = new ImportSVGAction();
		ExportSVGAction svgExportAction = new ExportSVGAction();
		AddMapAction    addMapAction    = new AddMapAction();

		fileMenu.add(svgImportAction);
		fileMenu.add(svgExportAction);
		fileMenu.add(pdfAction);
		fileMenu.add(printAction);

		editMenu.add(addMapAction);

		JMenuBar menubar = new JMenuBar();
		menubar.add(fileMenu);
		menubar.add(editMenu);

		setJMenuBar(menubar);

		panel.add(scroller, BorderLayout.CENTER);
		return panel;
	}

	public void print() {
		docManager.print();
	}

	public void addMap() {

		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractDocument document =
			(AbstractDocument)impl.createDocument(svgNS, "svg", null);

		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		LayerViewPanel lvp = pluginContext.getLayerViewPanel();

		Envelope env =  lvp.getViewport().getEnvelopeInModelCoordinates();

		System.err.println(env);

		RenderingManager rms = lvp.getRenderingManager();

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
		}

		//java.awt.Rectangle rect = lvp.getVisibleRect();
		//svgGenerator.setClip(rect);

		lvp.repaint();
		lvp.paintComponent(svgGenerator);

		for (int i = 0; i < N; ++i) {
			Layer    layer    = (Layer)layers.get(i);		
			Renderer renderer = rms.getRenderer(layer);
			if (renderer instanceof LayerRenderer) {
				LayerRenderer layerRenderer = (LayerRenderer)renderer;
				layerRenderer.setMaxFeatures(oldMaxFeaures[i]);
			}
		}

		//svgGenerator.dispose();

		Envelope xenv = new Envelope(
			0, lvp.getWidth(), 0, lvp.getHeight());

		AffineTransform xform = fitToPaper(xenv);

		/*
		try {
			java.io.FileOutputStream f = new java.io.FileOutputStream("raw.svg");
			java.io.Writer out = new java.io.OutputStreamWriter(f, "UTF-8");
    	svgGenerator.stream(out, true);
			f.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		*/

		AbstractElement root = (AbstractElement)document.getDocumentElement();
		root = (AbstractElement)svgGenerator.getRoot(root);

		root.setAttributeNS(null, "width", String.valueOf(xenv.getWidth()));
		root.setAttributeNS(null, "height", String.valueOf(xenv.getHeight()));

		root.setAttributeNS(null, "x", "0");
		root.setAttributeNS(null, "y", "0");

		docManager.appendSVG((AbstractDocument)document, xform, false);
	}

	protected AffineTransform fitToPaper(Envelope env) {
		double [] paper = new double[2];
		docManager.getPaperSize(paper);

		/*

		double maxEnvExt   = Math.max(env.getWidth(), env.getHeight());
		double maxPaperExt = Math.max(paper[0], paper[1]);

		AffineTransform trans = AffineTransform.getTranslateInstance(
			-env.getMinX(),
			-env.getMinY());

		// scale * maxEnvExt = maxPaperExt
		//double scale = maxPaperExt/maxEnvExt;
		double scale = maxEnvExt/maxPaperExt;

		AffineTransform scaleM = AffineTransform.getScaleInstance(scale, scale);

		scaleM.concatenate(trans);
		*/

		double s1 = paper[0]/env.getWidth();
		double s2 = paper[1]/env.getHeight();

		double s = Math.min(s1, s2);

		AffineTransform result = AffineTransform.getScaleInstance(s, s);

		/*
		Point2D org  = new Point2D.Double(env.getMinX(), env.getMinY());
		Point2D dest = new Point2D.Double();

		result.transform(org, dest);
		System.err.println(dest);

		org = new Point2D.Double(env.getMaxX(), env.getMaxY());

		result.transform(org, dest);
		System.err.println(dest);

		*/


		return result;
	}

	protected void exportPDF() {
		JFileChooser fc = new JFileChooser(".");

		if (fc.showSaveDialog(docManager.getCanvas()) 
			!= JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		docManager.exportPDF(file);
	}


	protected void exportSVG() {
		JFileChooser fc = new JFileChooser(".");

		if (fc.showSaveDialog(docManager.getCanvas()) 
			!= JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		docManager.exportSVG(file);
	}

	protected static SVGDocument createSheet(String id) {
		return createSheet(id, false);
	}

	protected static SVGDocument createSheet(String id, boolean landscape) {
		String text = PaperSizes.sheetForPaperSize(id, landscape);
		if (text == null)
			return null;

		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

		try {
			return (SVGDocument)factory.createDocument(
				null,
				new StringReader(text));
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}

	protected void importSVG() {

		JFileChooser fc = new JFileChooser(".");

		if (fc.showOpenDialog(docManager.getCanvas()) 
			!= JFileChooser.APPROVE_OPTION)
			return;


		docManager.appendSVG(fc.getSelectedFile());

		/*

		File file = fc.getSelectedFile();

		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

		try {
			String uri = file.toURL().toString();

			final AbstractDocument document = 
				(AbstractDocument)factory.createDocument(uri);

			modify(new Runnable() {
				public void run() { appendDocument(document); }
			});
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		*/
	}

	private class PrintAction extends AbstractAction {
		PrintAction() {
			super("print...");
		}
		public void actionPerformed(ActionEvent ae) {
			print();
		}
	}

	private class PDFAction extends AbstractAction {
		PDFAction() {
			super("export PDF...");
		}
		public void actionPerformed(ActionEvent ae) {
			exportPDF();
		}
	}

	private class ImportSVGAction extends AbstractAction {
		ImportSVGAction() {
			super("import SVG...");
		}

		public void actionPerformed(ActionEvent ae) {
			importSVG();
		}
	}

	private class ExportSVGAction extends AbstractAction {
		ExportSVGAction() {
			super("export SVG...");
		}
		public void actionPerformed(ActionEvent ae) {
			exportSVG();
		}
	}

	private class AddMapAction extends AbstractAction {
		AddMapAction() {
			super("add map");
		}
		public void actionPerformed(ActionEvent ae) {
			addMap();
		}
	}

}
// end of file
