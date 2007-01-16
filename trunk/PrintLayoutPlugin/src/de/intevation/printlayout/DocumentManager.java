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

public class DocumentManager
{
	public static final String DOCUMENT_SHEET = "viewer-layout-sheet-svg";
	public static final String OBJECT_ID      = "viewer-layout-id";

	protected LayoutCanvas svgCanvas;

	protected int          objectID;


	public interface DocumentModifier {
		Object run(SVGDocument svgDocument);
	}

	public DocumentManager() {
	}

	public DocumentManager(LayoutCanvas svgCanvas) {
		this.svgCanvas = svgCanvas;
	}

	public LayoutCanvas getCanvas() {
		return svgCanvas;
	}

	// XXX: potential sync problem?
	public void getPaperSize(double [] size) {

		UserAgent ua = svgCanvas.getUserAgent();
		SVGDocument document = svgCanvas.getSVGDocument();

		double px2mm;

		if (ua == null) {
			System.err.println("no user agent found");
			px2mm = 1d;
		}
		else {
			px2mm = ua.getPixelUnitToMillimeter();
		}

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
				modifier.run(svgCanvas.getSVGDocument());
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
					result[0] = modifier.run(svgCanvas.getSVGDocument());
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
		UpdateManager um = svgCanvas.getUpdateManager();

		if (um == null) {
			System.err.println("before first rendering finished");
			return;
		}

		um.getUpdateRunnableQueue().invokeLater(new Runnable() {
			public void run() {
				appendSVGwithinUM(document, xform);
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
			System.err.println(field + ": " + v[0]);
			svg.setAttributeNS(null, field, String.valueOf(v[0]));
		}
		catch (NumberFormatException nfe) {
			System.err.println(field + ": " + defaultVal);
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
 
	protected String uniqueObjectID() {
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
			public Object run(SVGDocument document) {

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
			public Object run(SVGDocument document) {
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
		AffineTransform  matrix
	) {
		AbstractDocument document = (AbstractDocument)svgCanvas.getSVGDocument();

		AbstractElement root = 
			(AbstractElement)document.getElementById(DOCUMENT_SHEET);

		adaptUnits(
			(AbstractElement)newDocument.getDocumentElement(), 
			root);

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		
		AbstractElement xform = 
			(AbstractElement)document.createElementNS(svgNS, "g");

		System.err.println(MatrixTools.toSVGString(matrix));

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

		/*
		AbstractElement rect = 
			(AbstractElement)document.createElementNS(svgNS, "rect");

		rect.setAttributeNS(null, "x", "1");
		rect.setAttributeNS(null, "y", "1");
		rect.setAttributeNS(null, "width", "110");
		rect.setAttributeNS(null, "height", "147");
		rect.setAttributeNS(null, "stroke", "black");
		*/

		root.appendChild(xform);
		//root.appendChild(rect);
	}
}
// end of file
