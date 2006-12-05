package it.ama_mi.sis.framework.db.openjump;

import it.ama_mi.sis.framework.db.DBData;
import it.ama_mi.sis.framework.db.col.DBColContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.datastore.DataStoreConnection;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.IndexedFeatureCollection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.tools.AttributeMapping;
import com.vividsolutions.jump.workbench.datastore.ConnectionDescriptor;
import com.vividsolutions.jump.workbench.datastore.ConnectionManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.GenericNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.datastore.DataStoreDataSource;


public class SISNodeOnArcPlugIn extends ThreadedBasePlugIn {

	private static Class CLAZZ = SISNodeOnArcPlugIn.class;
	
	private static SISI18N i18n = SISI18N.getInstance();
	
	private final static String LAYER1 = GenericNames.LAYER_A;
	private final static String LAYER2 = GenericNames.LAYER_B;

	private static String MAINMENUNAME = i18n.get(CLAZZ,"main-menu");	
	private static String TWOLAYERS = i18n.get(CLAZZ,"two-layers");
	
  private Layer layer1, layer2;
  
  private Map layerProp1;
  private Map layerProp2;
  
  private boolean exceptionThrown = false;

  public SISNodeOnArcPlugIn()
  {
  }

	public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuItem(
    		this,
    		new String[] { MAINMENUNAME, TWOLAYERS }, 
    		this.getName(), false, null, 
    		new MultiEnableCheck().add(
    				new EnableCheckFactory(context.getWorkbenchContext()).
    				createTaskWindowMustBeActiveCheck()).add(
    						new EnableCheckFactory(context.getWorkbenchContext()).
    						createAtLeastNLayersMustExistCheck(2))
		); 
  }

  public boolean execute(PlugInContext context) throws Exception {
    MultiInputDialog dialog = new MultiInputDialog(
    		context.getWorkbenchFrame(), getName(), true);
    setDialogValues(dialog, context);
    GUIUtil.centreOnWindow(dialog);
    dialog.setVisible(true);
    if (! dialog.wasOKPressed()) { return false; }
    getDialogValues(dialog);
    
    return true;
  }

	  public String getName(){
	  	return i18n.get(CLAZZ,"Node-on-arc");
	  }
	  
	  public void run(TaskMonitor monitor, PlugInContext context)
	      throws Exception
	  {
/*	  	
	    FeatureSchema featureSchema = new FeatureSchema();
	    
	   
	    FeatureCollection resultColl = runSpatialJoinMethod(layer1.getFeatureCollectionWrapper(),
	        layer2.getFeatureCollectionWrapper(),
	        "");
	  if (resultColl.size()>0)
	    context.addLayer(StandardCategoryNames.WORKING, I18N.get("org.openjump.sigle.plugin.SpatialJoinPlugIn.Result") + 
	    		"", resultColl);
	    if (exceptionThrown)
	      context.getWorkbenchFrame().warnUser(I18N.get("org.openjump.sigle.plugin.SpatialJoinPlugIn.Error-while-executing-spatial-function")); */
	  	
	  	layerProp1 = checkLayer(layer1);
	  	layerProp2 = checkLayer(layer2);
	  	
	  	SISDBDSConnection connection1 = getConnection(context,layerProp1);
	  	SISDBDSConnection connection2 = getConnection(context,layerProp2);
	  	
	  	DBColContext context1 = getContext(layerProp1);
	  	DBColContext context2 = getContext(layerProp2);
	  	
	  	DBData data1 = connection1.getData();
	  	DBData data2 = connection2.getData();
	  	
	  	//vedere AddDatastoreLayerPlugIn.createLayer() per un esempio di come creare
	  	//un layer basato su un DataStore
	  	//la cosa giusta da fare, forse, sarebbe creare un OpenJUMPDBDataImpl,
	  	//in modo da poter poi usare la FeedCopyOperation direttamente verso un Layer.
	  	//in questo modo si potrebbe riutilizzare in pratica tutto il codice di 
	  	//NodeOnArcTest facendogli scrivere il risultato o nel DB o in un Layer.
	  	//Inoltre se OpenJUMPDBDataImpl fosse in grado anche di leggere da un Layer
	  	//o da una FeatureCollection, sarebbe poi possibile usarlo anche per il Save As
	  	//attraverso SISDB!!!
	  	
	  	//Ma poi non so se ha molto senso farlo cosi'?!? In fondo poi non utilizzo
	  	//veramente il contenuto dei layer per questa elaborazione, ma solo i loro
	  	//parametri, perche' poi in realta' mi connetto direttamente al DB per eseguire,
	  	//quindi quello che dovrei fare e' invocare un pannello simile a quello di
	  	//AddDatastoreLayerPlugIn, ma e' piu' complicato, vedremo...!!!
	  }

	  private DBColContext getContext(Map layerProp) {
	  	String datasetName = (String)layerProp.get(
  				DataStoreDataSource.DATASET_NAME_KEY);
	  	
				//The dataset name inside the query may be in the form of schemaName.tableName
				//so separate them and use them accordingly
			return DBColContext.valueOf(datasetName);
	  }

		private SISDBDSConnection getConnection(PlugInContext context, Map layerProp) {
  		ConnectionDescriptor descr = (ConnectionDescriptor)layerProp.get(
  				DataStoreDataSource.CONNECTION_DESCRIPTOR_KEY);
	  	DataStoreConnection res = ConnectionManager.instance(
	  			context.getWorkbenchContext()).getConnection(descr);
	  	
	  	return (SISDBDSConnection)res;
	  }

		private Map checkLayer(Layer layer) {
	  	DataSource dataSource = layer.getDataSourceQuery().getDataSource();
	  	if( dataSource instanceof DataStoreDataSource ) {
	  		Map map = ((DataStoreDataSource)dataSource).getProperties();
	  		ConnectionDescriptor descr = (ConnectionDescriptor)map.get(
	  				DataStoreDataSource.CONNECTION_DESCRIPTOR_KEY);
	  		if( SISDBDataStoreDriver.class.getName().equals(
	  				descr.getDataStoreDriverClassName()) )
	  			return map;					//yes, it's a good layer
	  	}
	  	
  		throw new RuntimeException(i18n.getMessage(CLAZZ,
  				"Not-a-SISDB-Layer: %s",new Object[] { String.valueOf(layer)}));
		}

		private FeatureCollection runSpatialJoinMethod(FeatureCollection fcA,
	                                     FeatureCollection fcB,
	                                     String methodName
	                                     )
	  {
	    exceptionThrown = false;
	    FeatureCollection resultFC;
	    Feature fEqual = null;
	    Feature fWithin = null;
	    Feature fEqualAndWithin = null;
	    Feature fEqualOrWithin = null;
	    
    	AttributeMapping mapping = null;
	    
    	mapping = new AttributeMapping(new FeatureSchema(), new FeatureSchema());
        List aFeatures = null;
        mapping = new AttributeMapping(fcB.getFeatureSchema(), fcA.getFeatureSchema());
        aFeatures = fcA.getFeatures();
	    
        FeatureDataset fcRecup = new FeatureDataset(mapping.createSchema("GEOMETRY"));
        IndexedFeatureCollection indexedB = new IndexedFeatureCollection(fcB);
        
        for (int i = 0; (i < aFeatures.size());i++) {
        	Feature aFeature = (Feature) aFeatures.get(i);
        	Feature feature = new BasicFeature(fcRecup.getFeatureSchema());
        	int nbFeatureEqual = 0;
        	int nbFeatureWithin = 0;
        	int nbFeatureEqualAndWithin = 0;
        	int nbFeatureEqualOrWithin=0;
        	int nbFeature =0;
        	        	
        	for (Iterator j = indexedB.query(aFeature.getGeometry().getEnvelopeInternal()).iterator();
        		j.hasNext();) {
/*        	    
        	    Feature bFeature = (Feature) j.next();
        	    if (methodName.equals(METHOD_EQUAL)) {
        	    	if (aFeature.getGeometry().equals(bFeature.getGeometry())) {
		        	nbFeatureEqual++;
		        	nbFeature++;
					fEqual = bFeature;
        	    	}
        	}
        	    
        	    else if (methodName.equals(METHOD_WITHIN)) {
        	    	if (aFeature.getGeometry().within(bFeature.getGeometry())) {
		        	nbFeatureWithin++;
		        	nbFeature++;
					fEqual = bFeature;
        	    	}
        	}
        	    
        	   else if (methodName.equals(METHOD_EQUAL_AND_WITHIN)) {
        	    	if ((aFeature.getGeometry().equals(bFeature.getGeometry()))&& ((aFeature.getGeometry().within(bFeature.getGeometry())))) {
		        	nbFeatureEqualAndWithin++;
		        	nbFeature++;
					fEqualAndWithin = bFeature;
        	    	}
        	}
        	   
        	   else if (methodName.equals(METHOD_EQUAL_OR_WITHIN)) {
    	    	if ((aFeature.getGeometry().equals(bFeature.getGeometry()))|| ((aFeature.getGeometry().within(bFeature.getGeometry())))) {
	        	nbFeatureEqualOrWithin++;
	        	nbFeature++;
				fEqualOrWithin = bFeature;
    	    	} 
    	} */
        	}
	        // on ne transfere les attributs que lorsque la geometry resultat 
        	// n'est contenue que une seule geometry source
	        if (nbFeatureEqual == 1) {
	        	mapping.transferAttributes(fWithin, aFeature, feature);
	        	feature.setGeometry((Geometry) aFeature.getGeometry().clone()); 
		        fcRecup.add(feature);
	        }
	        
	        else if (nbFeatureWithin == 1){
	        	mapping.transferAttributes(fEqual, aFeature, feature);
	        	feature.setGeometry((Geometry) aFeature.getGeometry().clone()); 
		        fcRecup.add(feature);
	        }
	        
	        else if (nbFeatureWithin == 1){
	        	mapping.transferAttributes(fEqual, aFeature, feature);
	        	feature.setGeometry((Geometry) aFeature.getGeometry().clone()); 
		        fcRecup.add(feature);
	        }
	        
	        else if (nbFeatureEqualAndWithin == 1){
	        	mapping.transferAttributes(fEqual, aFeature, feature);
	        	feature.setGeometry((Geometry) aFeature.getGeometry().clone()); 
		        fcRecup.add(feature);
	        }
	        
	        else if (nbFeatureEqualOrWithin == 1){
	        	mapping.transferAttributes(fEqual, aFeature, feature);
	        	feature.setGeometry((Geometry) aFeature.getGeometry().clone()); 
		        fcRecup.add(feature);
	        }
	        	        
	        // on clone la geometry pour que les modifs sur la geometry source 
	        // ne soient pas transferees sur la geometry resultat
	        //feature.setGeometry((Geometry) aFeature.getGeometry().clone()); 
	       // fcRecup.add(feature);
	        
	    }
	    return fcRecup;
	  }
  

	  private void setDialogValues(MultiInputDialog dialog, PlugInContext context)
	  {
	    //dialog.setSideBarImage(new ImageIcon(getClass().getResource("DiffSegments.png")));
	    dialog.setSideBarDescription(i18n.get(CLAZZ,
	    	"Link-node-on-arc"));
	    //Set initial layer values to the first and second layers in the layer list.
	    //In #initialize we've already checked that the number of layers >= 2. [Jon Aquino]
	    dialog.addLayerComboBox(LAYER1, layer1, context.getLayerManager());
	    dialog.addLayerComboBox(LAYER2, layer2, context.getLayerManager());
	  }

	  private void getDialogValues(MultiInputDialog dialog) {
	    layer1 = dialog.getLayer(LAYER1);
	    layer2 = dialog.getLayer(LAYER2);
	  }
     
}


