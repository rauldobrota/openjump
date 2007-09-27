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

import javax.swing.ImageIcon;

import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.Surface;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
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
public class WPVSPlugIn extends ThreadedBasePlugIn {


    private WPVSClientDialog dialog;
    
    public boolean execute(final PlugInContext context)
	    throws Exception {
	    reportNothingToUndoYet(context);
	    
	    if ( dialog == null ){
	        dialog = new WPVSClientDialog( 
	            		context.getWorkbenchFrame(), 
	            		"WVS Request Parameters", 
	            		false,
	            		(TaskFrame)context.getActiveInternalFrame() );
	        
	        dialog.createTools( (TaskFrame)context.getActiveInternalFrame() );
	    	
	    }
	    dialog.setVisible( true );
        
	    if (!dialog.wasOKPressed()) {
            return false;
        }

	    return true;
    }
    


    private FeatureCollection createTilesFC( Surface[] tiles ){
        
        final String id = "ID";
        final String area = "AREA";
        final String geom = "GEOMETRY";
        final String scale = "SCALE";
        
        FeatureSchema fs = new FeatureSchema();
        fs.addAttribute( id, AttributeType.STRING );
        fs.addAttribute( area, AttributeType.DOUBLE );
        fs.addAttribute( scale, AttributeType.DOUBLE );
        fs.addAttribute( "GEOMETRY", AttributeType.GEOMETRY );
        
        FeatureCollection fc = new FeatureDataset( fs ); 
        for (int i = 0; i < tiles.length; i++) {
                
            BasicFeature bf = new BasicFeature( fs );
            bf.setAttribute( id, "" + i  );
            
            Geometry g = null;
            try {
                
                Envelope e = tiles[i].getEnvelope();
                org.deegree.model.spatialschema.Geometry gg =
                    GeometryFactory.createSurface( e, null );
                
                g = JTSAdapter.export( gg );
                
            } catch (GeometryException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            bf.setAttribute( area, new Double( g.getArea() ) );
            bf.setAttribute( geom, g );
            bf.setAttribute( scale, new Double( 123 ) );
//                new Double( GetViewServiceInvoker.calcResolution(tiles[i], new Dimension(256,256)) ) );
            fc.add( bf );
        }
        
        
        return fc;
    }
    

    
    /* (non-Javadoc)
     * @see com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn#run(com.vividsolutions.jump.task.TaskMonitor, com.vividsolutions.jump.workbench.plugin.PlugInContext)
     */
    public void run( TaskMonitor monitor, PlugInContext context ) throws Exception {

//       doRequest();
        
       
        /*
        GetView req = GetView.create( KVP2Map.toMap( dialog.createGetViewRequest() ) );
	    ViewPoint vp = ViewPoint.createViewPoint( req );
	    Splitter splitter = null;//new FractalSplitter( req, vp ); 
	    
	    Surface[] sur = splitter.createSurfaces();
	    
	    LayerManager layerManager = context.getLayerManager();
	    
	    FeatureCollection fc = createTilesFC( sur );
	    Layer layer = new Layer( "tiles", Color.GRAY, fc, layerManager );
	    
	    
	    layerManager.addLayer(	StandardCategoryNames.SYSTEM, layer)		    
        	.setDataSourceQuery(null)
        	.setFeatureCollectionModified(false);
        	*/

    }
/*
    private void doRequest() throws MalformedURLException, InterruptedException{
    	
        String s = dialog.createGetViewRequest();
        URL u = new URL( s );//"http://localhost:8080/deegree2wpvs/ogcwebservice?version=1.0.0&request=GetView&SRS=epsg:31466&OutputFormat=image/jpg&Datasets=Bonn_Luftbild&ElevationModel=BonnElevationModel&Width=400&Height=300&Distance=1000.0&Pitch=25.0&Yaw=290.0&AOV=45.0&POI=2579776,5618632,66&BoundingBox=2578440,5617628,2580764,5619349&CRS=EPSG:31466&Roll=0.0&BackgroundColor=001100&Background=latlonlogo&&EXCEPTIONS=application/vnd.ogc.se_xml" );

        viewWindow.setSize( 500,500);
        viewWindow.setVisible( true );
//        viewWindow.getContentPane().add( new JLabel( new ImageIcon( img ) ) );
        

        
        Image img = Toolkit.getDefaultToolkit().createImage( u );
        
        mediaTracker.addImage( img, 0 );
        mediaTracker.waitForID( 0 );
        
//        mediaTracker.waitForAll( 30000 );
        
        System.out.println("img: " + img);
//        ImageIO.write( (RenderedImage)img, "jpg", new File( "f:/temp/wpsv.jpg" ));
        
        System.out.println( s );
        
        Graphics g = viewWindow.getContentPane().getGraphics();
        g.drawImage( img, 0,0,null);
        g.dispose();

//        viewWindow.getContentPane().add( new JLabel( new ImageIcon(img)));
        viewWindow.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        viewWindow.pack();
        
    }
*/

    public ImageIcon getIcon() {
        return IconLoader.icon("World.gif");
    } 
    
    public static void main( String[] args ) {
    }
}


/* ********************************************************************
Changes to this class. What the people have been up to:
$Log$
Revision 1.1.2.1  2006/05/31 10:04:35  ut
deejump pre deegree day


********************************************************************** */