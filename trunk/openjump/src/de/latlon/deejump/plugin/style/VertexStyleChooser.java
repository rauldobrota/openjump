/*
 * Created on 27.09.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.latlon.deejump.plugin.style;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;

/**
 * @author hamammi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * 
 * [sstein 02.08.2006] - removed point size slider
 */
public class VertexStyleChooser extends JPanel {
	
	private static final List STYLE_NAMES;
	
	static {
		
		List TEMP_STYLE_NAMES = new ArrayList( 5 );
		TEMP_STYLE_NAMES.add( VertexStylesFactory.SQUARE_STYLE );
		TEMP_STYLE_NAMES.add( VertexStylesFactory.CIRCLE_STYLE );
		TEMP_STYLE_NAMES.add( VertexStylesFactory.TRIANGLE_STYLE );
		TEMP_STYLE_NAMES.add( VertexStylesFactory.CROSS_STYLE  );
		TEMP_STYLE_NAMES.add( VertexStylesFactory.STAR_STYLE );
		TEMP_STYLE_NAMES.add( VertexStylesFactory.BITMAP_STYLE );
		
		STYLE_NAMES = Collections.unmodifiableList( TEMP_STYLE_NAMES );		
	}
	
	
	private JComboBox pointTypeComboBox;
	private JButton bitmapChangeButton;
	private String selectedItem;
	private String currentFilename;
//[sstein 02.08.2006] - removed because we would have two sliders
//	private JSlider sizeSlider;
	
	public VertexStyleChooser(){
		super();
		initGUI();
	}
	
	private void initGUI(){
		pointTypeComboBox = new JComboBox();
		pointTypeComboBox.setEditable(false);
		pointTypeComboBox.addItem( I18N.get("deejump.ui.style.RenderingStylePanel.square"  ));
		pointTypeComboBox.addItem( I18N.get("deejump.ui.style.RenderingStylePanel.circle" ) );
		pointTypeComboBox.addItem( I18N.get("deejump.ui.style.RenderingStylePanel.triangle" ) );
		pointTypeComboBox.addItem( I18N.get("deejump.ui.style.RenderingStylePanel.cross" ) );
		pointTypeComboBox.addItem( I18N.get("deejump.ui.style.RenderingStylePanel.star" ) );
		pointTypeComboBox.addItem( I18N.get("deejump.ui.style.RenderingStylePanel.bitmap" ) );
		
		pointTypeComboBox.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e) {		
			   	JComboBox comboBox = (JComboBox)e.getSource();
			   	String selectedItem = (String)STYLE_NAMES.get(comboBox.getSelectedIndex() );
			   	if(selectedItem.equals(VertexStylesFactory.BITMAP_STYLE)){
			   		OpenFileChooser();
			   	}
			   	setSelectedStyle(selectedItem);
			   	setSelectedItem(selectedItem);
			}
		});
		
		bitmapChangeButton = new JButton( I18N.get("deejump.ui.style.RenderingStylePanel.bitmap-change" ));
		bitmapChangeButton.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
		        OpenFileChooser();
			}
			});
		
//		sizeSlider = new JSlider();
//		sizeSlider.setBorder( BorderFactory.createTitledBorder( "Point size: "));
		
		Hashtable labelTable = new Hashtable();
        labelTable.put(new Integer(5), new JLabel("5"));
        labelTable.put(new Integer(10), new JLabel("10"));
        labelTable.put(new Integer(15), new JLabel("15"));
        labelTable.put(new Integer(20), new JLabel("20"));
//        sizeSlider.setLabelTable(labelTable);
//		sizeSlider.setEnabled(true);
//		sizeSlider.setMajorTickSpacing(1);
//		sizeSlider.setMajorTickSpacing(0);
//		sizeSlider.setPaintLabels(true);
//		sizeSlider.setMinimum(4);
//		sizeSlider.setValue(4);
//		sizeSlider.setMaximum(20);
//		sizeSlider.setSnapToTicks(false);
//		sizeSlider.setPreferredSize(  new Dimension(130, 49));
		JPanel oberstPanel = new JPanel();
		oberstPanel.add(new JLabel(I18N.get("deejump.ui.style.RenderingStylePanel.point-display-type")));
		oberstPanel.add(pointTypeComboBox);
		oberstPanel.add(bitmapChangeButton);
		JPanel sliderPanel = new JPanel();
//		sliderPanel.add(sizeSlider);
		setLayout(new BorderLayout());
		add(oberstPanel, BorderLayout.NORTH);
		add(sliderPanel, BorderLayout.CENTER);
        
	}
	
	public void addActionListener( ActionListener actionListener ){
		pointTypeComboBox.addActionListener( actionListener );
		bitmapChangeButton.addActionListener( actionListener);
		
	}
	
	public void removeActionListener( ActionListener actionListener ){
		pointTypeComboBox.removeActionListener( actionListener );
		bitmapChangeButton.removeActionListener( actionListener);
	}
	
	private void setSelectedItem(String selectedItem) {
		this.selectedItem = selectedItem;
		
	}
	
	public void addChangeListener( ChangeListener cl ){
//	    this.sizeSlider.addChangeListener( cl );		
	}
	public void removeChangeListener( ChangeListener cl ){
//	    this.sizeSlider.removeChangeListener( cl );		
	}

	
    //GH 2005.09.15 this Methode opens a JFileChooser and returns true if the Image
    // be loaded sucssfully.
	 public boolean OpenFileChooser(){
    	boolean imageIsLoaded = false;
		JFileChooser fileChooser = new JFileChooser( );
 	 	fileChooser.setFileFilter( new FileFilter() {
 	 		public boolean accept(File file){
 	 			return file.isDirectory() ||
                   file.getName().toLowerCase().endsWith( ".png" ) || 
                   file.getName().toLowerCase().endsWith( ".gif" ) || 
                   file.getName().toLowerCase().endsWith( ".jpg" ) ;
 	 		}
 	 		
 	 		public String getDescription(){
 	 			return "Bitmap";
 	 		}
 	 	});
 	 	int showFileChooser = fileChooser.showOpenDialog( this);
 	 	if(showFileChooser == JFileChooser.APPROVE_OPTION){
 	 	    String currentFilePath;
 	 	    currentFilePath = fileChooser.getSelectedFile().getAbsolutePath();
 	 	   Image image = Toolkit.getDefaultToolkit().getImage(currentFilePath);
 	 		setCurrentFileName(currentFilePath);
 	 		MediaTracker mediaTracker = new MediaTracker(this);
 	 		mediaTracker.addImage(image,0);
 	 		try {
 	 	        mediaTracker.waitForAll();
 	 	        imageIsLoaded = true;
 	 	      } catch (InterruptedException e) {
 	 	        //nothing
 	 	      }
 	 	}
 	 	else {
 	 		// user cancelled dialog
 	 	}
 	 	return imageIsLoaded;
	}	
	 
	 private void setCurrentFileName(String fileName){
	 	currentFilename = fileName;
	 }
	 
	 public String getCurrentFileName(){
	 	return currentFilename;
	 }

	 public void setEnabled( boolean enabled ) {
	 	super.setEnabled( enabled );
        this.pointTypeComboBox.setEnabled( enabled ); 
        this.bitmapChangeButton.setEnabled( enabled  );
//        this.sizeSlider.setEnabled( enabled );
        
	 }
	 
	/**
	 * @return the selected vertex style
	 */
	public VertexStyle getSelectedStyle() {
		String wellKnowName = (String)STYLE_NAMES.get( this.pointTypeComboBox.getSelectedIndex() );
		if ( VertexStylesFactory.BITMAP_STYLE.equals( wellKnowName )){
			wellKnowName = this.getCurrentFileName();
        }
		if(getCurrentFileName() != null){
			wellKnowName = this.getCurrentFileName();
		}
		
		VertexStyle vertexStyle = VertexStylesFactory.createVertexStyle( wellKnowName );
		if ( !(vertexStyle instanceof BitmapVertexStyle) ) {
//			vertexStyle.setSize( sizeSlider.getValue() );
		}
		return vertexStyle;
	}

	/**
	 * @param currentVertexStyle
	 */
	public void setSelectedStyle(String currentVertexStyle) {
		int nameIndex = STYLE_NAMES.indexOf( currentVertexStyle); 
		if ( nameIndex > -1 && nameIndex < STYLE_NAMES.size() ){
			this.pointTypeComboBox.setSelectedIndex( nameIndex );
		}
	}
}