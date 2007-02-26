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

import org.apache.batik.transcoder.Transcoder;
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
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Stack;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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

import de.intevation.printlayout.util.TypoUnits;
import de.intevation.printlayout.util.GeometricMath;
import de.intevation.printlayout.util.MatrixTools;

import de.intevation.printlayout.beans.ExtraData;

/**
 *  Manages the SVG document and the attached data.
 *  It handles the operations of the SVG document.
 *  (Like printing, exporting, saving/loading projects, updating).
 *  It is also the place to modify a document.
 */
public class DocumentManager
{
	/**
	 * token of the layout sheet.
	 * In the SVG document always exist such a element.
	 * This element contains the paper size.
	 */
	public static final String DOCUMENT_SHEET = "viewer-layout-sheet-svg";
	
	/**
	 * token of the white rect below the sheet.
	 */
	public static final String DOCUMENT_BELOW = "viewer-layout-below";
	
	/**
	 * object id prefix.
	 */
	public static final String OBJECT_ID      = "viewer-layout-id";
	
	/**
	 * object leaf id prefix.
	 */
	public static final String OBJECT_ID_LEAF = "viewer-layout-id-leaf";


	/**
	 * stores the LayoutCanvas, which contains the SVG document.
	 */
	protected LayoutCanvas svgCanvas;

	protected int          objectID;

	protected ExtraData    extraData;

	/**
	 * for numeric stability
	 */
	private static final double EPS = 1e-2d;
	
	/**
	 * Use this interface to work on the SVG document.
	 *
	 * If you want to do operations on the SVG document (especially chage it),
	 * you need to do this inside an UpdateManager(UM).
	 * This is for synchronising the document with the Batik scenegraph.
	 *
	 * The DocumentManager works together with the modifyDocumentLater and
	 * the modifyDocumentNow methods. These two methods gives easy access
	 * to the UpdateManager.
	 * You have to implement the DocumentModifier interface and send it to one 
	 * of these methods to work on the document.
	 *
	 * Note: Every new added data element should be inserted
	 * into a g element with an unique object ID.
	 */
	public interface DocumentModifier {
		Object run(DocumentManager documentManager);
	}

	public interface ModificationCallback {
		void run(DocumentManager documentManager, AbstractElement element);
	}

	protected DocumentManager() {
		extraData = new ExtraData();
	}

	/**
	 * should be used. The DocumentManager needs the LayoutCanvas, because
	 * it contains the SVG document.
	 */
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

	/**
	 * returns the internal LayoutCanvas.
	 * @return svgCanvas
	 */
	public LayoutCanvas getCanvas() {
		return svgCanvas;
	}
	
	/**
	 * returns the managed SVG document from the svgCanvas.
	 * @return the document.
	 */
	public SVGDocument getSVGDocument() {
		return svgCanvas.getSVGDocument();
	}

	/**
	 * stores the paper size from the document sheet into the size
	 * array.
	 * @param size two dimensional array containing width and height
	 */
	public void getPaperSize(double [] size) {
		SVGDocument document = svgCanvas.getSVGDocument();
		if (document != null)
			getPaperSize(document, size);
	}

	/**
	 * helper method for getPaperSize
	 * 
	 * @param document the svg document
	 * @param size two dimensional array containing width and height
	 */
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

	/**
	 * sets the svg document to the LayoutCanvas.
	 * @param document the new SVG document.
	 */
	public void setDocument(SVGDocument document) {
		svgCanvas.setSVGDocument(document);
	}

	/**
	 * used to work on the SVG document. Mainly this method should be used
	 * for modifing the document.
	 * 
	 * @param modifier used to describe the modifications.
	 */ 
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

	/**
	 * tries to mofify the document at once. Normally modifyDocumentLater
	 * should be used.
	 * @param modifier used to describe the modifications.
	 */
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

	/**
	 * adds a text to the middle of the sheet of the document.
	 *
	 * @param text  the text
	 */	
	public void addText(String text) {
		addText(text, null);
	}

	/**
	 * is a generator for element.
	 * It works together with the addCenteredElement method.
	 */
	public interface ElementGenerator {
		AbstractElement generateElement(DocumentManager document);
	}

	/**
	 * addes an element in the center of the sheet.
	 * The element is created by the ElementGenerator.
	 * 
	 * @param generator used to create a SVG element.
	 * @param callback  called after inserting the element.
	 */
	public void addCenteredElement(
		final ElementGenerator     generator, 
		final ModificationCallback callback
	) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				AbstractElement element =
					generator.generateElement(documentManager);

				if (element == null)
					return null;

				SVGDocument document = documentManager.getSVGDocument();

				String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

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

				xform.appendChild(element);

				sheet.appendChild(xform);

				if (callback != null)
					callback.run(documentManager, xform);

				return null;
			}
		});
	}

	/**
	 * adds text to a document.
	 * The callback is called after adding the text.
	 *
	 * @param text the text added to the document.
	 * @param callback invoked after text adding.
	 */
	public void addText(final String text, final ModificationCallback callback) {
		ElementGenerator generator = new ElementGenerator() {
			public AbstractElement generateElement(DocumentManager docManager) {
				String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
				AbstractElement textElement = 
					(AbstractElement)docManager.getSVGDocument()
					.createElementNS(svgNS, "text");

				textElement.setTextContent(text);
			
				return textElement;
			}
		};

		addCenteredElement(generator, callback);
	}

	/**
	 * export the SVG to a file.
	 * It uses the UpdateManager and exportSVGWithinUM.
	 * @param file destination.
	 */
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

	/**
	 * helper method for exportSVG. It should be called in a UpdateManager.
	 * @param file destination
	 */
	public void exportSVGwithinUM(File file) {
		AbstractDocument innerSVG = isolateInnerDocument();

		try {
			TransformerFactory factory     = TransformerFactory.newInstance();
			Transformer        transformer = factory.newTransformer();

			StreamResult outputTarget = new StreamResult(file);
			OutputStream outputStream = null;
			if (file.getName().endsWith("z")) {
				outputTarget =  new StreamResult(
					outputStream = new GZIPOutputStream(new FileOutputStream(file)));
			}
			DOMSource    xmlSource    = new DOMSource(innerSVG);

			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(xmlSource, outputTarget);
			
			if (outputStream != null) {
				outputStream.close();
			}
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
	}

	/**
	 * loads a PrintLayoutPlugin session from a file.
	 * @param file source
	 */
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

	/**
	 * helper method for loadSession. It should be called inside a 
	 * UpdateManager and then sets all the information for displaying
	 * the svg.
	 * 
	 * @param extraData the new ExtraData 
	 * @param document  the new SVG document
	 */
	protected void loadSessionWithInUM(
		ExtraData   extraData,
		SVGDocument document
	) {
		this.extraData = extraData;
		setDocument(document);
	}

	/**
	 * saves the PrintLayoutPlugin session to a file-
	 * @param file destination.
	 */
	public void saveSession(final File file) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager manager) {
				manager.saveSessionWithinUM(file);
				return null;
			}
		});
	}

	/**
	 * helper method for saveSession. It should be called inside an 
	 * UpdateManager.
	 * @param file destination.
	 */
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

	/**
	 * extracts the inner document form a SVG document.
	 * It should be run in synced context.
	 * @param aspectRatio sets this string to the preserveAspectRatio attribute
	 * of the root node.
	 */
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

	/**
	 * switches to a new document. If a new paper size is choosen,
	 * this method is called.
	 * @param newDocument the new SVG document.
	 */
	public void switchToDocument(final SVGDocument newDocument) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager manager) {
				manager.switchToDocumentWithinUM(newDocument);
				return null;
			}
		});
	}

	/**
	 * helper method for switchToDocument. It should be invoked inside
	 * an UpdateManager.
	 * @param newDocument the new document.
	 */
	protected void switchToDocumentWithinUM(SVGDocument newDocument) {

		AbstractDocument document =
			(AbstractDocument)svgCanvas.getSVGDocument();

		AbstractElement oldSheet = 
			(AbstractElement)document.getElementById(DOCUMENT_SHEET);

		if (oldSheet == null)
			return;

		AbstractElement newSheet = 
			(AbstractElement) newDocument.getElementById(DOCUMENT_SHEET);

		if (newSheet == null)
			return;

		NodeList children = oldSheet.getChildNodes();

		for (int i = 0, N = children.getLength(); i < N; ++i) {
			AbstractNode child = (AbstractNode)children.item(i);
			newSheet.appendChild(newDocument.importNode(child, true));
		}

		setDocument(newDocument);
	}

	/**
	 * appends an image to the document.
	 * @param file source of the image.
	 */

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

					AbstractElement sheet = getElementById(document, DOCUMENT_SHEET);

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

	/**
	 * append the data of a svg(z) file to the document.
	 * @param file source of the svg(z) data.
	 */
	public void appendSVG(File file) {

		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

		InputStream in = null;
		try {
			String uri = file.toURL().toString();
			if (file.getName().endsWith("z"))
				appendSVG((AbstractDocument)factory.createDocument(
					uri,
					in = new GZIPInputStream(new FileInputStream(file))),
					null);
			else
				appendSVG((AbstractDocument)factory.createDocument(uri), null);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		finally {
			if (in != null) {
				try { in.close(); } catch (IOException ioe) {}
				in = null;
			}
		}
	}

	/**
	 * appends a SVG document with an affine transformation into the document of
	 * this manager.
	 *
	 * @param document the document to append.
	 * @param xform    the transform.
	 */
	public void appendSVG(
		AbstractDocument document, 
		AffineTransform  xform
	) {
		appendSVG(document, xform, true, null);
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

	/**
	 * used to create an unique object leaf id.
	 */
	public String uniqueObjectID() {
		return uniqueObjectID(true);
	}

	/**
	 * used to create unique object ids and object leaf ids..
	 * 
	 * There are to kinds of ids: object ids and object leaf ids.
	 * A single text, rect or etc element should be child of a "g"
	 * element with an unique object leaf id.
	 * That is also right for a map, because the subtree of a map
	 * should remain as a whole.
	 *
	 * A group of elements normallly has an unique object id.
	 */
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

	/**
	 * calls transcodeToFile(transcoder, file, true)
	 */
	public void transcodeToFile(
		Transcoder transcoder,
		File       file
	) {
		transcodeToFile(transcoder, file, false);
	}

	/**
	 * Isolates the inner SVG document and transcode it to a file.
	 * usefull for generating images.
	 * @param transcoder          the respective transcoder
	 * @param file                file to be written to
	 * @param preserveAspectRatio maintain aspect ratio of inner SVG?
	 */
	public void transcodeToFile(
		final Transcoder transcoder,
		final File       file,
		final boolean    preserveAspectRatio
	) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				BufferedOutputStream out = null;
				try {
					out =
						new BufferedOutputStream(
						new FileOutputStream(file));

					TranscoderOutput output = new TranscoderOutput(out);
					TranscoderInput  input  = new TranscoderInput(
						isolateInnerDocument(preserveAspectRatio ? null : "none"));
					transcoder.transcode(input, output);
				}
				catch (TranscoderException te) {
					te.printStackTrace();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
				finally {
					if (out != null) {
						try { out.flush(); } catch (IOException ioe) {}
						try { out.close(); } catch (IOException ioe) {}
						out = null;
					}
				}
				return null;
			}
		});
	}

	/**
	 * prints the document of this manager.
	 */
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

	/**
	 * exports the document as PDF to a file.
	 * @param file the destination of the pdf.
	 */
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

	/**
	 * helper method for appending SVG documents to the document of 
	 * this manager.
	 * 
	 * @param newDocument the new document.
	 * @param matrix   an affine transform of the new document.
	 * @param adjustView should the view be adjusted?
	 * @param modificationCallback called after the adding. It can be null.
	 */
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

	/**
	 * used to search for different objects.
	 * This interface works together with the visit method.
	 */
	protected interface ElementVisitor {
		/**
		 * looks at an element, chooses to save it and returns a status.
		 * The method DocumentManager.visit stops searching, 
		 * if visit returns false.
		 * 
		 * @param element the scaned element.
		 * @return        status, true if the search should continue
		 *												false if not.
		 */
		boolean visit(AbstractElement element);
	}

	/**
	 * searchs the subtree of element. Looks for element with object id or
	 * object leaf id and asks the visitor about them.
	 * @param element the subtree of this element is searched.
	 * @param visitor knows what to do with elements with ids.
	 * @return false, if the search is stopped, else true.
	 */
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
			AbstractElement element = getElementById(document, ids[i]);
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

	/**
	 * removes element subtrees which root has an specific id.
	 * @param ids  the ids which should be removed.
	 */
	public void removeIDs(final String [] ids) {
		if (ids == null || ids.length == 0)
			return;
		
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				for (int i = 0; i < ids.length; ++i) {

					AbstractElement element = getElementById(document, ids[i]);

					if (element == null || hasRecursiveChangeListeners(element)) 
						continue;

					removeElementFromParentNode(element);

					recursiveRemove(element);
					
				} // for all ids

				return null;
			}
		});
	}

	protected AbstractElement getElementById(SVGDocument document, String id) {
		return (AbstractElement) document.getElementById(id);
	}
	
	protected void removeElementFromParentNode(AbstractElement element) {
		AbstractElement parent = (AbstractElement)element.getParentNode();
		
		if (parent != null)
			parent.removeChild(element);
	}
	
	/**
	 * groups some element together.
	 * @param ids  the elements with this ids are grouped together.
	 */
	public void groupIDs(final String [] ids) {
		if (ids == null || ids.length < 2)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				AbstractElement sheet = getElementById(document, DOCUMENT_SHEET);

				ArrayList children = new ArrayList();

				for (int i = 0; i < ids.length; ++i) {
					AbstractElement element = getElementById(document, ids[i]);

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

				AbstractElement group = createGroupElement(document);

				for (int i = 0; i < N; ++i)
					group.appendChild((AbstractElement)children.get(i));

				sheet.appendChild(group);

				return null;
			}
		});
	}

	protected AbstractElement createGroupElement(SVGDocument document) {
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

		AbstractElement group = 
			(AbstractElement)document.createElementNS(svgNS, "g");

		group.setAttributeNS(null, "transform", "matrix(1 0 0 1 0 0)");
		group.setAttributeNS(null, "id", uniqueObjectID(false));

		return group;
	}
	
	/**
	 * ungroups elements by id.
	 * @param ids the elements to ungroup.
	 */
	public void ungroupIDs(final String [] ids) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				IDS: for (int j = 0; j < ids.length; ++j) {
					String id = ids[j];

					if (id == null || !id.startsWith(OBJECT_ID)) // only split our groups
						continue;

					SVGDocument document = documentManager.getSVGDocument();

					AbstractElement element = getElementById(document, id);

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

	/**
	 * translates the elements by a delta.
	 * Should be called inside an UpdateManager.
	 * 
	 * @param ids translated elements
	 * @param screenDelta the delta
	 */
	public void translateIDs(final String [] ids, final Point2D screenDelta) {

		if (ids == null || ids.length == 0)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				Point2D delta = new Point2D.Double();

				for (int i = 0; i < ids.length; ++i) {
					String id = ids[i];

					AbstractElement element = getElementById(document, id);

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

	/**
	 * shear some elements
	 */
	public void shearNoneUniformFixedIDs(
		final String [] ids, 
		final Point2D   screenDelta,
		final Point2D   screenPos,
		final Point2D   startPos,
		final Point2D   refPos
	) {
		if (ids == null || ids.length == 0)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				for (int i = 0; i < ids.length; ++i) {
					String id = ids[i];

					AbstractElement element = getElementById(document, id);

					if (element == null)
						return null;

					SVGLocatable locatable = (SVGLocatable)element;

					AffineTransform invCTM;
					try {
						AffineTransform CTM 
							= MatrixTools.toJavaTransform(locatable.getScreenCTM());
						invCTM = CTM.createInverse();
					}
					catch (NoninvertibleTransformException nite) {
						continue;
					}

					Point2D startPosOnPaper = new Point2D.Double();
					invCTM.transform(startPos, startPosOnPaper);
					Point2D screenPosOnPaper = new Point2D.Double();
					invCTM.transform(screenPos, screenPosOnPaper);
					Point2D refPosOnPaper = new Point2D.Double();
					invCTM.transform(refPos, refPosOnPaper);

					Point2D deltaOnPaper = new Point2D.Double();
					invCTM.deltaTransform(screenDelta, deltaOnPaper);

					double alpha = GeometricMath.angleBetween(
						refPosOnPaper,
						screenPosOnPaper);

					Point2D xDelta = new Point2D.Double(
						screenPosOnPaper.getX() + deltaOnPaper.getX(),
						screenPosOnPaper.getY());

					Point2D yDelta = new Point2D.Double(
						screenPosOnPaper.getX(),
						screenPosOnPaper.getY() + deltaOnPaper.getY());

					double betaX = GeometricMath.angleBetween(
						refPosOnPaper,
						xDelta);

					double betaY = GeometricMath.angleBetween(
						refPosOnPaper,
						yDelta);

					double gammaX = betaX - alpha;
					double gammaY = alpha - betaY;

					AffineTransform trans1 =
						AffineTransform.getTranslateInstance(
							-startPosOnPaper.getX(),
							-startPosOnPaper.getY());

					AffineTransform scaleTrans =
						AffineTransform.getShearInstance(
							Math.tan(gammaX),
							Math.tan(gammaY));

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

	/**
	 * scales some elements
	 */
	public void scaleNoneUniformFixedIDs(
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

					AbstractElement element = getElementById(document, id);

					if (element == null)
						return null;

					SVGLocatable locatable = (SVGLocatable)element;

					AffineTransform CTM;
					AffineTransform invCTM;
					try {
						CTM = MatrixTools.toJavaTransform(locatable.getScreenCTM());
						invCTM = CTM.createInverse();
					}
					catch (NoninvertibleTransformException nite) {
						continue;
					}

					Point2D startPosOnPaper = new Point2D.Double();
					invCTM.transform(startPos, startPosOnPaper);
					Point2D screenPosOnPaper = new Point2D.Double();
					invCTM.transform(screenPos, screenPosOnPaper);

					double distanceOrgX = 
						Math.abs(startPosOnPaper.getX() - screenPosOnPaper.getX());
					double distanceOrgY = 
						Math.abs(startPosOnPaper.getY() - screenPosOnPaper.getY());

					Point2D deltaOnPaper = new Point2D.Double();
					invCTM.deltaTransform(screenDelta, deltaOnPaper);

					screenPosOnPaper.setLocation(
						screenPosOnPaper.getX() + deltaOnPaper.getX(),
						screenPosOnPaper.getY() + deltaOnPaper.getY());

					double distanceDeltaX = 
						Math.abs(startPosOnPaper.getX() - screenPosOnPaper.getX());
					double distanceDeltaY = 
						Math.abs(startPosOnPaper.getY() - screenPosOnPaper.getY());

					double scaleX = Math.abs(distanceDeltaX - distanceOrgX) < EPS
					|| distanceOrgX < EPS
						? 1d
						: distanceDeltaX/distanceOrgX;

					double scaleY = Math.abs(distanceDeltaY - distanceOrgY) < EPS
					|| distanceOrgY < EPS
						? 1d
						: distanceDeltaY/distanceOrgY;

					AffineTransform trans1 =
						AffineTransform.getTranslateInstance(
							-startPosOnPaper.getX(),
							-startPosOnPaper.getY());

					AffineTransform scaleTrans =
						AffineTransform.getScaleInstance(scaleX, scaleY);

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

	/**
	 * scales some elements
	 */
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

					AbstractElement element = getElementById(document, id);

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

	/**
	 * rotates some elements
	 */
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
					AbstractElement element = getElementById(document, ids[i]);

					if (element == null)
						continue;

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

	/**
	 * looks if one of the given ids belong to an element
	 * that has a next element so it can be moved up.
	 * @param ids the ids to check
	 */
	public boolean hasNext(String [] ids) {
		if (ids != null && ids.length > 0) {
			SVGDocument document = getSVGDocument();
			for (int i = 0; i < ids.length; ++i) {
				if (!ids[i].startsWith(OBJECT_ID))
					continue;
				Node element = document.getElementById(ids[i]);
				if (element == null
				|| (element = element.getNextSibling()) == null
				|| !(element instanceof AbstractElement))
					continue;
				String id = ((AbstractElement)element).getAttributeNS(null, "id");
				if (id != null && id.startsWith(OBJECT_ID))
					return true;
			}
		}
		return false;
	}

	/**
	 * looks if one of the given ids belong to an element
	 * that has a previous element so it can be moved down.
	 * @param ids the ids to check
	 */
	public boolean hasPrevious(String [] ids) {
		if (ids != null && ids.length > 0) {
			SVGDocument document = getSVGDocument();
			for (int i = 0; i < ids.length; ++i) {
				if (!ids[i].startsWith(OBJECT_ID))
					continue;
				Node element = document.getElementById(ids[i]);
				if (element == null
				|| (element = element.getPreviousSibling()) == null
				|| !(element instanceof AbstractElement))
					continue;
				String id = ((AbstractElement)element).getAttributeNS(null, "id");
				if (id != null && id.startsWith(OBJECT_ID))
					return true;
			}
		}
		return false;
	}

	/**
	 * trys to move up the elements referenced by the ids one level.
	 * @param ids      the ids to move
	 * @param callback can be null. called when moving is done
	 */
	public void moveUp(
		final String []            ids, 
		final ModificationCallback callback
	) {
		if (ids == null || ids.length == 0)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				for (int i = 0; i < ids.length; ++i) {

					Node element = document.getElementById(ids[i]);
					if (element == null || !(element instanceof AbstractElement))
						continue;

					String id = ((AbstractElement)element).getAttributeNS(null, "id");
					if (id == null || !id.startsWith(OBJECT_ID))
						continue;

					Node next = element.getNextSibling();
					if (next == null || !(next instanceof AbstractElement))
						continue;

					id = ((AbstractElement)next).getAttributeNS(null, "id");
					if (id == null || !id.startsWith(OBJECT_ID))
						continue;

					Node parent = element.getParentNode();
					if (parent != null) {
						parent.removeChild(next);
						parent.insertBefore(next, element);
					}
				}

				if (callback != null)
					callback.run(documentManager, null);

				return null;
			}
		});
	}

	/**
	 * trys to move down the elements referenced by the ids one level.
	 * @param ids      the ids to move
	 * @param callback can be null. called when moving is done
	 */
	public void moveDown(
		final String []            ids,
		final ModificationCallback callback
	) {
		if (ids == null || ids.length == 0)
			return;

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				for (int i = 0; i < ids.length; ++i) {

					Node element = document.getElementById(ids[i]);
					if (element == null || !(element instanceof AbstractElement))
						continue;

					String id = ((AbstractElement)element).getAttributeNS(null, "id");
					if (id == null || !id.startsWith(OBJECT_ID))
						continue;

					Node prev = element.getPreviousSibling();
					if (prev == null || !(prev instanceof AbstractElement))
						continue;

					id = ((AbstractElement)prev).getAttributeNS(null, "id");
					if (id == null || !id.startsWith(OBJECT_ID))
						continue;

					Node parent = element.getParentNode();
					if (parent != null) {
						parent.removeChild(element);
						parent.insertBefore(element, prev);
					}
				}
				if (callback != null)
					callback.run(documentManager, null);

				return null;
			}
		});
	}

}
// end of file
