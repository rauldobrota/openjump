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
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.ImageIcon;

import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.FlowLayout;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;

import java.io.File;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferenceChangeEvent;

import java.text.NumberFormat;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;

import org.apache.batik.swing.gvt.Overlay;

import org.apache.batik.dom.AbstractElement;

import org.apache.batik.transcoder.Transcoder;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;

import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;

import de.intevation.printlayout.tools.PanInteractor;
import de.intevation.printlayout.tools.BoxInteractor;
import de.intevation.printlayout.tools.PickingInteractor;
import de.intevation.printlayout.tools.TextInteractor;
import de.intevation.printlayout.tools.BoxFactory;
import de.intevation.printlayout.tools.TextConsumer;
import de.intevation.printlayout.tools.TextDialog;
import de.intevation.printlayout.tools.DrawingAttributes;
import de.intevation.printlayout.tools.BoxPropertiesDialog;
import de.intevation.printlayout.tools.Tool;
import de.intevation.printlayout.tools.RulerOverlay;
import de.intevation.printlayout.tools.PrintBorderOverlay;

import de.intevation.printlayout.util.FileFilter;
import de.intevation.printlayout.util.PaperSizes;
import de.intevation.printlayout.util.MatrixTools;
import de.intevation.printlayout.util.ElementUtils;
import de.intevation.printlayout.util.Template;

import de.intevation.printlayout.beans.MapData;
import de.intevation.printlayout.beans.ScaleUpdater;
import de.intevation.printlayout.beans.ScaleBarUpdater;

import de.intevation.printlayout.ui.JPEGParameterDialog;
import de.intevation.printlayout.ui.OptionDialog;
import de.intevation.printlayout.ui.InfoDialog;

import de.intevation.printlayout.jump.SimplifyingJava2DConverter;

/**
 * Top level window containing the tool bar and the sheet panel.
 */
public class LayoutFrame 
extends      JFrame
implements   PickingInteractor.PickingListener
{
	/**
	 * zoom in icon resource
	 */
	public static final String ZOOM_IN_ICON =
		"resources/viewmag+.png";

	/**
	 * zoom out icon resource
	 */
	public static final String ZOOM_OUT_ICON =
		"resources/viewmag-.png";

	/**
	 * print border options
	 */
	protected static final String LEFT_BORDER =
		"de.intevation.printlayout.LeftBorder";
	protected static final String BOTTOM_BORDER = 
		"de.intevation.printlayout.BottomBorder";
	protected static final String RIGHT_BORDER =
		"de.intevation.printlayout.RightBorder";
	protected static final String TOP_BORDER =
		"de.intevation.printlayout.TopBorder";
	
	/**
	 * zoom 1:1 icon resource
	 */
	public static final String ZOOM_100_ICON =
		"resources/viewmag1.png";

  protected DocumentManager     docManager;

	protected PlugInContext       pluginContext;

	protected ArrayList           tools;

	protected RemoveAction        removeAction;
	protected GroupAction         groupAction;
	protected UngroupAction       ungroupAction;
	protected UpAction            upAction;
	protected DownAction          downAction;

	protected	AddScalebarAction   addScalebarAction;
	protected	AddScaletextAction  addScaletextAction;
	protected	BoxPropAction       boxPropAction;
	protected TextPropAction      textPropAction;
                   
	protected JCheckBoxMenuItem   printBorderMenuItem;
	
	protected PickingInteractor   pickingInteractor;

	protected File                lastDirectory;

	protected JLabel              mousePosition;
	protected NumberFormat        mouseFormat;

	protected RulerOverlay        rulerOverlay;
	protected PrintBorderOverlay printerBorderOverlay;

	public LayoutFrame() {
	}

	public LayoutFrame(PlugInContext pluginContext) {
		super("Print/Layout");

		mouseFormat = NumberFormat.getInstance();
		mouseFormat.setGroupingUsed(false);
		mouseFormat.setMaximumFractionDigits(2);

		this.pluginContext = pluginContext;
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(createComponents());
	}

	protected JComponent createComponents() {
		JPanel panel = new JPanel(new BorderLayout());

		LayoutCanvas svgCanvas = new LayoutCanvas(
			new SVGUserAgentGUIAdapter(panel), true, false);

		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		svgCanvas.setEnablePanInteractor(false);

		docManager = new DocumentManager(svgCanvas);

		rulerOverlay = new RulerOverlay(docManager);
		printerBorderOverlay = new PrintBorderOverlay(docManager);

		svgCanvas.getOverlays().add(0, rulerOverlay);
		svgCanvas.getOverlays().add(1, printerBorderOverlay);

		svgCanvas.setBackground(Color.gray);

		JSVGScrollPane scroller = new JSVGScrollPane(svgCanvas);

		panel.add(scroller, BorderLayout.CENTER);

		JMenu fileMenu   = createMenu("LayoutFrame.File",   "&File");
		JMenu editMenu   = createMenu("LayoutFrame.Edit",   "&Edit");
		JMenu viewMenu   = createMenu("LayoutFrame.View",   "&View");
		JMenu insertMenu = createMenu("LayoutFrame.Insert", "&Insert");
		JMenu infoMenu   = createMenu("LayoutFrame.Help",   "&Help");

		ExportSVGAction    svgExportAction    = new ExportSVGAction();
		PDFAction          pdfAction          = new PDFAction();
		JPGAction          jpgAction          = new JPGAction();
		PrintAction        printAction        = new PrintAction();
		SaveSessionAction  saveSessionAction  = new SaveSessionAction();
		LoadSessionAction  loadSessionAction  = new LoadSessionAction();
		QuitAction         quitAction         = new QuitAction();

		                   removeAction       = new RemoveAction();
										   groupAction        = new GroupAction();
										   ungroupAction      = new UngroupAction();
											 upAction           = new UpAction();
											 downAction         = new DownAction();
		OptionDialogAction optionDialogAction = new OptionDialogAction();

		RulerAction        rulerAction        = new RulerAction();
		PrintBorderAction  printBorderAction  = new PrintBorderAction();

		ImportImageAction  imageImportAction  = new ImportImageAction();
		ImportSVGAction    svgImportAction    = new ImportSVGAction();
		AddMapAction       addMapAction       = new AddMapAction();
		AddPreviewAction   addPreviewAction   = new AddPreviewAction();
		AddLegendAction    addLegendAction    = new AddLegendAction();
		                   addScalebarAction  = new AddScalebarAction();
		                   addScaletextAction = new AddScaletextAction();
		AboutDialogAction  aboutDialogAction  = new AboutDialogAction();
		InfoDialogAction   infoDialogAction   = new InfoDialogAction();

		fileMenu.add(loadSessionAction);
		fileMenu.add(saveSessionAction);
		fileMenu.addSeparator();
		fileMenu.add(svgExportAction);
		fileMenu.add(jpgAction);
		fileMenu.add(pdfAction);
		fileMenu.add(printAction);
		fileMenu.addSeparator();
		fileMenu.add(createPaperSizeMenu());
		fileMenu.addSeparator();
		fileMenu.add(quitAction);

		editMenu.add(removeAction);
		editMenu.addSeparator();
		editMenu.add(upAction);
		editMenu.add(downAction);
		editMenu.addSeparator();
		editMenu.add(groupAction);
		editMenu.add(ungroupAction);
		editMenu.addSeparator();
		editMenu.add(optionDialogAction);

		removeAction  .setEnabled(false);
		groupAction   .setEnabled(false);
		ungroupAction .setEnabled(false);
		upAction      .setEnabled(false);
		downAction    .setEnabled(false);

		viewMenu.add(new JCheckBoxMenuItem(rulerAction));
		printBorderMenuItem = new JCheckBoxMenuItem(printBorderAction);
		viewMenu.add(printBorderMenuItem);

		insertMenu.add(addMapAction);
		insertMenu.add(addPreviewAction);
		insertMenu.add(addLegendAction);
		insertMenu.add(svgImportAction);
		insertMenu.add(imageImportAction);
		insertMenu.add(addScalebarAction);
		insertMenu.add(addScaletextAction);

		addScalebarAction .setEnabled(false);
		addScaletextAction.setEnabled(false);
		
		infoMenu.add(infoDialogAction);
		infoMenu.add(aboutDialogAction);

		JMenuBar menubar = new JMenuBar();

		menubar.add(fileMenu);
		menubar.add(editMenu);
		menubar.add(viewMenu);
		menubar.add(insertMenu);
		menubar.add(infoMenu);

		setJMenuBar(menubar);

		createTools(panel);

		JPanel south = new JPanel(new BorderLayout());

		mousePosition = new JLabel("(0; 0)");
		mousePosition.setFont(new Font("Monospaced", Font.PLAIN, 11));

		south.add(mousePosition, BorderLayout.WEST);

		panel.add(south, BorderLayout.SOUTH);

		// track mouse coordinates
		svgCanvas.addMouseMotionListener(
			new MouseMotionAdapter() {
				public void mouseMoved(MouseEvent me) {
					updateMousePositionLabel(me);
				}
		});

		// to update print border
		Options.getInstance().addPreferenceChangeListener(
				new PreferenceChangeListener() {
					public void preferenceChange(PreferenceChangeEvent pce) {
						if (!LayoutFrame.this.printBorderMenuItem.getState())
							return;
						LayoutFrame.this.printerBorderOverlay.setPrintBorder(
							LayoutFrame.this.getBorder());
					}
		});
		
		return panel;
	}

	
	protected static JMenu createMenu(String key, String def) {
		String label = I18N.getString(key, def);

		if (label == null)
			label = key;
		
		JMenu menu = new JMenu(I18N.getName(label));

		Integer mnemonic = I18N.getMnemonic(label);
		if (mnemonic != null)
			menu.setMnemonic(mnemonic.intValue());

		return menu;
	}

	protected JMenu createPaperSizeMenu() {
		JMenu menu = createMenu("LayoutFrame.PaperSize", "&Paper Size");
		for (Iterator i = PaperSizes.knownPaperSizeKeys(); i.hasNext();) {
			String size = (String)i.next();
			SwitchPaperSizeAction action = new
				SwitchPaperSizeAction(size, size);
			menu.add(action);
		}
		return menu;
	}

	protected void createTools(JPanel panel) {

		DrawingAttributes attributes = new DrawingAttributes();

		attributes.setStrokeColor(Color.black);
		attributes.setFillColor(null);
		attributes.setStroke(new BasicStroke());

		BoxFactory boxFactory = new BoxFactory();
		boxFactory.setDrawingAttributes(attributes);

		TextConsumer textConsumer = new TextConsumer();

		BoxInteractor     boxInteractor     = new BoxInteractor();
		                  pickingInteractor = new PickingInteractor();
		PanInteractor     panInteractor     = new PanInteractor();
		TextInteractor    textInteractor    = new TextInteractor();

		pickingInteractor.addPickingListener(this);

		boxInteractor.setFactory(boxFactory);
		textInteractor.setConsumer(textConsumer);
		boxInteractor.setDocumentManager(docManager);
		pickingInteractor.setDocumentManager(docManager);
		textInteractor.setDocumentManager(docManager);

		addTool(panInteractor);
		addTool(boxInteractor);
		addTool(pickingInteractor);
		addTool(textInteractor);

		activateTool(panInteractor.getToolIdentifier());

		JToolBar toolBar = new JToolBar();

		toolBar.setRollover(true);
		toolBar.setFloatable(false);

		JToggleButton boxBnt  = new JToggleButton(
			IconLoader.icon("DrawRectangle.gif"));
		boxBnt.setToolTipText(I18N.getString("LayoutFrame.Draw", "draw"));
		JToggleButton pickBtn = new JToggleButton(
			IconLoader.icon("Select.gif"));
		pickBtn.setToolTipText(I18N.getString("LayoutFrame.Select", "select"));
		JToggleButton nopBnt  = new JToggleButton(
			IconLoader.icon("BigHand.gif"));
		nopBnt.setToolTipText(I18N.getString("LayoutFrame.Pan", "pan"));
		JToggleButton textBnt = new JToggleButton(
			IconLoader.icon("LabelOn.gif"));
		textBnt.setToolTipText(I18N.getString("LayoutFrame.Text", "text"));
		
		
		boxBnt .setActionCommand(boxInteractor.getToolIdentifier());
		nopBnt .setActionCommand(panInteractor.getToolIdentifier());
		pickBtn.setActionCommand(pickingInteractor.getToolIdentifier());
		textBnt.setActionCommand(textInteractor.getToolIdentifier());
		
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				activateTool(ae.getActionCommand());
			}
		};

		boxBnt .addActionListener(listener);
		nopBnt .addActionListener(listener);
		pickBtn.addActionListener(listener);
		textBnt.addActionListener(listener);

		ButtonGroup group = new ButtonGroup();

		group.add(nopBnt);
		group.add(pickBtn);
		group.add(boxBnt);
		group.add(textBnt);

		nopBnt.setSelected(true);

		toolBar.add(nopBnt);
		toolBar.add(pickBtn);
		toolBar.add(boxBnt);
		toolBar.add(textBnt);

		LayoutCanvas svgCanvas = docManager.getCanvas();

		ActionMap actionMap = svgCanvas.getActionMap();

		Action fullExtendAction =
			actionMap.get(LayoutCanvas.RESET_TRANSFORM_ACTION);

		Action zoomInAction =
			actionMap.get(LayoutCanvas.ZOOM_IN_ACTION);

		Action zoomOutAction =
			actionMap.get(LayoutCanvas.ZOOM_OUT_ACTION);

		fullExtendAction.putValue(
			Action.SMALL_ICON,
			new ImageIcon(getClass().getResource(ZOOM_100_ICON)));

		zoomInAction.putValue(
			Action.SMALL_ICON,
			new ImageIcon(getClass().getResource(ZOOM_IN_ICON)));

		zoomOutAction.putValue(
			Action.SMALL_ICON,
			new ImageIcon(getClass().getResource(ZOOM_OUT_ICON)));

		JButton boxProp    = new JButton(
				boxPropAction  = new BoxPropAction(pickingInteractor));
		JButton textProp   = new JButton(
				textPropAction = new TextPropAction(pickingInteractor));
		JButton fullExtend = new JButton(fullExtendAction);
		JButton zoomIn     = new JButton(zoomInAction);
		JButton zoomOut    = new JButton(zoomOutAction);
	
		boxPropAction.setEnabled(false);
		textPropAction.setEnabled(false);
		
		toolBar.addSeparator();
		toolBar.add(boxProp);
		toolBar.add(textProp);
		toolBar.addSeparator();
		toolBar.add(fullExtend);
		toolBar.add(zoomIn);
		toolBar.add(zoomOut);

		JPanel north = new JPanel(
			new FlowLayout(FlowLayout.LEADING));

		north.add(toolBar);

		panel.add(north, BorderLayout.NORTH);
	}

	/**
	 * adds a Tool to this frame.
	 * A specific Tool can have a overlay, which is
	 * added to the overlays of the canvas.
	 * It also has an interactor, which is added to the interactors of
	 * the canvas.
	 *
	 * @param tool  adds this to the frame.
	 */
	public void addTool(Tool tool) {
		if (tools == null)
			tools = new ArrayList();

		tools.add(tool);

		LayoutCanvas svgCanvas = docManager.getCanvas();

		if (tool instanceof Overlay) {
			List overlays = svgCanvas.getOverlays();
			overlays.add(0, tool);
		}

		List interactors = svgCanvas.getInteractors();
		interactors.add(0, tool);
	}

	/**
	 * returns the document manager.
	 * @return DocumentManager
	 */
	public DocumentManager getDocumentManager() {
		return docManager;
	}

	/**
	 * activates tool by an identifier.
	 * Iterates through the tools list and activates 
	 * the tool with this identifier and deactivates the rest.
	 *
	 * @param identifier the identifier of the tool, which should be selected.
	 */
	public void activateTool(String identifier) {
		if (tools == null || identifier == null)
			return;
		for (int i = tools.size()-1; i >= 0; --i) {
			Tool tool = (Tool)tools.get(i);
			tool.setInUse(identifier.equals(tool.getToolIdentifier()));
		}
	}

	/**
	 * sets a point to the mouse position label.
	 * 
	 * @param p the point with x/y coordinates in mm.
	 */
	public void setMousePostion(Point2D p) {
		mousePosition.setText(
			"(" + mouseFormat.format(p.getX()) + 
			"; " + mouseFormat.format(p.getY()) + ") [mm]");
	}

	/**
	 * projects screen coordinates back to paper coordinates
	 * and updates the mouse position label accordingly
	 *
	 * @param me  mouse event to extract screen coords from.
	 */
	protected void updateMousePositionLabel(MouseEvent me) {

		SVGDocument doc = docManager.getSVGDocument();

		if (doc != null) {
			SVGLocatable sheet =
				(SVGLocatable)doc.getElementById(
					DocumentManager.DOCUMENT_SHEET);

			if (sheet != null)
				try {
					SVGMatrix matrix = sheet.getScreenCTM();

					if (matrix != null) {
						AffineTransform at =
							MatrixTools.toJavaTransform(matrix).createInverse();

						Point2D src = new Point2D.Double(me.getX(), me.getY());
						Point2D dst = new Point2D.Double();
						at.transform(src, dst);
						setMousePostion(dst);
						return;
					}
				} 
				catch (Exception ex) {
				}
		}
		mousePosition.setText("(" + me.getX() + "; " + me.getY() + ")");
	}

	/**
	 * Nullify instance variables. Called on close.
	 */
	public void clear() {
		docManager    = null;
		pluginContext = null;

		if (tools != null) {
			ArrayList x = tools;
			tools = null;
			x.clear();
		}

		removeAction       = null;
		groupAction        = null;
		ungroupAction      = null;
		upAction           = null;
		downAction         = null;
		addScalebarAction  = null;
		addScaletextAction = null;
		boxPropAction      = null;
		textPropAction     = null;
		pickingInteractor  = null;
		lastDirectory      = null;
		mousePosition      = null;
		mouseFormat        = null;
		rulerOverlay       = null;
	}

	/**
	 * starts printing.
	 */
	public void print() {
		docManager.print(getBorder());
	}

	/**
	 * adds the openjump legent to the SVG document.
	 * Fetches the legend from the core and puts it into the SVG
	 * document.
	 */
	public void addLegend() {
		docManager.modifyDocumentLater(new Legend2SVG(pluginContext));
	}

	/**
	 * adds the openjump map to the SVG document.
	 * Fetches the openjump map from the core and puts it into the SVG
	 * document.
	 */
	public void addMap() {
		docManager.modifyDocumentLater(new Map2SVG(pluginContext));
	}

	/**
	 * adds a preview image oth the openjump map to the SVG document.
	 * Fetches the openjump map from the core and puts it into the SVG
	 * document.
	 */
	public void addPreviewMap() {
		docManager.modifyDocumentLater(new PreviewMap(pluginContext));
	}

	protected boolean confirmWrite(File file)
	{
	  if (!file.exists())
			return true;
		
		if (JOptionPane.showConfirmDialog(LayoutFrame.this, 
					I18N.getString("LayoutFrame.OverrideDialog.Message", 
						"This file already exists. Override it?"),
					I18N.getString("LayoutFrame.OverrideDialog.Title",
						"override dialog"),
					JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION)
			return true;
		return false;
	}
		
			
	protected void exportPDF() {
		JFileChooser fc = new JFileChooser(lastDirectory);
		FileFilter ff = new FileFilter("pdf", "*.pdf: PDF Document");

		fc.setFileFilter(ff);
		
		int result = fc.showSaveDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		file = ff.addExtIfNeeded(file);
	  
		if (confirmWrite(file))
			docManager.exportPDF(file);
	}

	protected void exportJPG() {
		JFileChooser fc = new JFileChooser(lastDirectory);

		FileFilter ff = new FileFilter(
			new String [] { "jpg", "jpeg"},
			"*.jpg, *.jpeg: JPG Image");

		fc.setFileFilter(ff);
		
		int result = fc.showSaveDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		file = ff.addExtIfNeeded(file);
	  
		if (!confirmWrite(file))
			return;

		JPEGParameterDialog jpgParameters =
			new JPEGParameterDialog(this);

		double [] size = new double[2];
		docManager.getPaperSize(size);

		Transcoder transcoder =
			jpgParameters.getTranscoder(size);

		jpgParameters.dispose();

		if (transcoder != null)
			docManager.transcodeToFile(transcoder, file);
	}


	protected void exportSVG() {
		JFileChooser fc = new JFileChooser(lastDirectory);

		FileFilter ff = new FileFilter(
			new String [] { "svg", "svgz" },
			"*.svg, *.svgz: SVG Document");

		fc.setFileFilter(ff);
		
		int result = fc.showSaveDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();
		file = ff.addExtIfNeeded(file);

		if (confirmWrite(file))
			docManager.exportSVG(file);
	}

	protected void importSVG() {

		JFileChooser fc = new JFileChooser(lastDirectory);

		FileFilter ff = new FileFilter(
			new String[] { "svg", "svgz" },
			"*.svg, *.svgz: SVG Document");

		fc.setFileFilter(ff);

		int result = fc.showOpenDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
			return;

		docManager.appendSVG(fc.getSelectedFile());
	}

	protected void saveSession() {
		JFileChooser fc = new JFileChooser(lastDirectory);

		int result = fc.showSaveDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
			return;

		File file = fc.getSelectedFile();

		if (confirmWrite(file))
			docManager.saveSession(file);
	}

	protected void loadSession() {

		JFileChooser fc = new JFileChooser(lastDirectory);

		int result = fc.showOpenDialog(docManager.getCanvas());

		lastDirectory = fc.getCurrentDirectory();

		if (result != JFileChooser.APPROVE_OPTION)
			return;

		docManager.loadSession(fc.getSelectedFile());
	}


	protected void importImage() {

		JFileChooser fc = new JFileChooser(lastDirectory);
		fc.setFileFilter(new FileFilter("gif", "*.gif"));
		fc.addChoosableFileFilter(
			new FileFilter(new String[] { "jpg", "jpeg" }, "*.jpeg"));
		fc.addChoosableFileFilter(
			new FileFilter(new String[] {"tiff", "tif"}, "*.tiff, *.tif" ));
		
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

	protected void addScaleBar() {
		final String [] ids = pickingInteractor.getSelectedIDs();

		if (ids == null || ids.length < 1)
			return;

		Object object = docManager.getData(ids[0]);
		if (!(object instanceof MapData))
			return;

		MapData map = (MapData)object;

		ScaleBarUpdater updater = new ScaleBarUpdater(
			map.getInitialScale(),
			ids[0]);

		docManager.addCenteredElement(updater, null);
	}

	protected void addScaleText() {
		final String [] ids = pickingInteractor.getSelectedIDs();

		if (ids == null || ids.length < 1)
			return;

		docManager.addText("",
			new DocumentManager.ModificationCallback() {
				public void run(DocumentManager manager, AbstractElement el) {

					SVGDocument doc = manager.getSVGDocument();
					SVGLocatable loc = (SVGLocatable)doc.getElementById(ids[0]);

					if (loc == null)
						return;
						
					String id = el.getAttributeNS(null, "id");
					ScaleUpdater updater = new ScaleUpdater(id, ids[0]);
					manager.addChangeListener(ids[0], updater);
					manager.addRemoveListener(id, updater);

					updater.elementTransformed(loc, manager);
				}
			});
	}

	protected void switchToPaperSize(String key) {
		double [] current = new double[2];
		docManager.getPaperSize(current);
		String guess = PaperSizes.guessPaperSize(current);
		if (guess == null || !guess.equals(key)) {
			SVGDocument document = PaperSizes.createSheet(key, null);
			if (document != null)
				docManager.switchToDocument(document);
		}
	}

	protected void activateRuler(boolean state) {
		rulerOverlay.setInUse(state);
	}

	protected Double[] getBorder() {
		Options op = Options.getInstance();
		Double[] border = {
			op.getDouble(LEFT_BORDER),
			op.getDouble(BOTTOM_BORDER),
			op.getDouble(RIGHT_BORDER),
			op.getDouble(TOP_BORDER)};

		return border;
	}
	
	protected void activateBorder(boolean state) {
		if (state) {
			Double[] border = getBorder();
			if (border == null)
				border = new Double[] {new Double(1.0), new Double(1.0), new Double(1.0),
						new Double(1.0)};
			printerBorderOverlay.setPrintBorder(border);
		}
		else
			printerBorderOverlay.setPrintBorder(null);
	}

	/**
	 * Pops up an options dialog.
	 */
	protected void showOptionDialog() {
		OptionDialog dialog = new OptionDialog(
			this,
			"/de/intevation/printlayout/resources/options_help.html");

		dialog.addIntegerOption(Map2SVG.EXTRA_WAIT, 
				I18N.getString("OptionDialog.Extra_Wait"));

		dialog.addIntegerOption(PreviewMapReplacer.EXTRA_ZOOM_WAIT,
				I18N.getString("OptionDialog.Extra_Zoom_Wait"));

		dialog.addBooleanOption(Map2SVG.OPTIMIZE_MAP_SVG,
				I18N.getString("OptionDialog.Optimize_Map"));

		dialog.addBooleanOption(Map2SVG.USE_CSS,
				I18N.getString("OptionDialog.Use_CSS"));

		// only make option editable if call is supported
		if (Map2SVG.supportsJava2DConverterSetter()) {

			dialog.addBooleanOption(Map2SVG.USE_SIMPLIFICATION,
					I18N.getString("OptionDialog.UseSimplification"));

			dialog.addDoubleOption(Map2SVG.SIMPLIFY_TOLERANCE,
					I18N.getString("OptionDialog.SimplifyTolerance"));

			dialog.addBooleanOption(Map2SVG.PRESERVE_TOPOLOGY,
					I18N.getString("OptionDialog.PreserveTopology"));
		}

		dialog.addDoubleOption(LEFT_BORDER, 
			I18N.getString("OptionDialog.LeftBorder", "LeftBorder"));
		dialog.addDoubleOption(BOTTOM_BORDER,
			I18N.getString("OptionDialog.BottomBorder", "BottomBorder"));
		dialog.addDoubleOption(RIGHT_BORDER,
			I18N.getString("OptionDialog.RightBorder", "RightBorder"));
		dialog.addDoubleOption(TOP_BORDER,
			I18N.getString("OptionDialog.TopBorder","TopBorder"));
		
		
		dialog.showDialog();
	}

	/**
	 * Pops up some usage information.
	 */
	public void showInfo() {
		InfoDialog.showDialog(
			LayoutFrame.this, 
			"/de/intevation/printlayout/resources/info.html");
	}

	/**
	 * Pops up an about dialog
	 */
	public void showAbout() {
		Template tmpl = new Template();
		tmpl.setVariable(
			"PLUGIN_NAME_VERSION", PrintLayoutPlugin.getNameAndVersion());
		InfoDialog.showDialog(
			LayoutFrame.this, 
			"/de/intevation/printlayout/resources/about.html", tmpl);
	}

	/**
	 * called after layer reordering to update the actions
	 */
	private final DocumentManager.ModificationCallback UPDATE_LAYER_BUTTONS =
		new DocumentManager.ModificationCallback() {
			public void run(DocumentManager manager, AbstractElement dummy) {
				String [] ids = pickingInteractor.getSelectedIDs();

				if (upAction != null)
					upAction.setEnabled(ids != null && docManager.hasNext(ids));

				if (downAction != null)
					downAction.setEnabled(ids != null && docManager.hasPrevious(ids));
			}
		};

	/** called if the user wants to move the selected elements
	 *  nearer to the top of the drawing.
	 */
	protected void elementsUp() {
		String [] ids = pickingInteractor.getSelectedIDs();
		docManager.moveUp(ids, UPDATE_LAYER_BUTTONS);
	}

	/** called if the user wants to move the selected elements
	 *  nearer to the bottom of the drawing.
	 */
	protected void elementsDown() {
		String [] ids = pickingInteractor.getSelectedIDs();
		docManager.moveDown(ids, UPDATE_LAYER_BUTTONS);
	}

	/** PickingInteractor.PickingListener */
	public void selectionChanged(PickingInteractor.PickingEvent evt) {
		PickingInteractor pi = (PickingInteractor)evt.getSource();
		String [] ids = pi.getSelectedIDs();
		int N = pi.numSelections();
			
		if (removeAction != null)
			removeAction.setEnabled(N > 0 
			&& !docManager.hasRecursiveChangeListeners(ids));

		if (groupAction != null)
			groupAction.setEnabled(N > 1);

		if (ungroupAction != null)
			ungroupAction.setEnabled(N > 0);

		if (upAction != null)
			upAction.setEnabled(N > 0
			&& docManager.hasNext(ids));

		if (downAction != null)
			downAction.setEnabled(N > 0
			&& docManager.hasPrevious(ids));

		if (addScaletextAction != null)
			addScaletextAction.setEnabled(
				N == 1 
				&& docManager.getData(ids[0]) instanceof MapData);

		if (addScalebarAction != null)
			addScalebarAction.setEnabled(
				N == 1 
				&& docManager.getData(ids[0]) instanceof MapData);
	
		if (boxPropAction != null)
			boxPropAction.setEnabled(
				N == 1
				&& ElementUtils.checkIDObjectByTag(
					getElementById(ids[0]), "rect"));
		
		if (textPropAction != null)
			textPropAction.setEnabled(
				N == 1
				&& ElementUtils.checkIDObjectByTag(
					getElementById(ids[0]), "text"));
	}

	/*
	protected void notImplementedYet() {
		JOptionPane.showMessageDialog(
			this,
			I18N.getString("LayoutFrame.NotImplementedYet", "Not implemented, yet!"),
			I18N.getString("LayoutFrame.Warning", "Warning"),
			JOptionPane.WARNING_MESSAGE);
	}
	*/

	private class PrintAction extends AbstractAction {
		PrintAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.Print", "Print...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.Print", "Print...")));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
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
					I18N.getString("LayoutFrame.ExportPDF", "Export &PDF...")));	
		}
		public void actionPerformed(ActionEvent ae) {
			exportPDF();
		}
	}

	private class JPGAction extends AbstractAction {
		JPGAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ExportJPG", "Export JPG...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ExportJPG", "Export &JPG...")));	
		}
		public void actionPerformed(ActionEvent ae) {
			exportJPG();
		}
	}

	private class ImportImageAction extends AbstractAction {
		ImportImageAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ImportImage", "Import Image...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ImportImage", "Import &Image...")));
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
					I18N.getString("LayoutFrame.ImportSVG", "Import &SVG...")));
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
				I18N.getString("LayoutFrame.ExportSVG", "E&xport SVG...")));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control E"));
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
					I18N.getString("LayoutFrame.AddMap", "Add &Map")));
		}
		public void actionPerformed(ActionEvent ae) {
			addMap();
		}
	}

	private class AddPreviewAction extends AbstractAction {
		AddPreviewAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.AddPreviewMap", "Add Preview Map")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.AddPreviewMap", "Add &Preview Map")));
		}
		public void actionPerformed(ActionEvent ae) {
			addPreviewMap();
		}
	}

	private class QuitAction extends AbstractAction {
		QuitAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.Close", "Close")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.Close", "&Close")));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control W"));
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
				I18N.getString("LayoutFrame.Remove", "&Remove")));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
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
					I18N.getString("LayoutFrame.Group", "&Group")));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl G"));
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
					I18N.getString("LayoutFrame.Ungroup", "&Ungroup")));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift ctrl G"));
		}
		public void actionPerformed(ActionEvent ae) {
			ungroup();
		}
	}

	private class UpAction extends AbstractAction {
		UpAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ElementsUp", "Move Up")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ElementsUp", "Move U&p")));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift PAGE_UP"));
		}
		public void actionPerformed(ActionEvent ae) {
			elementsUp();
		}
	}

	private class DownAction extends AbstractAction {
		DownAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ElementsDown", "Move Down")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ElementsDown", "Move &Down")));
			putValue(
				Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift PAGE_DOWN"));
		}
		public void actionPerformed(ActionEvent ae) {
			elementsDown();
		}
	}

	private class AddScalebarAction extends AbstractAction {
		AddScalebarAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.AddScaleBar", "Add Scalebar")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.AddScaleBar", "Add Scale&bar")));
		}
		public void actionPerformed(ActionEvent ae) {
			addScaleBar();
		}
	}

	private class AddScaletextAction extends AbstractAction {
		AddScaletextAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.AddScaleText", "Add Scaletext")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.AddScaleText", "Add S&caletext")));
		}
		public void actionPerformed(ActionEvent ae) {
			addScaleText();
		}
	}

	private class AddLegendAction extends AbstractAction {
		AddLegendAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.AddLegend", "Add Legend")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.AddLegend", "Add &Legend")));
		}
		public void actionPerformed(ActionEvent ae) {
			addLegend();
		}
	}

	private class AboutDialogAction extends AbstractAction {
		AboutDialogAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ShowAboutDialog", "About...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ShowAboutDialog", "&About...")));
		}
		public void actionPerformed(ActionEvent ae) {
			showAbout();
		}
	}

	private class InfoDialogAction extends AbstractAction {
		InfoDialogAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ShowInfoDialog", "Info...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ShowInfoDialog", "&Info...")));
			putValue(Action.ACCELERATOR_KEY, 
					KeyStroke.getKeyStroke("F1"));
		}
		public void actionPerformed(ActionEvent ae) {
			showInfo();
		}
	}

	private class OptionDialogAction extends AbstractAction {
		OptionDialogAction() {
			super(I18N.getString("LayoutFrame.OptionDialog"));
		}
		public void actionPerformed(ActionEvent ae) {
			showOptionDialog();
		}
	}

	private class SwitchPaperSizeAction extends AbstractAction {
		SwitchPaperSizeAction(String text, String actionCmd) {
			super(text);
			putValue(ACTION_COMMAND_KEY, actionCmd);
		}
		public void actionPerformed(ActionEvent ae) {
			switchToPaperSize((String)getValue(ACTION_COMMAND_KEY));
		}
	}

	private class SaveSessionAction extends AbstractAction {
		SaveSessionAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.SaveSession", "Save...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.SaveSession", "S&ave...")));
		  putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
		}
		public void actionPerformed(ActionEvent ae) {
			saveSession();
		}
	}

	private class LoadSessionAction extends AbstractAction {
		LoadSessionAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.LoadSession", "Load...")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.LoadSession", "&Load...")));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
		}
		public void actionPerformed(ActionEvent ae) {
			loadSession();
		}
	}

	private class BoxPropAction extends AbstractAction {
		private PickingInteractor interactor;
		private BoxFactory        factory = new BoxFactory();
		
		BoxPropAction(final PickingInteractor interactor) {
			super("");
			putValue(SMALL_ICON, IconLoader.icon("Palette.gif"));
			this.interactor = interactor;
			
		}
		public void actionPerformed(ActionEvent ae) {
			DrawingAttributes oldAttr = 
				DrawingAttributes.getRectDrawingAttributs(
					ElementUtils.getIDObjectByTag(
						getElementById(pickingInteractor.getSelectedIDs()[0]),
						"rect"));
			DrawingAttributes attr = 
				BoxPropertiesDialog.showDialog(LayoutFrame.this, oldAttr);
			String [] ids = interactor.getSelectedIDs();
			if (attr != null 
			&& ids != null
			) {
				factory.setDrawingAttributes(attr);
				docManager.modifyDocumentLater(factory.createUpdateModifier(ids));
			}	
		}
	}
	
	protected AbstractElement getElementById(String id) {
		return (AbstractElement)docManager.getSVGDocument().getElementById(id);
	}
	
	private class TextPropAction extends AbstractAction {
		private PickingInteractor interactor;
		private TextConsumer      consumer = new TextConsumer();
		
		TextPropAction(final PickingInteractor interactor) {
			super("");
			putValue(SMALL_ICON, IconLoader.icon("LabelAbove.gif"));
			this.interactor = interactor;
			
		}
		public void actionPerformed(ActionEvent ae) {
			AbstractElement element = ElementUtils.getIDObjectByTag(
					getElementById(pickingInteractor.getSelectedIDs()[0]),
					"text");
			
			TextDialog dialog = new TextDialog(LayoutFrame.this,
					DrawingAttributes.getText(element), 
					DrawingAttributes.getFont(element),
					DrawingAttributes.getColor(element, "stroke"));
			dialog.setVisible(true);

			if(dialog.isAccepted() && interactor.getSelectedIDs() != null
			&& interactor.getSelectedIDs().length > 0)
				docManager.modifyDocumentLater(
						consumer.createUpdateModifier(interactor.getSelectedIDs()[0],
							dialog.getChoosenText(), dialog.getChoosenColor(),
							dialog.getChoosenFont()));
		}
	}

	private class RulerAction extends AbstractAction {
		RulerAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ShowRuler", "&Show Ruler")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ShowRuler", "&Show Ruler")));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control R"));
		}
		public void actionPerformed(ActionEvent ae) {
			activateRuler(((JCheckBoxMenuItem)ae.getSource()).getState());
		}
	}

	private class PrintBorderAction extends AbstractAction {
		PrintBorderAction() {
			super(I18N.getName(
					I18N.getString("LayoutFrame.ShowBorder", "S&how Border")));
			putValue(Action.MNEMONIC_KEY, I18N.getMnemonic(
					I18N.getString("LayoutFrame.ShowBorder", "S&how Border")));
		}
		public void actionPerformed(ActionEvent ae) {
			activateBorder(((JCheckBoxMenuItem)ae.getSource()).getState());
		}
	}
}
// end of file
