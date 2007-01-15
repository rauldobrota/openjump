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

import javax.xml.transform.Transformer;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;  

import java.io.File;
import java.io.IOException;

import java.awt.geom.Rectangle2D;

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

	public void setDocument(SVGDocument document) {
		svgCanvas.installDocument(document);
	}

	public void modifyDocumentLater(final DocumentModifier modifier) {
		UpdateManager um = svgCanvas.getUpdateManager();

		if (um == null) {
			System.err.println(" before first rendering finbished");
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
		UpdateManager um = svgCanvas.getUpdateManager();

		if (um == null) {
			System.err.println("before first rendering finished");
			return;
		}

		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

		try {
			String uri = file.toURL().toString();

			final AbstractDocument document = 
				(AbstractDocument)factory.createDocument(uri);

			um.getUpdateRunnableQueue().invokeLater(new Runnable() {
				public void run() {
					appendSVGwithinUM(document);
				}
			});
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
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
			System.err.println("px2mm: " + px2mm);
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

	public void appendSVGwithinUM(AbstractDocument newDocument) {

		AbstractDocument document = (AbstractDocument)svgCanvas.getSVGDocument();

		AbstractElement root = 
			(AbstractElement)document.getElementById(DOCUMENT_SHEET);

		adaptUnits(
			(AbstractElement)newDocument.getDocumentElement(), 
			root);

		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		
		AbstractElement xform = 
			(AbstractElement)document.createElementNS(svgNS, "g");

		xform.setAttributeNS(null, "transform", "matrix(1 0 0 1 0 0)");
		xform.setAttributeNS(null, "id", uniqueObjectID());

		AbstractNode node = (AbstractNode)document.importNode(
			newDocument.getDocumentElement(), 
			true,
			false);

		xform.appendChild(node);

		root.appendChild(xform);
	}
}
// end of file
