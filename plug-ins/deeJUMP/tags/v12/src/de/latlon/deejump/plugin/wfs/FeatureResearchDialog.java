/*----------------    FILE HEADER  ------------------------------------------

 Copyright (C) 2001-2005 by:
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstraﬂe 19
 53177 Bonn
 Germany


 ---------------------------------------------------------------------------*/

package de.latlon.deejump.plugin.wfs;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import org.deegree.gml.GMLGeometry;
import com.vividsolutions.jts.geom.Envelope;

import de.latlon.deejump.ui.ExtensibleComboBox;
import de.latlon.deejump.ui.Messages;

/**
 * This dialog presents a graphical user interface to OGC Filter operations. It
 * encapsulates two panels, one for attribute-based feature search and the other
 * for geometry-based search. Both search methods can be combined. The dialog
 * generates a GetFeature request as an XML string. This can be used to query a
 * WFS. <p/>Original design: Poth
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei </a>
 *  
 */
public class FeatureResearchDialog extends JDialog {

	public static final String WFS_URL_LIST = "WFS_URL_LIST";

	private static List servers = new ArrayList();

	private List lastValidURLsList;

	// Constants for spatial search criteria type
	// also used by child panels
	/** Search uses no spatial criteria */
	public static final String NONE = "NONE";

	/** Search uses bounding box as spatial criteria */
	public static final String BBOX = "BBOX";

	/** Search uses a selected (GML) geometry as spatial criteria */
	public static final String SELECTED_GEOM = "SELECTED_GEOM";

	/**
	 * The standard geometry type name (used when getting schemata and creating
	 * filters with spatial clauses
	 */
	public static final String GEOMETRY_PROPERTY_NAME = "GEOM";

	/** The panel containing the interface for attribute-based search */
	private AttributeResearchPanel attributeResPanel;

	/** The panel containing the interface for geometry-based search */
	private SpatialResearchPanel spatialResPanel;

	String[] attributeNames = new String[] {};

	WFService wfService;

	private String geoPropName = GEOMETRY_PROPERTY_NAME;

	/**
	 * Whether the dialog has enough info to produce a search or it makes sense
	 * to carry on. For example, when the user closed (cancelled) the dialog.
	 */
	private boolean canSearch = false;

	private JTextArea textArea;

	private JComboBox serverCombo;

	private JButton okButton;

	private JTabbedPane tabs;

	private static boolean IS_TEST = false;

	private JComboBox featureTypeCombo;

	private JPanel mainPanel;

	private Box box;

	private JButton extrasButton;

	private JLabel featTypeLabel;

	// FIXME should be called bboxGeom and be of GMLGeom type?
	// should no crete dependency on JUMP use deegree Envelope
	/** The envelope of the current bounding box */
	private Envelope envelope = new Envelope(-1d, -1d, 1d, 1d);

	//private GMLGeometry gmlBbox;
	private GMLGeometry selectedGeom;

	private String srs = "EPSG:4326";

	/**
	 * Creates a dialog from an owner, with a title and a WFS server address.
	 * 
	 * @param owner
	 *            the parent window
	 * @param title
	 *            the name to appear on the window bar
	 * @param wfsServer
	 *            the address of the server. This is something like
	 *            http://my.domain.com/deegreewfs/wfs
	 * @throws java.awt.HeadlessException
	 */
	public FeatureResearchDialog(Frame owner, String title, List urlList)
			throws Exception {

		super(owner, title, true);
		setLocation(0, 50);
		//        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				canSearch = false;
				if (IS_TEST) {
					System.exit(0);
				}
				dispose();
			}

			public void windowActivated() {
			}

			public void windowOpened() {
			}
		});

		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				int key = event.getKeyCode();
				if (key == KeyEvent.VK_ESCAPE) {
					dispose();
					setVisible(false);
					if (IS_TEST) {
						System.exit(0);
					}
				}

			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}
		});
		
		setWFSList( urlList );
		if (IS_TEST) {
			wfService = new WFService( "http://127.0.0.1:8080/deegreewfs/wfs" );
			initFeatureTypeCombo();
		}
		initGUI();

	}

	public FeatureResearchDialog(ArrayList urls) {
		setWFSList(urls);
	}

	/** Initializes the FeatureType combo box of the AttributeResearchPanel */

	private void initFeatureTypeCombo() {

		try {

			String[] featTypes = wfService.getFeatureTypes();
			featureTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(
					featTypes));
			attributeNames = wfService.getFeatureProperties(featTypes[0]);

			okButton.setEnabled(true);
			attributeResPanel.setEnabled(true);
			tabs.setEnabledAt(1, true);
			featureTypeCombo.setEnabled(true);
			featureTypeCombo.setVisible(true);
			featTypeLabel.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Could not connect to WFS server at '"
							+ wfService.getWfsURL() + "'\n" + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);

			featureTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] {}));
			attributeResPanel.setEnabled(false);
			tabs.setEnabledAt(1, false);
			setCanSearch(false);

			featureTypeCombo.setEnabled(false);
			featureTypeCombo.setVisible(false);
			featTypeLabel.setVisible(false);
		}
	}

	/** Initialize main GUI and its children */
	private void initGUI() {

		setSize(450, 140);
		setResizable(true);

		// convenience panel
		mainPanel = new JPanel();
		setContentPane(mainPanel);
		LayoutManager lm = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
		mainPanel.setLayout(lm);

		serverCombo = createServerCombo();
		serverCombo.setPreferredSize(new Dimension(260, 21));

		JButton connecButton = new JButton(Messages
				.getString("FeatureResearchDialog.connect"));
		connecButton.setAlignmentX(0.5f);
		connecButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wfService = new WFService((String) serverCombo
						.getSelectedItem());
				initFeatureTypeCombo();
			}
		});

		featureTypeCombo = createFeatureTypeCombo();
		featureTypeCombo.setVisible(false);

		JPanel p = new JPanel();
		p.setPreferredSize(this.getSize());
		p.setToolTipText(Messages
				.getString("FeatureResearchDialog.wfsServiceToolTip"));

		p.add(new JLabel(Messages
						.getString("FeatureResearchDialog.wfsService")));

		p.add(serverCombo);
		p.add(connecButton);

		featTypeLabel = new JLabel(Messages
				.getString("FeatureResearchDialog.featureType"));
		featTypeLabel.setVisible(false);
		p.add(featTypeLabel);

		p.add(featureTypeCombo);
		p.add(featureTypeCombo);

		final Dimension dim = new Dimension(400, 650);
		final Dimension minDim = new Dimension(400, 500);

		tabs = new JTabbedPane() {
			public Dimension getPreferredSize() {
				return dim;
			}

			public Dimension getMinimumSize() {
				return minDim;
			}
		};

		attributeResPanel = new AttributeResearchPanel(this, featureTypeCombo);
		attributeResPanel.setEnabled(false);
		tabs.add(Messages.getString("FeatureResearchDialog.attributeSearch"),
				attributeResPanel);
		spatialResPanel = new SpatialResearchPanel(this);
		tabs.add(Messages.getString("FeatureResearchDialog.spatialSearch"),
				spatialResPanel);

		tabs.add(Messages.getString("FeatureResearchDialog.request"),
				createTextArea());

		box = Box.createHorizontalBox();

		okButton = new JButton(Messages.getString("OK"));
		//okButton.setAlignmentX(0.5f);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
				setCanSearch(true);
				if (IS_TEST) {
					System.exit(0);
				}
			}
		});
		okButton.setEnabled(false);

		JButton cancelButton = new JButton(Messages.getString("CANCEL"));

		cancelButton.setAlignmentX(0.5f);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
				setCanSearch(false);
				if (IS_TEST) {
					System.exit(0);
				}

			}
		});

		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.add(p);
		mainPanel.add(Box.createVerticalStrut(10));

		okButton.setFocusable(true);

		final String showExtras = "Extras";
		final String hideExtras = "Hide Extras";

		extrasButton = new JButton(showExtras);
		extrasButton.setBounds(260, 20, 80, 20);
		extrasButton.setAlignmentX(0.5f);
		extrasButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String actComm = e.getActionCommand();
				JButton b = (JButton) e.getSource();
				if (showExtras.equals(actComm)) {
					mainPanel.remove(box);
					resize(450, 650);
					mainPanel.add(tabs);
					mainPanel.add(box);
					b.setText(hideExtras);
					b.setActionCommand(hideExtras);
				} else {
					mainPanel.remove(box);
					mainPanel.remove(tabs);
					resize(450, 150);
					mainPanel.add(box);
					b.setText(showExtras);
					b.setActionCommand(showExtras);

				}
			}
		});

		box.add(extrasButton);
		box.add(new JLabel("         "));//Box.createHorizontalStrut(20));
		box.add(okButton);
		box.add(new JLabel("         "));//Box.createHorizontalStrut(20));
		box.add(cancelButton);
		mainPanel.add(box);
	}

	// Gh 15.11.05
	private JComboBox createServerCombo() {
		// 
		if (wfService != null) {
			servers.add(0, wfService.getWfsURL());
		}
		String[] server = (String[]) servers
				.toArray(new String[servers.size()]);
		final ExtensibleComboBox extensibleComboBox = new ExtensibleComboBox(
				server);
		extensibleComboBox.setSelectedIndex(0);
		extensibleComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String selected = extensibleComboBox.getSelectedItem()
							.toString();
					wfService = new WFService(selected);
					initFeatureTypeCombo();
				}
			}
		});
		return extensibleComboBox;
	}

	//GH 29.11.05
	public void setWFSList( List serverURLS ) {
		servers = serverURLS;
	}

	public List getWfsList() {
		return servers;
	}

	private JComboBox createFeatureTypeCombo() {
		String[] start = { "            " };
		JComboBox tmpFeatureTypeCombo = new JComboBox(start);
		return tmpFeatureTypeCombo;
	}

	/** Creates a GetFeature request by concatenation of xlm elements */
	private StringBuffer createRequest() {

		StringBuffer sb = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");

		sb.append("<wfs:GetFeature xmlns:ogc=\"http://www.opengis.net/ogc\" ")
				.append("xmlns:gml=\"http://www.opengis.net/gml\" ").append(
						"xmlns:wfs=\"http://www.opengis.net/wfs\" ").append(
						"outputFormat=\"GML2\">")

				.append("<wfs:Query typeName=\"").append(
						(String) featureTypeCombo.getSelectedItem()).append(
						"\">");

		String spatCrit = attributeResPanel.getSpatialCriteria();

		int listSize = attributeResPanel.getListSize();

		String[] filterTags = new String[] { "", "" };

		if (listSize > 0 || !NONE.equals(spatCrit)) {
			filterTags = createStartStopTags("Filter");
		}
		sb.append(filterTags[0]);

		boolean includesSpatialClause = !NONE.equals(spatCrit);
		if (includesSpatialClause && listSize > 0) {
			sb.append(FeatureResearchDialog.createStartStopTags("And")[0]);
		}

		if (BBOX.equals(spatCrit)) {
			sb.append(createBboxGml());
		} else if (SELECTED_GEOM.equals(spatCrit)) {
			sb.append(spatialResPanel.getXmlElement());
		}

		sb.append(attributeResPanel.getXmlElement());
		if (includesSpatialClause && listSize > 0) {
			sb.append(FeatureResearchDialog.createStartStopTags("And")[1]);
		}

		sb.append(filterTags[1]);
		sb.append("</wfs:Query></wfs:GetFeature>");

		return sb;
	}

	private void setTextAreaText(String text) {
		textArea.setText(text.replaceAll(">", ">\n"));

	}

	/**
	 * Returns the complete GetFeature request as XML
	 */
	public String getWfsRequest() {
		String t = createRequest().toString();
		setTextAreaText(t);
		return t;
	}

	/**
	 * Convenience method to create XML tags mit "ogc" namespace. For example an
	 * input like MyTag will return <code>{"<ogc:MyTag>", "</ogc:MyTag>"}</code>
	 * 
	 * @param tagName
	 *            the tag name
	 * @return a String[] with start and stop tags
	 */
	public static final String[] createStartStopTags(String tagName) {
		String[] tags = new String[] { "<ogc:" + tagName + ">",
				"</ogc:" + tagName + ">" };
		return tags;
	}

	/**
	 * Returns the address of the WFS server
	 * 
	 * @return the address of the WFS server
	 */
	public String getWfsServer() {
		return (String) serverCombo.getSelectedItem();
		//return wfsServer;
	}

	public WFService getWfService() {
		return this.wfService;
	}

	/**
	 * Returns the currently chosen feature type
	 * 
	 * @return the name of the currently chosen feature type
	 */
	public String getFeatureType() {
		return (String) featureTypeCombo.getSelectedItem();
	}

	/**
	 * Sets the GML Geometry that is used for spatial comparison operations
	 * 
	 * @param geom
	 *            the comparison geometry. If null, the GUI will disable spatial
	 *            comparison
	 */
	public void setSelectedGMLGeometry(GMLGeometry geom) {
		//this.spatialResPanel.setGMLGeometry( geom );
		this.selectedGeom = geom;
		this.attributeResPanel.setSelGeoButtonEnabled(geom != null);
		//tabs.setEnabledAt(1, geom != null && attributeNames.length > 0);
	}

	public void setGMLGeometrySRS(String srs) {
		this.srs = srs;
		if (this.selectedGeom != null) {
			this.selectedGeom.setSrs(srs);
		}
	}

	public String getGMLGeometrySRS() {
		return this.srs;
	}

	/**
	 * Returns the currently selected geometry that serves as basis for spatial
	 * operation operations
	 * 
	 * @return the currently selected geometry
	 */
	public GMLGeometry getSelectedGMLGeometry() {
		return this.selectedGeom;
	}

	/**
	 * Sets the envelope inside of which the GetFeature request will be
	 * performed.
	 * 
	 * @param env
	 *            the envelope defining the current visible bounding box
	 * 
	 * FIXME this method should be removed in order to avoid unnecessary
	 * dependence on JUMP (use deegree Envelope or simply some double[][]
	 */
	public void setEnvelope(Envelope env) {
		if (env != null) {
			this.envelope = env;
		}
	}

	/**
	 * Creates the XML fragment containing a bounding box filter
	 * 
	 * @return the XML fragment containing a bounding box filter
	 */
	private StringBuffer createBboxGml() {

		StringBuffer sb = new StringBuffer(500);

		if (envelope != null) {
			sb.append("<ogc:BBOX>")
			//FIXME hardcoded PropertyName=GEOM
					.append("<ogc:PropertyName>GEOM</ogc:PropertyName>")
					.append("<gml:Box><gml:coord>").append("<gml:X>").append(
							envelope.getMinX()).append("</gml:X>").append(
							"<gml:Y>").append(envelope.getMinY()).append(
							"</gml:Y>").append("</gml:coord><gml:coord>")
					.append("<gml:X>").append(envelope.getMaxX()).append(
							"</gml:X>").append("<gml:Y>").append(
							envelope.getMaxY()).append("</gml:Y>").append(
							"</gml:coord></gml:Box></ogc:BBOX>");
		}

		return sb;
	}

	private JComponent createTextArea() {

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JScrollPane jsp = new JScrollPane(textArea);

		p.add(jsp);

		JButton createReq = new JButton(Messages
				.getString("FeatureResearchDialog.attributeSearch"));
		createReq.setBounds(260, 20, 80, 20);
		createReq.setAlignmentX(0.5f);
		createReq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTextAreaText(createRequest().toString());
				tabs.setSelectedIndex(2);
			}
		});

		p.add(createReq);
		return p;
	}

	/**
	 * Whether it makes sense to ask for a GetFeature request. This is generally
	 * true, but clicking on the Cancel or clisong the dialog will return
	 * <code>false</code>, meaning that the user changed his mind and no
	 * requst should be sent.
	 * 
	 * @return a boolean value hinting whether to carry on or not
	 */
	public boolean canSearch() {
		return this.canSearch;
	}

	public void setCanSearch(boolean canSearch) {
		this.canSearch = canSearch;
	}

	public void setGeoPropName(String name) {
		if (name != null) {
			this.geoPropName = name;
		}
	}

	public String getGeoPropName() {
		//return this.geoPropName;
		return wfService.getGeometryProperty(this.getFeatureType());
	}

	public boolean isEditable() {
		return this.attributeResPanel.isEditable();
	}

	//TODO remove this method
	public void _addGeoPropName(String name) {
		spatialResPanel.addGeoPropName(name);
	}

	/** Used for GUI layout testing */
	public static void main(String[] args) throws Exception {

		setWinLaF();
		FeatureResearchDialog rd = new FeatureResearchDialog(new JFrame(),
				"Recherche", null);
		IS_TEST = true;
		rd.setVisible(true);

	}

	/** Sets the look and feel to be that of Windows (the default by JUMP) */
	private static void setWinLaF() {
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}