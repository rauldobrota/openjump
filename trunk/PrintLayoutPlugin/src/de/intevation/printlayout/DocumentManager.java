/*
 * DocumentManager.java
 * --------------------
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
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.AbstractDocumentFragment;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;

import org.apache.batik.util.XMLResourceDescriptor; 

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.bridge.UserAgent;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGGElement;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGRect;

import org.w3c.dom.NodeList; 
import org.w3c.dom.Node; 
import org.w3c.dom.DOMImplementation;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;

import org.apache.batik.svggen.ImageHandlerBase64Encoder; 
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2DIOException;

import org.apache.batik.transcoder.print.PrintTranscoder;

import org.apache.fop.svg.PDFTranscoder;

import javax.xml.transform.Transformer;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;  

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Stack;

import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Graphics2D;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.Paper;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.imageio.ImageIO;

public class DocumentManager
{
	public static final String DOCUMENT_SHEET = "viewer-layout-sheet-svg";
	public static final String DOCUMENT_BELOW = "viewer-layout-below";
	public static final String OBJECT_ID      = "viewer-layout-id";
	public static final String OBJECT_ID_LEAF = "viewer-layout-id-leaf";

	protected LayoutCanvas svgCanvas;

	protected int          objectID;

	protected ExtraData    extraData;


	public interface DocumentModifier {
		Object run(DocumentManager documentManager);
	}

	public interface ModificationCallback {
		void run(DocumentManager documentManager, AbstractElement element);
	}

	protected DocumentManager() {
		extraData = new ExtraData();
	}

	public DocumentManager(LayoutCanvas svgCanvas) {
		this();
		this.svgCanvas = svgCanvas;
	}

	public void addChangeListener(
		String                   id,
		ExtraData.ChangeListener listener
	) {
		extraData.addChangeListener(id, listener);
	}

	public void removeChangeListener(
		String                   id,
		ExtraData.ChangeListener listener
	) {
		extraData.removeChangeListener(id, listener);
	}

	public void addRemoveListener(
		String                   id,
		ExtraData.RemoveListener listener
	) {
		extraData.addRemoveListener(id, listener);
	}

	public void removeRemoveListener(
		String                   id,
		ExtraData.RemoveListener listener
	) {
		extraData.removeRemoveListener(id, listener);
	}

	public ExtraData.Entry getOrCreateEntry(String id) {
		return extraData.getOrCreateEntry(id);
	}

	public void setData(String id, Object data) {
		extraData.setData(id, data);
	}

	public Object getData(String id) {
		return extraData.getData(id);
	}

	public boolean hasChangeListeners(String [] ids) {
		for (int i = 0; i < ids.length; ++i)
			if (extraData.hasChangeListeners(ids[i]))
				return true;
		return false;
	}

	public boolean hasChangeListeners(String id) {
		return extraData.hasChangeListeners(id);
	}

	public LayoutCanvas getCanvas() {
		return svgCanvas;
	}

	public SVGDocument getSVGDocument() {
		return svgCanvas.getSVGDocument();
	}

	public void getPaperSize(double [] size) {
		SVGDocument document = svgCanvas.getSVGDocument();
		if (document != null)
			getPaperSize(document, size);
	}

	public static void getPaperSize(SVGDocument document, double [] size) {

		AbstractElement sheet =
			(AbstractElement)document.getElementById(DOCUMENT_SHEET);

		if (sheet == null) {
			System.err.println("sheet not found");
			return;
		}

		try {
			size[0] = Double.parseDouble(sheet.getAttributeNS(null, "width"));
			size[1] = Double.parseDouble(sheet.getAttributeNS(null, "height"));
		}
		catch (NumberFormatException nfe) {
			size[0] = 210d;
			size[1] = 297d;
		}
	}

	public void setDocument(SVGDocument document) {
		svgCanvas.setSVGDocument(document);
	}

	public void modifyDocumentLater(final DocumentModifier modifier) {
		UpdateManager um = svgCanvas.getUpdateManager();

		if (um == null) {
			System.err.println("before first rendering finished");
			return;
		}

		um.getUpdateRunnableQueue().invokeLater(new Runnable() {
			public void run() {
				modifier.run(DocumentManager.this);
			}
		});
	}

	public Object modifyDocumentNow(final DocumentModifier modifier) {
		UpdateManager um = svgCanvas.getUpdateManager();

		if (um == null) {
			System.err.println("before first rendering finished");
			return null;
		}

		final Object [] result = new Object[1];

		try {
			um.getUpdateRunnableQueue().invokeAndWait(new Runnable() {
				public void run() {
					result[0] = modifier.run(DocumentManager.this);
				}
			});
		}
		catch (InterruptedException ie) {
			ie.printStackTrace();
			return null;
		}

		return result[0];
	}

	public void addText(String text) {
		addText(text, null);
	}

	public void addText(final String text, final ModificationCallback callback) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

				AbstractElement textElement = 
					(AbstractElement)document.createElementNS(svgNS, "text");

				textElement.setTextContent(text);

				AbstractElement xform =
					(AbstractElement)document.createElementNS(svgNS, "g");

				xform.setAttributeNS(null, "id", uniqueObjectID());

				Rectangle rect = documentManager.getCanvas().getBounds();

				AbstractElement sheet =
					(AbstractElement)document.getElementById(DOCUMENT_SHEET);

				Point2D centerScreen = new Point2D.Double(
					rect.x + 0.5d * rect.width,
					rect.y + 0.5d * rect.height);

				AffineTransform trans =
					MatrixTools.toJavaTransform(
						((SVGLocatable)sheet).getScreenCTM().inverse());

				Point2D center = new Point2D.Double();
				trans.transform(centerScreen, center);

				trans = AffineTransform
					.getTranslateInstance(center.getX(), center.getY());

				xform.setAttributeNS(
					null, "transform", MatrixTools.toSVGString(trans));

				xform.appendChild(textElement);

				sheet.appendChild(xform);

				if (callback != null)
					callback.run(documentManager, xform);


				return null;
			}
		});
	}

	public void exportSVG(final File file) {

		UpdateManager um = svgCanvas.getUpdateManager();

		if (um == null) {
			System.err.println("before first rendering finished");
			return;
		}

		um.getUpdateRunnableQueue().invokeLater(new Runnable() {
			public void run() {
				exportSVGwithinUM(file);
			}
		});
	}

	public void exportSVGwithinUM(File file) {
		AbstractDocument innerSVG = isolateInnerDocument();

		try {
			TransformerFactory factory     = TransformerFactory.newInstance();
			Transformer        transformer = factory.newTransformer();

			StreamResult outputTarget = new StreamResult(file);
			DOMSource    xmlSource    = new DOMSource(innerSVG);

			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(xmlSource, outputTarget);
		} 
		catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public void loadSession(File file) {

		ZipFile zip = null;

		try {
			zip = new ZipFile(file);

			// first read extra data
			ZipEntry extraEntry = zip.getEntry("extra.xml");
			if (extraEntry == null)
				return;

			XMLDecoder decoder = null;

			ExtraData extra;

			try {
				decoder =
					new XMLDecoder(
					zip.getInputStream(extraEntry));

				extra = (ExtraData)decoder.readObject();
			}
			finally {
				if (decoder != null) {
					decoder.close();
					decoder = null;
				}
			}

			// now the SVG document
			ZipEntry documentEntry = zip.getEntry("document.svg");
			if (documentEntry == null)
				return;

			SVGDocument document;

			InputStream is = null;

			try {
				is = zip.getInputStream(documentEntry);

				String parser = XMLResourceDescriptor.getXMLParserClassName();
				SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
				String uri = new File("document.svg").toURL().toString();

				document =
					(SVGDocument)factory.createDocument(uri, is);
			}
			finally {
				if (is != null) {
					try { is.close(); } catch (IOException ioe) {}
					is = null;
				}
			}

			final ExtraData   extraDataFromZip = extra;
			final SVGDocument documentFromZip  = document;

			modifyDocumentLater(new DocumentModifier() {
				public Object run(DocumentManager manager) {
					manager.loadSessionWithInUM(
						extraDataFromZip, 
						documentFromZip);
					return null;
				}
			});
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	protected void loadSessionWithInUM(
		ExtraData   extraData,
		SVGDocument document
	) {
		this.extraData = extraData;
		setDocument(document);
	}

	public void saveSession(final File file) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager manager) {
				manager.saveSessionWithinUM(file);
				return null;
			}
		});
	}

	protected void saveSessionWithinUM(File file) {

		AbstractDocument document =
			(AbstractDocument)getSVGDocument();

		if (document == null)
			return;

		ZipOutputStream zip = null;

		try {
			zip =
				new ZipOutputStream(
				new BufferedOutputStream(
				new FileOutputStream(file)));

			// first store the extra data
			zip.putNextEntry(new ZipEntry("extra.xml"));

			XMLEncoder encoder = null;
			try {
				encoder = new XMLEncoder(
					new FilterOutputStream(zip) { 
						public void close() throws IOException {} 
					});
				encoder.writeObject(extraData);
			}
			finally {
				if (encoder != null) {
					encoder.close();
					encoder = null;
				}
			}

			// now store the SVG document
			zip.putNextEntry(new ZipEntry("document.svg"));
			
			TransformerFactory factory     = TransformerFactory.newInstance();
			Transformer        transformer = factory.newTransformer();

			StreamResult outputTarget = new StreamResult(zip);

			DOMSource xmlSource = new DOMSource(document);

			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(xmlSource, outputTarget);   	
		}
		catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		catch (TransformerException e) {
			e.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally {
			if (zip != null) {
				try { zip.close(); } catch (IOException ioe) {}
				zip = null;
			}
		}
	}

	/** run in synced context, please! */
	public AbstractDocument isolateInnerDocument() {
		return isolateInnerDocument(null);
	}

	public AbstractDocument isolateInnerDocument(String aspectRatio) {

		AbstractDocument document = (AbstractDocument)svgCanvas.getSVGDocument();

		AbstractElement root = (AbstractElement)document.getDocumentElement();

		AbstractElement sheet =
			(AbstractElement)document.getElementById(DOCUMENT_SHEET);

		if (sheet == null) {
			System.err.println("sheet not found");
			return null;
		}

		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();

    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractDocument newDocument = (AbstractDocument)
			impl.createDocument(svgNS, "svg", null);

		NodeList children = sheet.getChildNodes();
		
		AbstractElement newRoot = 
			(AbstractElement)newDocument.getDocumentElement();

		if (aspectRatio != null)
			newRoot.setAttributeNS(null, "preserveAspectRatio", aspectRatio);
		
		AbstractDocumentFragment fragment = 
			(AbstractDocumentFragment)newDocument.createDocumentFragment(); 

		for (int i = 0, N = children.getLength(); i < N; ++i) {
			AbstractNode child = (AbstractNode)children.item(i);
			fragment.appendChild(newDocument.importNode(child, true));
		}

		newRoot.appendChild(fragment);

		newRoot.setAttributeNS(
			null, "width", sheet.getAttributeNS(null, "width") + "mm");

		newRoot.setAttributeNS(
			null, "height", sheet.getAttributeNS(null, "height") + "mm");

		newRoot.setAttributeNS(
			null, 
			"viewBox",
			"0 0 " + sheet.getAttributeNS(null, "width") + 
			" "    + sheet.getAttributeNS(null, "height"));   

		return newDocument;
	}

	public void switchToDocument(final SVGDocument newDocument) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager manager) {
				manager.switchToDocumentWithinUM(newDocument);
				return null;
			}
		});
	}

	protected void switchToDocumentWithinUM(SVGDocument newDocument) {

		AbstractDocument document =
			(AbstractDocument)svgCanvas.getSVGDocument();

		AbstractElement oldSheet =
			(AbstractElement)document.getElementById(DOCUMENT_SHEET);

		if (oldSheet == null)
			return;

		AbstractElement newSheet =
			(AbstractElement)newDocument.getElementById(DOCUMENT_SHEET);

		if (newSheet == null)
			return;

		NodeList children = oldSheet.getChildNodes();

		for (int i = 0, N = children.getLength(); i < N; ++i) {
			AbstractNode child = (AbstractNode)children.item(i);
			newSheet.appendChild(newDocument.importNode(child, true));
		}

		setDocument(newDocument);
	}


	public void appendImage(File file) {
		try {
			final BufferedImage image = ImageIO.read(file);

			modifyDocumentLater(new DocumentModifier() {
				public Object run(DocumentManager documentManager) {

					SVGDocument document = documentManager.getSVGDocument();

					String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

					AbstractElement img = 
						(AbstractElement)document.createElementNS(svgNS, "image");

					ImageHandlerBase64Encoder handler =
						new ImageHandlerBase64Encoder();

					try {
						handler.handleHREF(
							(RenderedImage)image,
							img,
							SVGGeneratorContext.createDefault(document));
					}
					catch (SVGGraphics2DIOException g2ioe) {
						g2ioe.printStackTrace();
						return null;
					}

					int width  = image.getWidth();
					int height = image.getHeight();

					double [] paper = new double[2];

					getPaperSize(paper);

					img.setAttributeNS(null, "width",  String.valueOf(width));
					img.setAttributeNS(null, "height", String.valueOf(height));

					img.setAttributeNS(null, "x", "0");
					img.setAttributeNS(null, "y", "0");
       
					AbstractElement group = 
						(AbstractElement)document.createElementNS(svgNS, "g");

					double s1 = paper[0]/(double)width;
					double s2 = paper[1]/(double)height;

					double scale = Math.min(s1, s2);

					AffineTransform xfrom =
						AffineTransform.getScaleInstance(scale, scale);

					group.setAttributeNS(null, 
						"transform", MatrixTools.toSVGString(xfrom));

					group.setAttributeNS(null,
						"id", documentManager.uniqueObjectID());

					group.appendChild(img);

					AbstractElement sheet =
						(AbstractElement)document.getElementById(DOCUMENT_SHEET);

					sheet.appendChild(group);

					return null;
				}
			});
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}
	}

	public void appendSVG(File file) {

		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

		try {
			String uri = file.toURL().toString();
			appendSVG((AbstractDocument)factory.createDocument(uri), null);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void appendSVG(
		AbstractDocument document, 
		AffineTransform  xform
	) {
		appendSVG(document, xform, true, null);
	}

	public void appendSVG(
		AbstractDocument document, 
		AffineTransform  xform,
		boolean          adjustView
	) {
		appendSVG(document, xform, adjustView);
	}

	public void appendSVG(
		final AbstractDocument     document, 
		final AffineTransform      xform,
		final boolean              adjustView,
		final ModificationCallback callback
	) {
		UpdateManager um = svgCanvas.getUpdateManager();

		if (um == null) {
			System.err.println("before first rendering finished");
			return;
		}

		um.getUpdateRunnableQueue().invokeLater(new Runnable() {
			public void run() {
				appendSVGwithinUM(document, xform, adjustView, callback);
			}
		});
	}

	protected static Rectangle2D.Double pseudoViewBox(AbstractElement svg) {
		return new Rectangle2D.Double(
			0d, 0d,
			Double.parseDouble(svg.getAttributeNS(null, "width")),
			Double.parseDouble(svg.getAttributeNS(null, "height")));
	}

	protected static void setAttrib(
		AbstractElement svg,
		String          field,
		double          px2mm,
		double          defaultVal
	) {
		try {
			double [] v = new double[1];
			TypoUnits.stringToMM(
				svg.getAttributeNS(null, field), 
				px2mm, 
				defaultVal,
				v);
			svg.setAttributeNS(null, field, String.valueOf(v[0]));
		}
		catch (NumberFormatException nfe) {
			svg.setAttributeNS(null, field, String.valueOf(defaultVal));
		}
	}

	protected void adaptUnits(AbstractElement svg, AbstractElement master) {

		Rectangle2D viewBox = pseudoViewBox(master);

		UserAgent ua = svgCanvas.getUserAgent();

		double px2mm;

		if (ua == null) {
			System.err.println("no user agent found");
			px2mm = 1d;
		}
		else {
			px2mm = ua.getPixelUnitToMillimeter();
			//System.err.println("px2mm: " + px2mm);
		}

		setAttrib(svg, "x",      px2mm, viewBox.getX());
		setAttrib(svg, "y",      px2mm, viewBox.getY());
		setAttrib(svg, "width",  px2mm, viewBox.getWidth());
		setAttrib(svg, "height", px2mm, viewBox.getHeight());
	}     
 
	public String uniqueObjectID() {
		return uniqueObjectID(true);
	}

	public String uniqueObjectID(boolean leaf) {
		String idString;
		String prefix = leaf ? OBJECT_ID_LEAF : OBJECT_ID;

		AbstractDocument document = (AbstractDocument)svgCanvas.getSVGDocument();
		do {
			idString = prefix + objectID;
			++objectID;
		}
		while (document.getElementById(idString) != null);
		return idString;
	}    

 	public void print() {

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {
				SVGDocument document = documentManager.getSVGDocument();
				final PrintTranscoder transcoder = new PrintTranscoder();

				TranscoderInput  input  = new TranscoderInput(
					isolateInnerDocument("none"));

				transcoder.transcode(input, null);

				PrinterJob job = PrinterJob.getPrinterJob();
				PageFormat pageFomat = new PageFormat();

				double [] size = new double[2];

				getPaperSize(size);

				// DIN A4: 210 mm × 297 mm 
				Paper paper = new Paper();
				double width  = TypoUnits.mm2in(size[0])*72d;
				double height = TypoUnits.mm2in(size[1])*72d;
				paper.setSize(width, height);

				pageFomat.setPaper(paper);

				/*
				pageFomat.setOrientation(
					height > width
					?	PageFormat.LANDSCAPE
					: PageFormat.PORTRAIT);
				*/

				job.setPrintable(new Printable() {
					public int print(Graphics g, PageFormat pf, int page) {

						Graphics2D g2d = (Graphics2D)g;

						AffineTransform trans =
							AffineTransform.getTranslateInstance(
								-pf.getImageableX(),
								-pf.getImageableY());

						double sw = pf.getWidth()/pf.getImageableWidth();
						double sh = pf.getHeight()/pf.getImageableHeight();

						AffineTransform scale =
							AffineTransform.getScaleInstance(sw, sh);

						trans.concatenate(scale);

						AffineTransform old = g2d.getTransform();
						old.concatenate(trans);
						g2d.setTransform(old);

						return transcoder.print(g2d, pf, page);
					}
				});

				if (job.printDialog()) {
					System.err.println("printing ...");
					try {
						job.print();
					}
					catch (PrinterException pe) {
						pe.printStackTrace();
					}
				}
				else
					System.err.println("print cancelled");
				return null;
			}
		});
	}          

	public void exportPDF(final File file) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {
				TranscoderInput input = new TranscoderInput(
					isolateInnerDocument());

				OutputStream out = null;
				try {
					out =
						new BufferedOutputStream(
						new FileOutputStream(file));

					PDFTranscoder pdfTrancoder = new PDFTranscoder();
						
					TranscoderOutput output = new TranscoderOutput(out);
					pdfTrancoder.transcode(input, output);

					out.flush();
				}
				catch (TranscoderException te) {
					te.printStackTrace();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
				finally {
					if (out != null) {
						try { out.close(); } catch (IOException ioe) {}
						out = null;
					}
				}
				return null;
			}
		});
	}

	public void appendSVGwithinUM(
		AbstractDocument     newDocument, 
		AffineTransform      matrix,
		boolean              adjustView,
		ModificationCallback modificationCallback
	) {
		AbstractDocument document = (AbstractDocument)svgCanvas.getSVGDocument();

		AbstractElement root = 
			(AbstractElement)document.getElementById(DOCUMENT_SHEET);

		if (adjustView)
			adaptUnits(
				(AbstractElement)newDocument.getDocumentElement(), 
				root);

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		
		AbstractElement xform = 
			(AbstractElement)document.createElementNS(svgNS, "g");

		xform.setAttributeNS(null, 
			"transform", matrix == null 
				? "matrix(1 0 0 1 0 0)" 
				: MatrixTools.toSVGString(matrix));

		xform.setAttributeNS(null, "id", uniqueObjectID());

		AbstractNode node = (AbstractNode)document.importNode(
			newDocument.getDocumentElement(), 
			true,
			false);

		xform.appendChild(node);

		root.appendChild(xform);

		if (modificationCallback != null)
			modificationCallback.run(this, xform);
	}

	protected interface ElementVisitor {
		boolean visit(AbstractElement element);
	}

	protected static boolean visit(
		AbstractElement element,
		ElementVisitor  visitor
	) {
		Stack stack = new Stack();

		stack.push(element);

		while (!stack.empty()) {
			element = (AbstractElement)stack.pop();
			String id = element.getAttributeNS(null, "id");
			if (id != null && id.startsWith(OBJECT_ID)) { 
				if (!visitor.visit(element))
					return false;
				if (id.startsWith(OBJECT_ID_LEAF))
					continue;
			}
			if (element.hasChildNodes()) {
				NodeList children = element.getChildNodes();
				for (int i = children.getLength()-1; i >= 0; --i) {
					Node node = children.item(i); 
					if (node instanceof AbstractElement)
						stack.push(node);
				}
			}
		}

		return true;
	}

	public boolean hasRecursiveChangeListeners(String [] ids) {

		if (ids == null)
			return false;

		SVGDocument document = getSVGDocument();

		for (int i = 0; i < ids.length; ++i) {
			AbstractElement element =
				(AbstractElement)document.getElementById(ids[i]);
			if (element != null && hasRecursiveChangeListeners(element))
				return true;
		}
		return false;
	}

	public boolean hasRecursiveChangeListeners(AbstractElement element) {

		final boolean [] has = new boolean[1];

		visit(element, new ElementVisitor() {
			public boolean visit(AbstractElement element) {
				String id = element.getAttributeNS(null, "id");
				if (id != null && hasChangeListeners(id)) {
					has[0] = true;
					return false;
				}
				return true;
			}
		});
		return has[0];
	}

	protected void recursiveRemove(AbstractElement element) {
		visit(element, new ElementVisitor() {
			public boolean visit(AbstractElement element) {
				extraData.remove(DocumentManager.this, element);
				return true;
			}
		});
	}

	protected void recursiveTransform(AbstractElement element) {
		visit(element, new ElementVisitor() {
			public boolean visit(AbstractElement element) {
				extraData.fireElementTransformed(DocumentManager.this, element);
				return true;
			}
		});
	}

	public void removeIDs(final String [] ids) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				for (int i = 0; i < ids.length; ++i) {

					AbstractElement element =
						(AbstractElement)document.getElementById(ids[i]);

					if (element == null || hasRecursiveChangeListeners(element)) 
						continue;

					AbstractElement parent =
						(AbstractElement)element.getParentNode();
					if (parent != null)
						parent.removeChild(element);

					recursiveRemove(element);

				} // for all ids

				return null;
			}
		});
	}

	public void groupIDs(final String [] ids) {
		if (ids == null || ids.length < 2)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				AbstractElement sheet = 
					(AbstractElement)document.getElementById(DOCUMENT_SHEET);

				ArrayList children = new ArrayList();

				for (int i = 0; i < ids.length; ++i) {
					AbstractElement element =
						(AbstractElement)document.getElementById(ids[i]);

					if (element != null) { // child found?
						AbstractElement parent =
							(AbstractElement)element.getParentNode();

						if (parent == sheet) {
							parent.removeChild(element);
							children.add(element);
						}
					}
				} // for all ids

				int N = children.size();

				if (N == 0)
					return null;

				String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

				AbstractElement group = 
					(AbstractElement)document.createElementNS(svgNS, "g");

				group.setAttributeNS(null, "transform", "matrix(1 0 0 1 0 0)");

				group.setAttributeNS(null, "id", uniqueObjectID(false));

				for (int i = 0; i < N; ++i)
					group.appendChild((AbstractElement)children.get(i));

				sheet.appendChild(group);

				return null;
			}
		});
	}

	public void ungroupIDs(final String [] ids) {


		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				IDS: for (int j = 0; j < ids.length; ++j) {
					String id = ids[j];

					if (id == null || !id.startsWith(OBJECT_ID)) // only split our groups
						continue;

					SVGDocument document = documentManager.getSVGDocument();

					AbstractElement element =
						(AbstractElement)document.getElementById(id);

					// found?
					if (element == null || !(element instanceof SVGGElement))
						continue;

					AbstractElement parent =
						(AbstractElement)element.getParentNode();

					// split only if directly in sheet
					if (parent == null 
					|| !parent.getAttributeNS(null, "id").equals(DOCUMENT_SHEET))
						continue;

					NodeList children = element.getChildNodes();

					int N = children.getLength();

					if (N < 2) // not really a group
						continue;

					ArrayList list = new ArrayList(N); // copy to

					// only split if the group consists of self created elements
					for (int i = 0; i < N; ++i) {
						AbstractElement child = (AbstractElement)children.item(i);
						if (!(child instanceof SVGGElement))
							continue IDS;
						String idx = child.getAttributeNS(null, "id");
						if (idx == null || !idx.startsWith(OBJECT_ID))
							continue IDS;
						list.add(child);
					}

					parent.removeChild(element);

					// save matrix
					String xformS = element.getAttributeNS(null, "transform");

					AffineTransform xform = xformS != null
						? MatrixTools.toJavaTransform(xformS)
						: new AffineTransform();

					for (int i = 0; i < N; ++i) {
						AbstractElement child = (AbstractElement)list.get(i);
						element.removeChild(child);

						xformS = child.getAttributeNS(null, "transform");

						if (xformS == null)
							child.setAttributeNS(
								null, "transform", MatrixTools.toSVGString(xform));
						else {
							AffineTransform yform = MatrixTools.toJavaTransform(xformS);
							yform.preConcatenate(xform);
							child.setAttributeNS(
								null, "transform", MatrixTools.toSVGString(yform));
						}

						parent.appendChild(child);
					}
				} // for all ids

				return null;
			}
		});
	}


	public void translateIDs(final String [] ids, final Point2D screenDelta) {

		if (ids == null || ids.length == 0)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				Point2D delta = new Point2D.Double();

				for (int i = 0; i < ids.length; ++i) {
					String id = ids[i];

					AbstractElement element =
						(AbstractElement)document.getElementById(id);

					if (element == null)
						return null;

					AffineTransform xform;

					try {
						xform	= MatrixTools.toJavaTransform(
							((SVGLocatable)element).getScreenCTM().inverse());
					}
					catch (SVGException se) {
						continue;
					} 

					xform.deltaTransform(screenDelta, delta);

					AffineTransform trans =
						AffineTransform.getTranslateInstance(delta.getX(), delta.getY());

					String xformS = element.getAttributeNS(null, "transform");

					xform = xformS == null
						? new AffineTransform()
						: MatrixTools.toJavaTransform(xformS);

					xform.concatenate(trans);

					element.setAttributeNS(
						null, "transform", MatrixTools.toSVGString(xform));

					recursiveTransform(element);
				}

				return null;
			}
		});
	}

	public void scaleFixedIDs(
		final String [] ids, 
		final Point2D   screenDelta,
		final Point2D   screenPos,
		final Point2D   startPos
	) {
		if (ids == null || ids.length == 0)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				for (int i = 0; i < ids.length; ++i) {
					String id = ids[i];

					AbstractElement element =
						(AbstractElement)document.getElementById(id);

					if (element == null)
						return null;

					SVGLocatable locatable = (SVGLocatable)element;

					AffineTransform invCTM;
					try {
						invCTM = MatrixTools.toJavaTransform(locatable.getScreenCTM())
							.createInverse();
					}
					catch (NoninvertibleTransformException nite) {
						continue;
					}

					Point2D startPosOnPaper = new Point2D.Double();
					invCTM.transform(startPos, startPosOnPaper);

					double distanceOrg = startPos.distance(screenPos);

					screenPos.setLocation(
						screenPos.getX() + screenDelta.getX(),
						screenPos.getY() + screenDelta.getY());

					double distanceDelta = startPos.distance(screenPos);

					double scale = distanceDelta/distanceOrg;

					AffineTransform trans1 =
						AffineTransform.getTranslateInstance(
							-startPosOnPaper.getX(),
							-startPosOnPaper.getY());

					AffineTransform scaleTrans =
						AffineTransform.getScaleInstance(scale, scale);

					AffineTransform trans2 =
						AffineTransform.getTranslateInstance(
							startPosOnPaper.getX(),
							startPosOnPaper.getY());

					scaleTrans.concatenate(trans1);
					trans2.concatenate(scaleTrans);

					String xformS = element.getAttributeNS(null, "transform");

					AffineTransform xform = xformS == null
						? new AffineTransform()
						: MatrixTools.toJavaTransform(xformS);

					xform.concatenate(trans2);

					element.setAttributeNS(
						null, "transform", MatrixTools.toSVGString(xform));

					recursiveTransform(element);
				}

				return null;
			}
		});
	}

	public void scaleIDs(
		final String [] ids, 
		final Point2D   screenDelta,
		final Point2D   screenPos
	) {
		if (ids == null || ids.length == 0)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				for (int i = 0; i < ids.length; ++i) {
					String id = ids[i];

					AbstractElement element =
						(AbstractElement)document.getElementById(id);

					if (element == null)
						return null;

					SVGLocatable locatable = (SVGLocatable)element;

					AffineTransform CTM =
						MatrixTools.toJavaTransform(locatable.getScreenCTM());

					SVGRect bbox = locatable.getBBox();

					Point2D center = new Point2D.Double(
						bbox.getX() + 0.5d * bbox.getWidth(),
						bbox.getY() + 0.5d * bbox.getHeight());

					Point2D centerOnScreen = new Point2D.Double();
					CTM.transform(center, centerOnScreen);

					double distanceOrg = centerOnScreen.distance(screenPos);

					screenPos.setLocation(
						screenPos.getX() + screenDelta.getX(),
						screenPos.getY() + screenDelta.getY());

					double distanceDelta = centerOnScreen.distance(screenPos);

					double scale = distanceDelta/distanceOrg;

					AffineTransform trans1 =
						AffineTransform.getTranslateInstance(
							-center.getX(),
							-center.getY());

					AffineTransform scaleTrans =
						AffineTransform.getScaleInstance(scale, scale);

					AffineTransform trans2 =
						AffineTransform.getTranslateInstance(
							center.getX(),
							center.getY());

					scaleTrans.concatenate(trans1);
					trans2.concatenate(scaleTrans);

					String xformS = element.getAttributeNS(null, "transform");

					AffineTransform xform = xformS == null
						? new AffineTransform()
						: MatrixTools.toJavaTransform(xformS);

					xform.concatenate(trans2);

					element.setAttributeNS(
						null, "transform", MatrixTools.toSVGString(xform));

					recursiveTransform(element);
				}

				return null;
			}
		});
	}

	public void rotateIDs(
		final String [] ids, 
		final Point2D   screenDelta,
		final Point2D   screenPos
	) {
		if (ids == null || ids.length == 0)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				for (int i = 0; i < ids.length; ++i) {
					String id = ids[i];

					AbstractElement element =
						(AbstractElement)document.getElementById(id);

					if (element == null)
						return null;

					SVGLocatable locatable = (SVGLocatable)element;

					AffineTransform CTM =
						MatrixTools.toJavaTransform(locatable.getScreenCTM());

					AffineTransform invCTM;
					try {
						invCTM = CTM.createInverse();
					}
					catch (NoninvertibleTransformException nite) {
						continue;
					}

					SVGRect bbox = locatable.getBBox();

					Point2D center = new Point2D.Double(
						bbox.getX() + 0.5d * bbox.getWidth(),
						bbox.getY() + 0.5d * bbox.getHeight());

					Point2D screenPosInCTM = new Point2D.Double();

					invCTM.transform(screenPos, screenPosInCTM);

					Point2D deltaInCTM = new Point2D.Double();

					invCTM.deltaTransform(screenDelta, deltaInCTM);

					double alpha = GeometricMath.angleBetween(
						center, screenPosInCTM);

					screenPosInCTM.setLocation(
						screenPosInCTM.getX() + deltaInCTM.getX(),
						screenPosInCTM.getY() + deltaInCTM.getY());

					double beta = GeometricMath.angleBetween(
						center, screenPosInCTM);

					double gamma = beta - alpha;

					AffineTransform rotate =
						AffineTransform.getRotateInstance(
							gamma, center.getX(), center.getY());

					String xformS = element.getAttributeNS(null, "transform");

					AffineTransform xform = xformS == null
						? new AffineTransform()
						: MatrixTools.toJavaTransform(xformS);

					xform.concatenate(rotate);

					element.setAttributeNS(
						null, "transform", MatrixTools.toSVGString(xform));

					recursiveTransform(element);
				}

				return null;
			}
		});
	}

	public static void decorateWithRulers(SVGDocument document) {
		if (document == null)
			return;

		AbstractElement root =
			(AbstractElement)document.getElementById(DOCUMENT_BELOW);

		if (root == null) {
			System.err.println("'" + DOCUMENT_BELOW + "' no found");
			return;
		}

		double [] sizes = new double[2];
		getPaperSize(document, sizes);

		// down
		root.appendChild(line(document, 17d, 20d, 17d, sizes[1] + 20d));
		// right
		root.appendChild(line(document, 20d, 17d, sizes[0]+ 20d, 17d));

		int count = 0;

		// markings x
		double end = sizes[0];
		for (double x = 0d; x <= end; ++x) {

			double length;
					 if (count == 0) length = 7d;
			else if (count == 5) length = 3.5d;
			else                 length = 2d;

			if (++count > 9) count = 0;

			root.appendChild(
				line(document, 20d+x, 17d, 20d+x, 17d-length));
		}

		count = 0;

		// markings y
		end = sizes[1];
		for (double y = 0d; y <= end; ++y) {

			double length;
					 if (count == 0) length = 7d;
			else if (count == 5) length = 3.5d;
			else                 length = 2d;

			if (++count > 9) count = 0;

			root.appendChild(
				line(document, 17d, 20d+y, 17d-length, 20d+y));
		}
	}

	protected static AbstractElement line(
		SVGDocument document,
		double x1, double y1,
		double x2, double y2
	) 
	{
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractElement l = 
			(AbstractElement)document.createElementNS(svgNS, "line");
		
		l.setAttributeNS(null, "x1", String.valueOf(x1));
		l.setAttributeNS(null, "y1", String.valueOf(y1));
		l.setAttributeNS(null, "x2", String.valueOf(x2));
		l.setAttributeNS(null, "y2", String.valueOf(y2));
		l.setAttributeNS(null, "stroke", "black");
		l.setAttributeNS(null, "stroke-width", "0.5"); 
	
		return l;
	}
}
// end of file
