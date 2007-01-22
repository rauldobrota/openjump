/*
 * LayoutFrame.java
 * ----------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.intevation.printlayout;

import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.ActionMap;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.FlowLayout;

import java.awt.geom.AffineTransform;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.net.URL;

import java.util.List;
import java.util.ArrayList;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractElement;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;

import org.apache.batik.util.XMLResourceDescriptor;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.w3c.dom.svg.SVGDocument;

import org.w3c.dom.DOMImplementation;

import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;

import org.apache.batik.svggen.SVGGraphics2D;


import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager; 

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.renderer.LayerRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;    

import com.vividsolutions.jts.geom.Envelope;

import de.intevation.printlayout.tools.BoxInteractor;
import de.intevation.printlayout.tools.PickingInteractor;
import de.intevation.printlayout.tools.BoxFactory;
import de.intevation.printlayout.tools.DrawingAttributes;
import de.intevation.printlayout.tools.Tool;

public class LayoutFrame 
extends      JFrame
implements   PickingInteractor.PickingListener
{
	public static final String A4_SHEET = "resources/a4.svg";
	
  protected DocumentManager    docManager;

  protected LayoutCanvas       svgCanvas;

	protected PlugInContext      pluginContext;

	protected ArrayList          tools;

	protected RemoveAction       removeAction;
	protected GroupAction        groupAction;
	protected UngroupAction      ungroupAction;

	protected PickingInteractor  pickingInteractor;

	protected File               lastDirectory;

	public LayoutFrame() {
	}

	public LayoutFrame(PlugInContext pluginContext) {
		super("Print/Layout");
		this.pluginContext = pluginContext;
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(createComponents());
	}

	public JComponent createComponents() {
		JPanel panel = new JPanel(new BorderLayout());

		svgCanvas = new LayoutCanvas(
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
			}
		});

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

		JMenu fileMenu   = createMenu("LayoutFrame.File", "File");
		JMenu editMenu   = createMenu("LayoutFrame.Edit", "Edit");
		JMenu insertMenu = createMenu("LayoutFrame.Insert", "Insert");

		ExportSVGAction   svgExportAction   = new ExportSVGAction();
		PDFAction         pdfAction         = new PDFAction();
		PrintAction       printAction       = new PrintAction();
		QuitAction        quitAction        = new QuitAction();

		                  removeAction      = new RemoveAction();
											groupAction       = new GroupAction();
											ungroupAction     = new UngroupAction();

		ImportImageAction imageImportAction = new ImportImageAction();
		ImportSVGAction   svgImportAction   = new ImportSVGAction();
		AddMapAction      addMapAction      = new AddMapAction();

		fileMenu.add(svgExportAction);
		fileMenu.add(pdfAction);
		fileMenu.add(printAction);
		fileMenu.addSeparator();
		fileMenu.add(quitAction);

		editMenu.add(removeAction);
		editMenu.add(groupAction);
		editMenu.add(ungroupAction);

		removeAction  .setEnabled(false);
		groupAction   .setEnabled(false);
		ungroupAction .setEnabled(false);

		insertMenu.add(addMapAction);
		insertMenu.add(svgImportAction);
		insertMenu.add(imageImportAction);

		JMenuBar menubar = new JMenuBar();

		menubar.add(fileMenu);
		menubar.add(editMenu);
		menubar.add(insertMenu);

		setJMenuBar(menubar);

		panel.add(scroller, BorderLayout.CENTER);

		createTools(panel);

		return panel;
	}
	
	protected static JMenu createMenu(String key, String def) {
		String label = I18N.getString(key, def);
		
		JMenu menu = new JMenu(I18N.getName(label));
		menu.setMnemonic(I18N.getMnemonic(label));

		return menu;
	}

	protected void createTools(JPanel panel) {

		DrawingAttributes attributes = new DrawingAttributes();

		attributes.setStrokeColor(Color.black);
		attributes.setFillColor(null);
		attributes.setStroke(new BasicStroke());
		/*
			new BasicStroke(
				5.0f, 
				BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER,
				1.0f,
				new float[]{ 3f, 3f, 3f, 3f},
				0.0f));
		*/

		BoxFactory boxFactory = new BoxFactory();
		boxFactory.setDrawingAttributes(attributes);

		BoxInteractor     boxInteractor     = new BoxInteractor();
		                  pickingInteractor = new PickingInteractor();

		pickingInteractor.addPickingListener(this);

		boxInteractor.setFactory(boxFactory);
		boxInteractor.setDocumentManager(docManager);
		pickingInteractor.setDocumentManager(docManager);

		addTool(boxInteractor);
		addTool(pickingInteractor);

		JToolBar toolBar = new JToolBar();

		toolBar.setRollover(true);
		toolBar.setFloatable(false);

		JToggleButton boxBnt  = new JToggleButton(
			I18N.getString("LayoutFrame.Draw", "draw"));
		JToggleButton pickBtn = new JToggleButton(
			I18N.getString("LayoutFrame.Select", "select"));
		JToggleButton nopBnt  = new JToggleButton(
			I18N.getString("LayoutFrame.Pan", "pan"));

		boxBnt .setActionCommand(boxInteractor.getToolIdentifier());
		nopBnt .setActionCommand("pan");
		pickBtn.setActionCommand(pickingInteractor.getToolIdentifier());


		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				activateTool(ae.getActionCommand());
			}
		};

		boxBnt .addActionListener(listener);
		nopBnt .addActionListener(listener);
		pickBtn.addActionListener(listener);

		ButtonGroup group = new ButtonGroup();

		group.add(nopBnt);
		group.add(pickBtn);
		group.add(boxBnt);

		nopBnt.setSelected(true);

		toolBar.add(boxBnt);
		toolBar.add(pickBtn);
		toolBar.add(nopBnt);

		ActionMap actionMap = svgCanvas.getActionMap();

		Action fullExtendAction =
			actionMap.get(LayoutCanvas.RESET_TRANSFORM_ACTION);

		Action zoomInAction =
			actionMap.get(LayoutCanvas.ZOOM_IN_ACTION);

		Action zoomOutAction =
			actionMap.get(LayoutCanvas.ZOOM_OUT_ACTION);

		// TODO: store icon here
		fullExtendAction.putValue(Action.NAME, "1:1");
		zoomInAction.putValue(Action.NAME, "+");
		zoomOutAction.putValue(Action.NAME, "-");

		JButton fullExtend = new JButton(fullExtendAction);
		JButton zoomIn     = new JButton(zoomInAction);
		JButton zoomOut    = new JButton(zoomOutAction);

		toolBar.addSeparator();
		toolBar.add(fullExtend);
		toolBar.add(zoomIn);
		toolBar.add(zoomOut);

		JPanel north = new JPanel(
			new FlowLayout(FlowLayout.LEADING));

		north.add(toolBar);

		panel.add(north, BorderLayout.NORTH);
	}

	public void addTool(Tool tool) {
		if (tools == null)
			tools = new ArrayList();

		tools.add(tool);

		List overlays = svgCanvas.getOverlays();
		overlays.add(0, tool);

		List interactors = svgCanvas.getInteractors();
		interactors.add(0, tool);
	}

	public void activateTool(String identifier) {
		if (tools == null || identifier == null)
			return;
		for (int i = tools.size()-1; i >= 0; --i) {
			Tool tool = (Tool)tools.get(i);
			tool.setInUse(identifier.equals(tool.getToolIdentifier()));
		}
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

		double s1 = paper[0]/env.getWidth();
		double s2 = paper[1]/env.getHeight();

		double s = Math.min(s1, s2);

		AffineTransform result = AffineTransform.getScaleInstance(s, s);

		return result;
	}

	protected void exportPDF() {
		JFileChooser fc = new JFileChooser(lastDirectory);

		int result = fc.showSaveDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		docManager.exportPDF(file);
	}


	protected void exportSVG() {
		JFileChooser fc = new JFileChooser(lastDirectory);

		int result = fc.showSaveDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
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

		JFileChooser fc = new JFileChooser(lastDirectory);

		int result = fc.showOpenDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
			return;

		docManager.appendSVG(fc.getSelectedFile());
	}

	protected void importImage() {

		JFileChooser fc = new JFileChooser(lastDirectory);

		int result = fc.showOpenDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
			return;

		docManager.appendImage(fc.getSelectedFile());
	}

	protected void removeSelected() {
		String [] ids = pickingInteractor.getSelectedIDs();
		if (ids != null) {
			pickingInteractor.clearSelection();
			docManager.removeIDs(ids);
		}
	}

	protected void group() {
		String [] ids = pickingInteractor.getSelectedIDs();
		if (ids != null) {
			pickingInteractor.clearSelection();
			docManager.groupIDs(ids);
		}
	}

	protected void ungroup() {
		String [] ids = pickingInteractor.getSelectedIDs();
		if (ids != null) {
			pickingInteractor.clearSelection();
			docManager.ungroupIDs(ids);
		}
	}

	/** PickingInteractor.PickingListener */
	public void selectionChanged(PickingInteractor.PickingEvent evt) {
		PickingInteractor pi = (PickingInteractor)evt.getSource();
		int N = pi.numSelections();

		if (removeAction != null)
			removeAction.setEnabled(N > 0);

		if (groupAction != null)
			groupAction.setEnabled(N > 1);

		if (ungroupAction != null)
			ungroupAction.setEnabled(N > 0);
	}

	private class PrintAction extends AbstractAction {
		PrintAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.Print", "Print...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.Print", "Print...")));
		}
		public void actionPerformed(ActionEvent ae) {
			print();
		}
	}

	private class PDFAction extends AbstractAction {
		PDFAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ExportPDF", "Export PDF...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ExportPDF", "Export PDF...")));	
		}
		public void actionPerformed(ActionEvent ae) {
			exportPDF();
		}
	}

	private class ImportImageAction extends AbstractAction {
		ImportImageAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ImportImage", "Import Image...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ImportImage", "Import Image...")));
		}

		public void actionPerformed(ActionEvent ae) {
			importImage();
		}
	}

	private class ImportSVGAction extends AbstractAction {
		ImportSVGAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ImportSVG", "Import SVG...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ImportSVG", "Import SVG...")));
		}

		public void actionPerformed(ActionEvent ae) {
			importSVG();
		}
	}

	private class ExportSVGAction extends AbstractAction {
		ExportSVGAction() {
			super(I18N.getName(
				I18N.getString("LayoutFrame.ExportSVG", "Export SVG...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
				I18N.getString("LayoutFrame.ExportSVG", "Export SVG...")));
		}
		public void actionPerformed(ActionEvent ae) {
			exportSVG();
		}
	}

	private class AddMapAction extends AbstractAction {
		AddMapAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.AddMap", "Add Map")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.AddMap", "Add Map")));
		}
		public void actionPerformed(ActionEvent ae) {
			addMap();
		}
	}

	private class QuitAction extends AbstractAction {
		QuitAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.Close", "Close")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.Close", "Close")));
		}
		public void actionPerformed(ActionEvent ae) {
			LayoutFrame.this.dispose();
		}
	}

	private class RemoveAction extends AbstractAction {
		RemoveAction() {
			super(I18N.getName(
				I18N.getString("LayoutFrame.Remove", "Remove")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
				I18N.getString("LayoutFrame.Remove", "Remove")));
		}
		public void actionPerformed(ActionEvent ae) {
			removeSelected();
		}
	}

	private class GroupAction extends AbstractAction {
		GroupAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.Group", "Group")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.Group", "Group")));
		}
		public void actionPerformed(ActionEvent ae) {
			group();
		}
	}

	private class UngroupAction extends AbstractAction {
		UngroupAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.Ungroup", "Ungroup")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.Ungroup", "Ungroup")));
		}
		public void actionPerformed(ActionEvent ae) {
			ungroup();
		}
	}
}
// end of file
