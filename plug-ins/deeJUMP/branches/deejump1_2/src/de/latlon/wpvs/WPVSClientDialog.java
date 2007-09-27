//$Header$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2005 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/exse/
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
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 
 ---------------------------------------------------------------------------*/

package de.latlon.wpvs;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

/**
 * ... 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author: ut $
 * 
 * @version 2.0, $Revision: 112 $, $Date: 2006-05-31 12:04:37 +0200 (Mi, 31 Mai 2006) $
 * 
 * @since 2.0
 */
public class WPVSClientDialog extends MultiInputDialog  implements AbstractCursorTool.Listener{

    public static final String WPVS_SERVER_URL = "Server URL";
    public static final String OUTPUT_FORMAT = "OutputFormat";
    public static final String DATASETS = "Datasets";
    public static final String ELEVATION_MODEL = "ElevationModel";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    public static final String POI = "PointOfInterest";
    public static final String BBOX = "BoundingBox";
    public static final String CRS = "CRS";
    public static final String DISTANCE = "Distance";
    public static final String PITCH = "Pitch";
    public static final String YAW = "Yaw";
    public static final String AOV = "AngleOfView";
    public static final String ROLL = "Roll";

    public static final String BACKGROUND_COLOR = "BackgroundColor";
    public static final String BACKGROUND = "Background";

    private JFrame viewWindow;

    private MediaTracker mediaTracker;
    
    private WPVSTool wpvsTool;
    private JTextField distanceTextField;
    private JTextField poiTextField;
    private JTextField boxTextField;
    private JTextField yawTextField;
    
    /**
     * @param frame
     * @param title
     * @param modal
     */
    public WPVSClientDialog( Frame frame, String title, boolean modal, TaskFrame taskFrame ) {
        super( frame, title, modal );
        initDialog( taskFrame );
        if ( mediaTracker == null ){
	    	mediaTracker = new MediaTracker( new JButton() );
	    }
        initViewWindow();
    }

    private void initDialog( TaskFrame taskFrame ) {
		
//    	createTools( taskFrame );
    	
        addTextField( WPVS_SERVER_URL, "http://localhost:8080/deegree2wpvs/ogcwebservice?", 50, null, null );
        addTextField( OUTPUT_FORMAT, "image/jpg", 25, null, null );
 		addTextField( DATASETS, "Bonn_TK", 25, null, DATASETS );
		addTextField( ELEVATION_MODEL, "BonnElevationModel_fine", 25, null, null );
		addSeparator();
		addIntegerField( WIDTH, 400, 5, null);
		addIntegerField( HEIGHT, 300, 5, null);
		addSeparator();
        poiTextField = addTextField( POI, "2579776,5618632,66", 20, null, null );
        boxTextField =  addTextField( BBOX, "2571542,5613593,2589508,5627273", 20, null, null );
		addTextField( CRS, "EPSG:31466", 10, null, null );
        distanceTextField = addDoubleField( DISTANCE, 1000, 5, null );
		addDoubleField( PITCH, 25, 4, null );
		yawTextField = addDoubleField( YAW, 290, 5, null );
		addDoubleField( AOV, 45, 5, null );
		addDoubleField( ROLL, 0, 4, null );
		addSeparator();
		addTextField( BACKGROUND_COLOR, "001100", 25, null, null );
		addTextField( BACKGROUND, "latlonlogo", 25, null, null );
		
//		GUIUtil.centreOnWindow( getParent() );
     }
    
        
    public void createTools( TaskFrame taskFrame ) {
    	WorkbenchToolBar toolBar = new WorkbenchToolBar( taskFrame );
    	
    	WPVSTool wpvsTool = new WPVSTool(){
    		public void activate(LayerViewPanel layerViewPanel) {
    			super.activate(layerViewPanel);
    			WPVSClientDialog.this.wpvsTool = this;
    		}
    		
    	};
    	wpvsTool.add( this );
    	toolBar.addCursorTool( new WPVSTool() );
    	
    	wpvsTool = new WPVSTool(){
    		
    		public javax.swing.Icon getIcon() {
    			return IconLoader.icon( "DrawRectangle.gif" );
    		}
    		
    		protected Shape getShape() throws NoninvertibleTransformException {
    			List coords = getCoordinates();
    			GeneralPath path = new GeneralPath();
    			Point2D firstPoint = getPanel().getViewport().toViewPoint((Coordinate)coords.get(0));
    			path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());

    			Point2D lastPoint = getPanel().getViewport().toViewPoint((Coordinate)coords.get( coords.size()-1 ));
    			path.lineTo((float) lastPoint.getX(), (float) lastPoint.getY());

    	        return path;
    		}
    		
    		public void activate(LayerViewPanel layerViewPanel) {
    			super.activate(layerViewPanel);
    			WPVSClientDialog.this.wpvsTool = this;
    		}
    		
    		public double[] getPOI(){
    			
    			Coordinate[] coords = this.getCordinates();
    			System.out.println("sasd");
    	    	return new double[]{
    	    			(coords[0].x + coords[1].x)/2d, 
    	    			(coords[0].y + coords[1].y)/2d,
    	    			50};
    	    }
    	};
    	wpvsTool.add( this );
    	
    	toolBar.addCursorTool( wpvsTool );
    	
    	JButton button = new JButton( "GetView!" );
    	button.addActionListener( new ActionListener(){
    		public void actionPerformed(ActionEvent e) {
    			try {
					doRequest();
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    		}
    	});
    	
    	JPanel p = new JPanel();
    	p.add( button );
    	p.add( toolBar );
    	
    	addRow( p );
    	
	}

	public String createGetViewRequest(){
        StringBuffer sb = new StringBuffer();
        sb.append( getText( WPVS_SERVER_URL ) )
            .append( "version=1.0.0&request=GetView&SRS=epsg:31466&")
         	
         
         	.append( OUTPUT_FORMAT ).append( "=" ).append( getText( OUTPUT_FORMAT ) ).append( "&" )
         	.append( DATASETS ).append( "=" ).append( getText( DATASETS ) ).append( "&" )
         	.append( ELEVATION_MODEL ).append( "=" ).append( getText( ELEVATION_MODEL ) ).append( "&" )

         	.append( WIDTH ).append( "=" ).append( getInteger( WIDTH ) ).append( "&" )
         	.append( HEIGHT ).append( "=" ).append( getInteger( HEIGHT ) ).append( "&" )

         	.append( DISTANCE ).append( "=" ).append( getDouble( DISTANCE ) ).append( "&" )
         	.append( PITCH ).append( "=" ).append( getDouble( PITCH ) ).append( "&" )
         	.append( YAW ).append( "=" ).append( getDouble( YAW ) ).append( "&" )
         	.append( "AOV" ).append( "=" ).append( getDouble( AOV ) ).append( "&" )
         	.append( "POI" ).append( "=" ).append( getText( POI ) ).append( "&" )
         	.append( BBOX ).append( "=" ).append( getText( BBOX ) ).append( "&" )
         	.append( CRS ).append( "=" ).append( getText( CRS ) ).append( "&" )
			.append( ROLL ).append( "=" ).append( getDouble( ROLL ) ).append( "&" )
         	
         	.append( BACKGROUND_COLOR ).append( "=" ).append( getText( BACKGROUND_COLOR ) ).append( "&" )
         	.append( BACKGROUND ).append( "=" ).append( getText( BACKGROUND ) ).append( "&" )
         	
         	.append( "&EXCEPTIONS=application/vnd.ogc.se_xml" )
         	.append( "&SPLITTER=BBOX" );

         return sb.toString();
     }
     
    private void doRequest() throws InterruptedException, IOException{
    	
        String s = createGetViewRequest();
        URL u = new URL( s );
        //"http://localhost:8080/deegree2wpvs/ogcwebservice?version=1.0.0&request=GetView&SRS=epsg:31466&OutputFormat=image/jpg&Datasets=Bonn_Luftbild&ElevationModel=BonnElevationModel&Width=400&Height=300&Distance=1000.0&Pitch=25.0&Yaw=290.0&AOV=45.0&POI=2579776,5618632,66&BoundingBox=2578440,5617628,2580764,5619349&CRS=EPSG:31466&Roll=0.0&BackgroundColor=001100&Background=latlonlogo&&EXCEPTIONS=application/vnd.ogc.se_xml" );

        viewWindow.setSize( 500,500);
        viewWindow.setVisible( true );
//        viewWindow.getContentPane().add( new JLabel( new ImageIcon( img ) ) );
        

        
        Image img = Toolkit.getDefaultToolkit().createImage( u );
        
        mediaTracker.addImage( img, 0 );
        mediaTracker.waitForID( 0 );
        
//        mediaTracker.waitForAll( 30000 );
        
        System.out.println("img: " + img);
        
        try {
        	BufferedImage img2 = new BufferedImage( img.getWidth( null), img.getHeight( null ), BufferedImage.TYPE_INT_RGB );
        	Graphics g = img2.getGraphics();
        	g.drawImage( img,0,0, null);
        	g.dispose();
            ImageIO.write( (RenderedImage)img2, "jpg", new File( "f:/temp/wpsv.jpg" ));
			
		} catch (Exception e) {
			// TODO: handle exception
		}
        
        System.out.println( s );
        /*
        Graphics g = viewWindow.getContentPane().getGraphics();
        g.drawImage( img, 0,0,null);
        g.dispose();
*/
        viewWindow.getContentPane().removeAll();
        viewWindow.getContentPane().add( new JLabel( new ImageIcon(img)));
        viewWindow.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        viewWindow.pack();
        
    }
	
    /**
     * 
     */
    private void initViewWindow() {
        
        viewWindow = new JFrame( "View" );
        viewWindow.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        
    }
    
    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool.Listener#gestureFinished()
     */
    public void gestureFinished() {
    	
    	System.out.println("gestureFinished-..");
    	
        Coordinate[] coords = this.wpvsTool.getCordinates();
        
        if( coords == null ){
        	return;
        }
        
        double distance =  Math.sqrt( Math.pow( coords[1].x - coords[0].x, 2 ) 
        						+ Math.pow( coords[1].y - coords[0].y, 2 ) );
        
        System.out.println("must make get compo public or access it someother way");
        
        distanceTextField.setText( String.valueOf( distance ) );
        
        //FIXME remove hardcoded height, height must be extra par, cos jump can't really change
        // z values of cursor tools
        double[] poi = this.wpvsTool.getPOI();
        poiTextField.setText( String.valueOf( poi[0] +","+ poi[1] +",50" ) );

        double x = coords[1].x - coords[0].x;
        double y = coords[1].y - coords[0].y;
        
        double theta = Math.atan2( y, x);
        double angle = -1d * Math.toDegrees( theta ) - 90d;
        angle = angle  < 0 ? angle + 360d : angle;
        
        yawTextField.setText( String.valueOf( angle ) );
        
        String box = coords[0].x + "," + coords[0].y + "," + coords[1].x + "," +coords[1].y;
        boxTextField.setText( box );
    
    }
}


/* ********************************************************************
Changes to this class. What the people have been up to:
$Log$
Revision 1.1.2.1  2006/05/31 10:04:35  ut
deejump pre deegree day


********************************************************************** */