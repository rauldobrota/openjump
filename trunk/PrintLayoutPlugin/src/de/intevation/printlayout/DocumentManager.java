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

import org.w3c.dom.NodeList; 
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
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

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
	public static final String OBJECT_ID      = "viewer-layout-id";

	protected LayoutCanvas svgCanvas;

	protected int          objectID;


	public interface DocumentModifier {
		Object run(DocumentManager documentManager);
	}

	public DocumentManager() {
	}

	public DocumentManager(LayoutCanvas svgCanvas) {
		this.svgCanvas = svgCanvas;
	}

	public LayoutCanvas getCanvas() {
		return svgCanvas;
	}

	public SVGDocument getSVGDocument() {
		return svgCanvas.getSVGDocument();
	}

	// XXX: potential sync problem?
	public void getPaperSize(double [] size) {

		SVGDocument document = svgCanvas.getSVGDocument();

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
		svgCanvas.installDocument(document);
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
			System.err.println("before first rendering finbished");
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

	/** run in synced context, please! */
	public AbstractDocument isolateInnerDocument() {
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
		final AbstractDocument document, 
		final AffineTransform  xform
	) {
		appendSVG(document, xform, true);
	}

	public void appendSVG(
		final AbstractDocument document, 
		final AffineTransform  xform,
		final boolean          adjustView
	) {
		UpdateManager um = svgCanvas.getUpdateManager();

		if (um == null) {
			System.err.println("before first rendering finished");
			return;
		}

		um.getUpdateRunnableQueue().invokeLater(new Runnable() {
			public void run() {
				appendSVGwithinUM(document, xform, adjustView);
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
		String idString;
		AbstractDocument document = (AbstractDocument)svgCanvas.getSVGDocument();
		do {
			idString = OBJECT_ID + objectID;
			++objectID;
		}
		while (document.getElementById(idString) != null);
		return idString;
	}    

 	public void print() {

		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {
				SVGDocument document = documentManager.getSVGDocument();
				PrintTranscoder transcoder = new PrintTranscoder();

				TranscoderInput  input  = new TranscoderInput(
					isolateInnerDocument());

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

				job.setPrintable(transcoder, pageFomat);

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
		AbstractDocument newDocument, 
		AffineTransform  matrix,
		boolean          adjustView
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
	}

	public void removeIDs(final String [] ids) {
		modifyDocumentLater(new DocumentModifier() {
			public Object run(DocumentManager documentManager) {

				SVGDocument document = documentManager.getSVGDocument();

				for (int i = 0; i < ids.length; ++i) {
					AbstractElement element =
						(AbstractElement)document.getElementById(ids[i]);

					if (element != null) { // child found?
						AbstractElement parent =
							(AbstractElement)element.getParentNode();
						if (parent != null)
							parent.removeChild(element);
					}
				} // for all ids

				return null;
			}
		});
	}
}
// end of file
