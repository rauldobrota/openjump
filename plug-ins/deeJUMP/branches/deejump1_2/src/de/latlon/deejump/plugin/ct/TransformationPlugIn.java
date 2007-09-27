package de.latlon.deejump.plugin.ct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryImpl;
import org.deegree.model.spatialschema.JTSAdapter;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

public class TransformationPlugIn extends ThreadedBasePlugIn {
    private Layer layer;
    private MultiInputDialog dialog;
    
    private static final Collection crsList = new ArrayList(4);
    
    static{
        crsList.add( "EPSG:2152" );
        crsList.add( "EPSG:31466" );
        crsList.add( "EPSG:31467" );
        crsList.add( "EPSG:4326" );
        crsList.add( "EPSG:4314" );
    }

    private String sourceCRS;
    private String targetCRS;
    
    public boolean execute( PlugInContext context ) throws Exception {

        layer = context.getSelectedLayer(0);   
              
        if( dialog == null ){
        	initDialog( context );
        }
        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
        
        if (!dialog.wasOKPressed()) {
            return false;
        }

        // SourceCRS und TargeCRS setzen    
        sourceCRS = dialog.getText("Source CRS");
        targetCRS = dialog.getText("Target CRS");
                
        return true;
    }
    
    public void run( TaskMonitor monitor, PlugInContext context ) throws Exception {
        if(sourceCRS.equals(targetCRS)) {
            return;
        }
        doTransform( sourceCRS, targetCRS, layer, context);
    }
    
    private void doTransform( String source, String target, Layer layer, PlugInContext context) throws Exception {
      
        FeatureCollection featureCollection = layer.getFeatureCollectionWrapper();
         
        FeatureCollection newFC = new FeatureDataset( featureCollection.getFeatureSchema() );
               
        List features = featureCollection.getFeatures();
            
        CoordinateSystem sourceCS = CRSFactory.create( source );
        CoordinateSystem targetCS = CRSFactory.create( target );
        GeoTransformer gt = new GeoTransformer(targetCS);
        
        for (Iterator iter = features.iterator(); iter.hasNext();) {
            Feature f = (Feature) iter.next();
            Feature fClone = f.clone( true ); 
            com.vividsolutions.jts.geom.Geometry g = fClone.getGeometry();
           
            Geometry geoObj = JTSAdapter.wrap( g );
            
            ( (GeometryImpl) geoObj).setCoordinateSystem( sourceCS );
            
            Geometry outputGeom = gt.transform( geoObj ); 
            g = JTSAdapter.export(outputGeom);
            fClone.setGeometry( g );
            newFC.add( fClone );
        }   
        
        // create new layer and add to category
        Layer newLayer = new Layer("Transformed: " + layer.getName() , 
        		layer.getLabelStyle().getColor().brighter(), newFC, context.getLayerManager());
        context.getLayerManager().addLayer("System" , newLayer);
       
    }
    
    public Icon getIcon() {
        return new IconLoader().icon("Atom.gif");
    }
    public void initialize( PlugInContext context ) throws Exception {
    	initDialog( context );
    }
    private void initDialog( PlugInContext context ) {
    	
        dialog = new MultiInputDialog(context.getWorkbenchFrame(), "Transformation", true);       
        
        dialog.addComboBox("Source CRS", ((ArrayList)crsList).get(0), crsList, "source crs");
        dialog.addComboBox("Target CRS", ((ArrayList)crsList).get(1), crsList, "target crs");

	}

	public EnableCheck createEnableCheck( WorkbenchContext workbenchContext ) {
		EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck())
                                     .add(checkFactory.createExactlyNLayerablesMustBeSelectedCheck( 1, Layerable.class));
    }
}

/* ********************************************************************
Changes to this class. What the people have been up to:
$Log$
Revision 1.1.2.3  2006/08/17 06:48:54  ut
pre-release2 2.0

Revision 1.1.2.2  2006/06/01 08:55:37  sn
*** empty log message ***

Revision 1.1.2.1  2006/05/31 10:04:36  ut
deejump pre deegree day

********************************************************************** */