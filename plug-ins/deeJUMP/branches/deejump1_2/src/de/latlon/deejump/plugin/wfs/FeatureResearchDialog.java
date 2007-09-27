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

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryImpl;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;

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

    private QualifiedName[] geoProperties;
    
    private QualifiedName geoProperty;
    
	WFService wfService;

	/**
	 * Whether the dialog has enough info to produce a search or it makes sense
	 * to carry on. For example, when the user closed (cancelled) the dialog.
	 */
	private boolean canSearch = false;

	private JTextArea textArea;

	private JComboBox serverCombo;

	private JButton okButton;

	private JTabbedPane tabs;

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
	private Geometry selectedGeom;

	private String srs = "EPSG:4326";

    private PropertiesPanel propertiesPanel;

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
				dispose();
			}
		});

		
		setWFSList( urlList );
		initGUI();

	}

	public FeatureResearchDialog(ArrayList urls) {
		setWFSList(urls);
	}

	/** Initializes the FeatureType combo box of the AttributeResearchPanel */

	private void refreshGUIs() {

        String[] featTypes = null;
        
		try {
			featTypes = wfService.getFeatureTypes();
			featureTypeCombo
                .setModel( new javax.swing.DefaultComboBoxModel( featTypes ) );
            

			okButton.setEnabled(true);
			tabs.setEnabledAt(1, true);
            
			featureTypeCombo.setEnabled(true);
			featureTypeCombo.setVisible(true);
			featTypeLabel.setVisible(true);
            attributeResPanel.setFeatureTypeComboEnabled( true );
            
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Could not connect to WFS server at '"
							+ wfService.getWfsURL() + "'\n" + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);

            
			featureTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] {}));
            attributeResPanel.setFeatureTypeComboEnabled( false );
            tabs.setEnabledAt(1, false);
			setCanSearch(false);

		}
        
        
        
        if( featTypes != null && featTypes.length > 0 ){
            try {
                attributeNames = wfService.getFeatureProperties( featTypes[0] );
                attributeResPanel.setEnabled(true);
                geoProperties = wfService.getGeometryProperties( featTypes[0] );
                
                propertiesPanel.setProperties( attributeNames, geoProperties );
                propertiesPanel.setEnabled( true );
                spatialResPanel.resetGeoCombo( geoProperties );

                
            } catch ( Exception e ) {
                
                e.printStackTrace();
                
                attributeResPanel.setEnabled(false);
                propertiesPanel.setEnabled( false );
                
                JOptionPane.showMessageDialog(this,
                      "Could not get DescribeFeatureType for '" + featTypes[0] 
                      +"' from WFS server at '"
                      + wfService.getWfsURL() + "'\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
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
                
                reinitService( (String) serverCombo.getSelectedItem() );
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

		final Dimension dim = new Dimension(400, 670);
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
        
        propertiesPanel = new PropertiesPanel( this );
        tabs.add( "Properties", propertiesPanel);

        
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
			}
		});
		okButton.setEnabled(false);

		JButton cancelButton = new JButton(Messages.getString("CANCEL"));

		cancelButton.setAlignmentX(0.5f);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
				setCanSearch(false);

			}
		});

		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.add(p);
		mainPanel.add(Box.createVerticalStrut(10));

		okButton.setFocusable(true);

		final String showAdvanced = "Advanced";
		final String hideAdvanced = "Hide Advanced Settings";

		extrasButton = new JButton(showAdvanced);
		extrasButton.setBounds(260, 20, 80, 20);
		extrasButton.setAlignmentX(0.5f);
		extrasButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String actComm = e.getActionCommand();
				JButton b = (JButton) e.getSource();
				if (showAdvanced.equals(actComm)) {
					mainPanel.remove(box);
                    setSize(450, 670);
					mainPanel.add(tabs);
					mainPanel.add(box);
					b.setText(hideAdvanced);
					b.setActionCommand(hideAdvanced);
				} else {
					mainPanel.remove(box);
					mainPanel.remove(tabs);
					setSize(450, 150);
					mainPanel.add(box);
					b.setText(showAdvanced);
					b.setActionCommand(showAdvanced);

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
                    reinitService( selected );

				}
			}
		});
		return extensibleComboBox;
	}

    private void reinitService( String url ) {
        try {
            wfService = new WFService( url );
            refreshGUIs();
            String ft = (String) featureTypeCombo.getSelectedItem();
        } catch ( Exception e ) {
            
            JOptionPane.showMessageDialog( this,
                                           "Could not connect to WFS server at '"
                                                   + url + "'\n" + e.getMessage(),
                                           "Error", JOptionPane.ERROR_MESSAGE);


            e.printStackTrace();
            
        } 

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
        
        tmpFeatureTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                JComboBox combo = (JComboBox)evt.getSource();
                
                try {
                    attributeNames = wfService
                        .getFeatureProperties( (String)combo.getSelectedItem() );
                    
                    attributeResPanel.refreshPanel();
                    geoProperties = getGeoProperties();
                    propertiesPanel.setProperties( attributeNames, geoProperties );
                    spatialResPanel.resetGeoCombo( geoProperties );
                    
                } catch ( Exception e ) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog( FeatureResearchDialog.this, "Error loading schema: " + e.getMessage() );
                }

                
            }
        });
        
        
		return tmpFeatureTypeCombo;
	}

	/** Creates a GetFeature request by concatenation of xlm elements */
	private StringBuffer createRequest() {

		StringBuffer sb = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");

		sb.append("<wfs:GetFeature xmlns:ogc=\"http://www.opengis.net/ogc\" ")
				.append( "xmlns:gml=\"http://www.opengis.net/gml\" ")
                .append( "xmlns:wfs=\"http://www.opengis.net/wfs\" ")
                .append( "outputFormat=\"text/xml; subtype=gml/3.1.1\">")

				.append( "<wfs:Query ");
                
        String ftName = (String) featureTypeCombo.getSelectedItem();
        QualifiedName ft = wfService.getQualiNameByFeatureTypeName( ftName );
        
        sb.append( "xmlns:" ).append( ft.getPrefix() )
            .append( "=\"" ).append( ft.getNamespace() ).append( "\" " )
                .append( "typeName=\"").append(
						ftName).append(
						"\">");

        sb.append( propertiesPanel.getXmlElement() );
        
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

	private void setRequestText(String text) {
		textArea.setText(text.replaceAll(">", ">\n"));

	}

	/**
	 * Returns the complete GetFeature request as XML
	 */
	public String getWfsRequest() {
		String t = createRequest().toString();
        setRequestText(t);
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
	}

	public WFService getWfService() {
		return this.wfService;
	}

	/**
	 * Returns the currently chosen feature type
	 * 
	 * @return the name of the currently chosen feature type
	 */
	public QualifiedName getFeatureType() {
        String s = (String)featureTypeCombo.getSelectedItem();
        return wfService.getQualiNameByFeatureTypeName( s );
	}

    public String getCurrentCrs(){
        return this.wfService.getCrsForFeatureType( getFeatureType() );
    }
    
	/**
	 * Sets the GML Geometry that is used for spatial comparison operations
	 * 
	 * @param geom
	 *            the comparison geometry. If null, the GUI will disable spatial
	 *            comparison
	 */
	public void setSelectedGMLGeometry( Geometry geom) {
		//this.spatialResPanel.setGMLGeometry( geom );
		this.selectedGeom = geom;
		this.attributeResPanel.setSelGeoButtonEnabled(geom != null);
		//tabs.setEnabledAt(1, geom != null && attributeNames.length > 0);
	}

	public void setGMLGeometrySRS( CoordinateSystem cs ) {
        //FIXME is this needed?
//		this.srs = cs;
		if (this.selectedGeom != null) {
			( (GeometryImpl) this.selectedGeom).setCoordinateSystem( cs );
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
	public Geometry getSelectedGeometry() {
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

        QualifiedName ft = getFeatureType(); 
		QualifiedName qn = getChosenGeoProperty();
        
		if (envelope != null) {
			sb.append("<ogc:BBOX>")
					.append("<ogc:PropertyName>")
                    .append( ft.getPrefix() ).append( ":" ).append( qn.getLocalName() )
                    .append("</ogc:PropertyName>")
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
				.getString("FeatureResearchDialog.createWFSRequest"));
		createReq.setBounds(260, 20, 80, 20);
		createReq.setAlignmentX(0.5f);
		createReq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setRequestText(createRequest().toString());
				tabs.setSelectedIndex(3);
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

	/*public void setGeoPropName(String name) {
		if (name != null) {
			this.geoPropName = name;
		}
	}*/

	public QualifiedName getChosenGeoProperty() {
        return geoProperty;
        /*QualifiedName[] qns = wfService.getGeometryProperties(this.getFeatureType().getAsString());
        QualifiedName qn = null;
        if ( qns.length > 1 ){
            qn = qns[0];
        }
		return qn;*/
	}

    public void setGeoProperty( QualifiedName geoProp ){
        this.geoProperty = geoProp;
    }
    
    public QualifiedName[] getGeoProperties() {
        return this.wfService.getGeometryProperties( (String)featureTypeCombo.getSelectedItem() );
    }
    
    public String[] getPropertiesNames(){
        return this.attributeNames;
    }
    
	public boolean isEditable() {
		return this.attributeResPanel.isEditable();
	}

	/** Used for GUI layout testing */
	public static void main(String[] args) throws Exception {

		setWinLaF();
		FeatureResearchDialog rd = new FeatureResearchDialog(new JFrame(),
				"WFS Dialog", null);
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