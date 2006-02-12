
/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * JUMP is Copyright (C) 2003 Vivid Solutions
 *
 * This program implements extensions to JUMP and is
 * Copyright (C) 2004 Integrated Systems Analysts, Inc.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Integrated Systems Analysts, Inc.
 * 630C Anchors St., Suite 101
 * Fort Walton Beach, Florida
 * USA
 *
 * (850)862-7321
 * www.ashs.isa.com
 */


package org.openjump.core.ui.plugin.mousemenu;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JPopupMenu;

import org.openjump.core.geomutils.GeoUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.FMEGMLWriter;
import com.vividsolutions.jump.io.GMLWriter;
import com.vividsolutions.jump.io.JMLWriter;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.WKTWriter;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.SaveProjectAsPlugIn;

public class SaveDatasetsPlugIn extends AbstractPlugIn
{
	private static final String sSaveSelectedDatasets = I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Save-Selected-Datasets");
	private static final String sUseSaveDatasetAsToSaveLayer= I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Use***Save-Dataset-As***to-Save-Layer");
	private static final String sSavedLayer= I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Saved-Layer");
	private static final String sErrorSeeOutputWindow=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Error-See-Output-Window");
	private static final String sWarningSeeOutputWindow=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Warning-See-Output-Window");
	private static final String sCouldNotSaveLayer=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Could-not-save-layer");
	private static final String sCouldNotSave=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Could-not-save");
	private static final String sLayer=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.layer");
	private static final String sWithEmptyGeometry=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.with-empty-geometry");
	private static final String sWithMixedGeometryTypes=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.with-mixed-geometry-types");
	private static final String sCanNotSaveReadOnly=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Cannot-save-to-read-only-source-for-layer");
	private static final String sDidNotSaveSameFile=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Did-not-save-these-layers-since-they-would-have-to-be-saved-to-the-same-file");
	private static final String sSavedTask=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Saved-task");
	private static final String sFileName=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.File-Name");
	private static final String sLayerName=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Layer-Name");
	private static final String sUnrecognizedFileType=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Unrecognized-file-type");
	private static final String sNewLayerCreated=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.New-layer-created");
	private static final String sCouldNotWrite=I18N.get("org.openjump.core.ui.plugin.mousemenu.SaveDatasetsPlugIn.Could-not-write");
	
	private boolean saveAll = false;
	
	
    public void initialize(PlugInContext context) throws Exception
    {     
        WorkbenchContext workbenchContext = context.getWorkbenchContext();
        FeatureInstaller featureInstaller = new FeatureInstaller(workbenchContext);
        JPopupMenu layerNamePopupMenu = workbenchContext.getWorkbench()
                                                        .getFrame()
                                                        .getLayerNamePopupMenu();
        featureInstaller.addPopupMenuItem(layerNamePopupMenu,
            this, sSaveSelectedDatasets +"{pos:10}",
            false, null,
            SaveDatasetsPlugIn.createEnableCheck(workbenchContext));
    }
    
    public boolean execute(PlugInContext context) throws Exception
    {
        try
        {
        	boolean writeWarning = false;
            String newLine = "";
            context.getWorkbenchFrame().getOutputFrame().createNewDocument();

            LayerManager layerManager = context.getLayerManager();
            Collection layerCollection = layerManager.getLayers();
            
            //ensure all appropriate layers get projection files
            for (Iterator i = layerCollection.iterator(); i.hasNext();)
            {
            	writeProjectionFile(context, (Layer) i.next());
            }
            
            List layerList = new ArrayList();
                        
            if (saveAll)
            {
                layerCollection = layerManager.getLayersWithModifiedFeatureCollections();
                for (Iterator i = layerCollection.iterator(); i.hasNext();)
                	layerList.add((Layer) i.next());
            }
            else
            {
                layerCollection = (Collection) context.getWorkbenchContext().getLayerNamePanel().selectedNodes(Layer.class);
                for (Iterator i = layerCollection.iterator(); i.hasNext();)
                {
                	Layer layer = (Layer) i.next();
                	if (layer.isFeatureCollectionModified()) //just add modified layers
                		layerList.add(layer);
                }
            }
                        
            //remove any layers which have no data sources, ie, 
            //those that have not been previously saved
            for (int i = layerList.size() - 1; i >= 0; i--)
            {
            	Layer layer = (Layer) layerList.get(i);
        		DataSourceQuery dsq = layer.getDataSourceQuery();

        		if (dsq == null) //layer does not have a data source
        		{
        			context.getWorkbenchFrame().getOutputFrame().addText(sUseSaveDatasetAsToSaveLayer +": " + layer.getName());
        			writeWarning = true;
        			layerList.remove(i);
        			newLine = "\n";
        		}
            }
            
            //remove any layers which have read-only sources, ie, SdeDataSources
            for (int i = layerList.size() - 1; i >= 0; i--)
            {
            	Layer layer = (Layer) layerList.get(i);
        		DataSourceQuery dsq = layer.getDataSourceQuery();
        		
        		if (!dsq.getDataSource().isWritable()) //data source is read-only
        		{
        			context.getWorkbenchFrame().getOutputFrame().addText(newLine + sCanNotSaveReadOnly + ": " + layer.getName());
        			newLine = "";
        			writeWarning = true;
        			layerList.remove(i);
        		}
            }
            
            //remove any layers which have the same data source
            //since we don't want to overwrite earlier layers with later layers
            int currRec = 0;
            int lastRec = layerList.size() - 1;
            boolean writeHeader = true;
            
            while (currRec < lastRec)
            {
            	Layer currLayer = (Layer) layerList.get(currRec);
        		String currSource = currLayer.getDataSourceQuery().getDataSource().getProperties().get("File").toString();
        		String dupLayers = "\n" + sFileName + ": " + currSource + "\n" + sLayerName + ": " + currLayer.getName();
        		int numDups = 0;
        		int checkRec = currRec + 1;
        		
        		while (checkRec <= lastRec)
            	{
        			Layer checkLayer = (Layer) layerList.get(checkRec);
            		String checkSource = checkLayer.getDataSourceQuery().getDataSource().getProperties().get("File").toString();
            		if (currSource.equals(checkSource)) //found duplicate source
            		{
            			dupLayers = dupLayers + "\n" + sLayerName + ": " + checkLayer.getName();
            			layerList.remove(checkRec);
            			lastRec--;
            			numDups++;
            		}
            		else
            		{
            			checkRec++;
            		}
            	}
        		
        		if (numDups > 0)
        		{
        			if (writeHeader)
        			{
        				writeHeader = false;
            			writeWarning = true;
        				context.getWorkbenchFrame().getOutputFrame().addText(
						"\n" + sDidNotSaveSameFile + ":");
        				newLine = "\n";
        			}
        			context.getWorkbenchFrame().getOutputFrame().addText(dupLayers);
        			
          			layerList.remove(currRec);
        			lastRec--;
        		}
        		else
        		{
        			currRec++;
        		}
            }
            
            newLine = "";
            context.getWorkbenchFrame().getOutputFrame().addText(newLine);
            
            for (int i = 0; i < layerList.size(); i++)
            {
            	Layer layer = (Layer) layerList.get(i);
    			
    			if (WriteLayer(context, layer))
    			{
    				layer.setFeatureCollectionModified(false);
    				context.getWorkbenchFrame().getOutputFrame().addText(sSavedLayer + ": " + layer.getName());
    			}
            	else
            	{
    				context.getWorkbenchFrame().getOutputFrame().addText(sCouldNotSaveLayer + ": " + layer.getName());
            	}
            }
            
            if (writeWarning)
    			context.getWorkbenchFrame().warnUser( sCouldNotSaveLayer + " --- " + sErrorSeeOutputWindow);
            	
            if (saveAll)
            {
            	if (context.getTask().getProjectFile() != null)
            	{
            		new SaveProjectPlugIn(new SaveProjectAsPlugIn()).execute(context);
            		context.getWorkbenchFrame().getOutputFrame().addText("\n " + sSavedTask +": " + context.getTask().getProjectFile().getName());
            	}
            }
            
            return true;
        }
        catch (Exception e)
        {
            context.getWorkbenchFrame().warnUser(sErrorSeeOutputWindow);
            context.getWorkbenchFrame().getOutputFrame().addText("SaveDatasetsPlugIn Exception:" + e.toString());
            return false;
        }
    }
    
    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext)
    {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        
        return new MultiEnableCheck()
        .add(checkFactory.createWindowWithSelectionManagerMustBeActiveCheck())
        .add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1));
    }  
    
    public void setSaveAll()
    {
    	saveAll = true;
    }
    
    private boolean WriteLayer(PlugInContext context, Layer layer)
    {
    	String filename = layer.getDataSourceQuery().getDataSource().getProperties().get("File").toString();
    	DriverProperties dp = new DriverProperties();
    	dp.set("File", filename);
    	
    	try
		{
    		if ((filename.toLowerCase()).endsWith(".shp"))
    		{
    	    	String path = new File(filename).getParent() + "\\";
    			List newLayers = new ArrayList();
    			
    			if (!CompatibleFeatures(layer)) 
    				newLayers = splitLayer(context, layer);
    			
    			(new ShapefileWriter()).write(layer.getFeatureCollectionWrapper(), dp);
    			
    			for (int i = 0; i < newLayers.size(); i++)
    			{
    				Layer newLayer = (Layer) newLayers.get(i);
    				String newFileName = path + newLayer.getName() + ".shp";
    				HashMap properties = new HashMap();
    				properties.put(DataSource.COORDINATE_SYSTEM_KEY, "Unspecified");
    				properties.put(DataSource.FILE_KEY, newFileName);
    				DataSource dataSource = (DataSource) StandardReaderWriterFileDataSource.Shapefile.class.newInstance();
        			dataSource.setProperties(properties);
    				DataSourceQuery dataSourceQuery = new DataSourceQuery(dataSource, newLayer.getName(), null);
    				newLayer.setDataSourceQuery(dataSourceQuery).setFeatureCollectionModified(false);
    				dp.set("File", newFileName);
    				(new ShapefileWriter()).write(newLayer.getFeatureCollectionWrapper(), dp);
    				context.getWorkbenchFrame().getOutputFrame().addText(sSavedLayer + ": " + newLayer.getName());
    			}
    			
    			return true;
    		}
    		
    		if ((filename.toLowerCase()).endsWith(".jml"))
    		{
    			(new JMLWriter()).write(layer.getFeatureCollectionWrapper(), dp);
    			return true;
    		}
    		
    		if ((filename.toLowerCase()).endsWith(".gml"))
    		{
    			(new GMLWriter()).write(layer.getFeatureCollectionWrapper(), dp);
    			return true;
    		}
    		
    		if ((filename.toLowerCase()).endsWith(".fme"))
    		{
    			(new FMEGMLWriter()).write(layer.getFeatureCollectionWrapper(), dp);
    			return true;
    		}
    		
    		if ((filename.toLowerCase()).endsWith(".wkt"))
    		{
    			(new WKTWriter()).write(layer.getFeatureCollectionWrapper(), dp);
    			return true;
    		}
    		
    		context.getWorkbenchFrame().getOutputFrame().addText( sUnrecognizedFileType + " --- " + sCouldNotSaveLayer + ": " + layer.getName());
    		context.getWorkbenchFrame().warnUser(sErrorSeeOutputWindow);
    		return false;
		}
    	catch (Exception e)
		{
    		context.getWorkbenchFrame().warnUser(sErrorSeeOutputWindow);
    		context.getWorkbenchFrame().getOutputFrame().createNewDocument();
    		context.getWorkbenchFrame().getOutputFrame().addText("SaveDatasetsPlugIn:WriteLayer Exception:" + e.toString());
    		return false;
		}
    }
    
    private boolean CompatibleFeatures(Layer layer)
    {
        BitSet bitSet = new BitSet();        
        FeatureCollectionWrapper featureCollection = layer.getFeatureCollectionWrapper();
        List featureList = featureCollection.getFeatures();
        
        for (Iterator i = featureList.iterator(); i.hasNext();)
            bitSet = GeoUtils.setBit(bitSet, ((Feature) i.next()).getGeometry());

        return (bitSet.cardinality() < 2);
    }
    
    private List splitLayer(PlugInContext context, Layer layer)
    {
    	ArrayList newLayers = new ArrayList();
    	
    	if (!CompatibleFeatures(layer))
    	{
            ArrayList emptyFeatures = new ArrayList();
            ArrayList pointFeatures = new ArrayList();
            ArrayList lineFeatures = new ArrayList();
            ArrayList polyFeatures = new ArrayList();
            ArrayList groupFeatures = new ArrayList();
            
    		FeatureCollectionWrapper featureCollection = layer.getFeatureCollectionWrapper();
            List featureList = featureCollection.getFeatures();
            FeatureSchema fs = layer.getFeatureCollectionWrapper().getFeatureSchema();
            
            //first find and handle all empty geometries and GeometryCollections
            for (Iterator i = featureList.iterator(); i.hasNext();)
            {
                Feature feature = (Feature) i.next();
                Geometry geometry = feature.getGeometry();
                
                if (geometry.isEmpty()) //going to delete it
                	emptyFeatures.add(feature);
                else if ((geometry instanceof GeometryCollection) &&
                		(!(geometry instanceof MultiPoint)) &&
                		(!(geometry instanceof MultiLineString)) &&
                		(!(geometry instanceof MultiPolygon))) //mixed geometry; going to explode it
                	groupFeatures.add(feature);
            }   
            
			for (int i = 0; i < emptyFeatures.size(); i++) //delete empty geometries
            {
            	featureCollection.remove((Feature) emptyFeatures.get(i));
            }

			for (int i = 0; i < groupFeatures.size(); i++) //delete GeometryCollections
            {
				Feature feature = (Feature) groupFeatures.get(i);
				GeometryCollection geometry = (GeometryCollection) feature.getGeometry();
				explodeGeometryCollection(fs, pointFeatures, lineFeatures, polyFeatures, geometry);
            	featureCollection.remove(feature);
            }
			
			//now get new list of remaining features
			featureCollection = layer.getFeatureCollectionWrapper();
            featureList = layer.getFeatureCollectionWrapper().getFeatures();
            BitSet layerBit = new BitSet();
            if (featureList.size() > 0)
            {
            	Geometry firstGeo = ((Feature) featureList.iterator().next()).getGeometry();
            	layerBit = GeoUtils.setBit(layerBit, firstGeo); //this is the layer type
            }
            
			//now add just the exploded features that belong on the the original layer
            if (layerBit.get(GeoUtils.polyBit))
            {
				if (polyFeatures.size() > 0)
				{
					for (int i = 0; i < polyFeatures.size(); i++)
		            {
		            	Feature feature = (Feature) polyFeatures.get(i);
		            	featureCollection.add(feature);
		            }
					polyFeatures.clear();
				}
            }
            else if (layerBit.get(GeoUtils.lineBit))
            {
				if (lineFeatures.size() > 0)
				{
					for (int i = 0; i < lineFeatures.size(); i++)
		            {
		            	Feature feature = (Feature) lineFeatures.get(i);
		            	featureCollection.add(feature);
		            }
					lineFeatures.clear();
				}
            }
            else if (layerBit.get(GeoUtils.pointBit))
            {
				if (pointFeatures.size() > 0)
				{
					for (int i = 0; i < pointFeatures.size(); i++)
		            {
		            	Feature feature = (Feature) pointFeatures.get(i);
		            	featureCollection.add(feature);
		            }
					pointFeatures.clear();
				}
            }
            else //nothing left on layer; just pick a type for the layer
            {
				if (polyFeatures.size() > 0)
				{
					for (int i = 0; i < polyFeatures.size(); i++)
		            {
		            	Feature feature = (Feature) polyFeatures.get(i);
		            	featureCollection.add(feature);
		            }
					polyFeatures.clear();
				}
				else if (lineFeatures.size() > 0)
				{
					for (int i = 0; i < lineFeatures.size(); i++)
		            {
		            	Feature feature = (Feature) lineFeatures.get(i);
		            	featureCollection.add(feature);
		            }
	            	lineFeatures.clear();
				}
				else if (pointFeatures.size() > 0)
				{
					for (int i = 0; i < pointFeatures.size(); i++)
		            {
		            	Feature feature = (Feature) pointFeatures.get(i);
		            	featureCollection.add(feature);
		            }
	            	pointFeatures.clear();
				}
            }
            
            //at this point we have taken care of the GeometryCollections
            //some part of them have been added to the original layer
            //the rest of the features are in the array lists waiting
            //to be added to the appropriate layers
            
            featureCollection = layer.getFeatureCollectionWrapper();
            featureList = layer.getFeatureCollectionWrapper().getFeatures();
            layerBit = new BitSet();
            if (featureList.size() > 0)
            {
            	Geometry firstGeo = ((Feature) featureList.iterator().next()).getGeometry();
            	layerBit = GeoUtils.setBit(layerBit, firstGeo); //this is the layer type
            }

            FeatureSchema featureSchema = new FeatureSchema();
            featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
            Collection selectedCategories = context.getLayerNamePanel().getSelectedCategories();
            
            for (Iterator i = featureList.iterator(); i.hasNext();)
            {
            	Feature feature = (Feature) i.next();
            	Geometry geo = feature.getGeometry();
            	BitSet currFeatureBit = new BitSet();
            	currFeatureBit = GeoUtils.setBit(currFeatureBit, geo);
            	
            	if (!layerBit.get(GeoUtils.pointBit) && currFeatureBit.get(GeoUtils.pointBit))
            		pointFeatures.add(feature);
            	
            	if (!layerBit.get(GeoUtils.lineBit) && currFeatureBit.get(GeoUtils.lineBit))
            		lineFeatures.add(feature);
            	
            	if (!layerBit.get(GeoUtils.polyBit) && currFeatureBit.get(GeoUtils.polyBit))
            		polyFeatures.add(feature);
            }
            
            if (pointFeatures.size() > 0)
            {
		        Layer pointLayer = context.addLayer(selectedCategories.isEmpty()
		        ? StandardCategoryNames.WORKING
		        : selectedCategories.iterator().next().toString(), layer.getName() + "_point",
		        new FeatureDataset(featureSchema));
		        
		        FeatureCollectionWrapper pointFeatureCollection = pointLayer.getFeatureCollectionWrapper();
		        newLayers.add(pointLayer);
		        context.getWorkbenchFrame().getOutputFrame().addText(sNewLayerCreated + ": " + pointLayer.getName());
		    	context.getWorkbenchFrame().warnUser(sNewLayerCreated + " - " + sErrorSeeOutputWindow);
	           
				for (int i = 0; i < pointFeatures.size(); i++)
	            {
	            	Feature feature = (Feature) pointFeatures.get(i);
	            	featureCollection.remove(feature);
	            	pointFeatureCollection.add(feature);
	            }
           }
            
            if (lineFeatures.size() > 0)
            {
		        Layer lineLayer = context.addLayer(selectedCategories.isEmpty()
				? StandardCategoryNames.WORKING
				: selectedCategories.iterator().next().toString(), layer.getName() + "_line",
				new FeatureDataset(featureSchema));
				        
		        FeatureCollectionWrapper lineFeatureCollection = lineLayer.getFeatureCollectionWrapper();
		        newLayers.add(lineLayer);
				context.getWorkbenchFrame().getOutputFrame().addText(sNewLayerCreated + ": " + lineLayer.getName());
		    	context.getWorkbenchFrame().warnUser(sNewLayerCreated + " - " + sErrorSeeOutputWindow);
	           
				for (int i = 0; i < lineFeatures.size(); i++)
	            {
	            	Feature feature = (Feature) lineFeatures.get(i);
	            	featureCollection.remove(feature);
	            	lineFeatureCollection.add(feature);
	            }
           }
            
            if (polyFeatures.size() > 0)
            {
		        Layer polyLayer = context.addLayer(selectedCategories.isEmpty()
				? StandardCategoryNames.WORKING
				: selectedCategories.iterator().next().toString(), layer.getName() + "_area",
				new FeatureDataset(featureSchema));
				        
		        FeatureCollectionWrapper polyFeatureCollection = polyLayer.getFeatureCollectionWrapper();
		        newLayers.add(polyLayer);
				context.getWorkbenchFrame().getOutputFrame().addText(sNewLayerCreated + ": " + polyLayer.getName());
		    	context.getWorkbenchFrame().warnUser(sNewLayerCreated + " - " + sErrorSeeOutputWindow);
	           
				for (int i = 0; i < polyFeatures.size(); i++)
	            {
	            	Feature feature = (Feature) polyFeatures.get(i);
	            	featureCollection.remove(feature);
	            	polyFeatureCollection.add(feature);
	            }
           }
    	}
    	return newLayers;
    }
    
    private void explodeGeometryCollection(FeatureSchema fs, ArrayList pointFeatures, ArrayList lineFeatures, ArrayList polyFeatures, GeometryCollection geometryCollection)
    {
    	for (int i = 0; i < geometryCollection.getNumGeometries(); i++)
    	{
    		Geometry geometry = geometryCollection.getGeometryN(i);
    		
    		if (geometry instanceof GeometryCollection)
    		{
    			explodeGeometryCollection(fs, pointFeatures, lineFeatures, polyFeatures, (GeometryCollection) geometry);
    		}
    		else
    		{
    			Feature newFeature = new BasicFeature(fs);
    			newFeature.setGeometry(geometry);
    			BitSet featureBit = new BitSet();
    			featureBit = GeoUtils.setBit(featureBit, geometry);
    			if (featureBit.get(GeoUtils.pointBit)) pointFeatures.add(newFeature);
    			if (featureBit.get(GeoUtils.lineBit)) lineFeatures.add(newFeature);
    			if (featureBit.get(GeoUtils.polyBit)) polyFeatures.add(newFeature);
    		}
    	}
    }

    private void writeProjectionFile(PlugInContext context, Layer outputLayer)throws IOException, FileNotFoundException
	{	//per LDB projection files only associated with .shp files; confirmed 8/16/05
    	DataSourceQuery dsqOut = outputLayer.getDataSourceQuery();
    	
    	if (dsqOut != null) //file exists; not a new layer
    	{
    		String outputFileName = dsqOut.getDataSource().getProperties().get("File").toString();
    		if ((outputFileName.toLowerCase()).endsWith(".shp"))
    		{
    			String outputPrjFileName = "";
    			int pos = outputFileName.lastIndexOf('.');
    			outputPrjFileName = outputFileName.substring(0, pos) + ".prj";
    			
    			if (!(new File(outputPrjFileName).exists()))
    			{	//loop through all layers to find a project file; then copy contents
    				List layerList = context.getLayerManager().getLayers();
    				
    				for (Iterator i = layerList.iterator(); i.hasNext();)
    				{
    					Layer layer = (Layer) i.next();
    					DataSourceQuery dsq = layer.getDataSourceQuery();
    					if (dsq != null)
    					{
    						String inputFileName = dsq.getDataSource().getProperties().get("File").toString();
    						
    						if ((inputFileName.toLowerCase()).endsWith(".shp"))
    						{
    							String inputPrjFileName = "";
    							pos = inputFileName.lastIndexOf('.');
    							inputPrjFileName = inputFileName.substring(0, pos) + ".prj";
    							
    							if (new File(inputPrjFileName).exists())
    							{
    								List prjStr = FileUtil.getContents(inputPrjFileName);
    								try
									{
    									FileUtil.setContents(outputPrjFileName, prjStr);
									}
    								catch (IOException ex)
									{
    									context.getWorkbenchFrame().getOutputFrame().addText(sCouldNotWrite + ": " + outputPrjFileName + " --- " + ex.getMessage());
									}
    								break;
    							}
    						}
    					}
    				}
    			} 
    		}
    	}
	}
    
}
