
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

import javax.swing.JPopupMenu;

import org.openjump.core.geomutils.GeoUtils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.FMEGMLWriter;
import com.vividsolutions.jump.io.GMLWriter;
import com.vividsolutions.jump.io.JMLWriter;
import com.vividsolutions.jump.io.ShapefileWriter;
import com.vividsolutions.jump.io.WKTWriter;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

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
            context.getWorkbenchFrame().getOutputFrame().createNewDocument();
            Collection layerCollection = (Collection) context.getWorkbenchContext().getLayerNamePanel().selectedNodes(Layer.class);
            DriverProperties dp = new DriverProperties();
            
            for (Iterator j = layerCollection.iterator(); j.hasNext();)
            {
                Layer layer = (Layer) j.next();
                
                if (layer.isFeatureCollectionModified())
                {
                    DataSourceQuery dsq = layer.getDataSourceQuery();
                    
                    if (dsq == null) //no file exists; must be a new layer
                    {
                        context.getWorkbenchFrame().getOutputFrame().addText(sUseSaveDatasetAsToSaveLayer + "  " + layer.getName());
                    }
                    else
                    {
                        String fname = dsq.getDataSource().getProperties().get("File").toString();
                        
                        if (WriteLayer(context, layer))
                        {
                            layer.setFeatureCollectionModified(false);
                            context.getWorkbenchFrame().getOutputFrame().addText(sSavedLayer +" " + layer.getName());
                        }
//                        else
//                        {
//                            context.getWorkbenchFrame().getOutputFrame().addText("Could not save layer: " + layer.getName());
//                            context.getWorkbenchFrame().warnUser("Warning: see output window");
//                        }
                    }
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
    
    private boolean WriteLayer(PlugInContext context, Layer layer)
    {
        String filename = layer.getDataSourceQuery().getDataSource().getProperties().get("File").toString();
        DriverProperties dp = new DriverProperties();
        dp.set("File", filename);

        try
        {
        if ((filename.toLowerCase()).endsWith(".shp"))
        {
            if (CompatibleFeatures(layer))
            {
                (new ShapefileWriter()).write(layer.getFeatureCollectionWrapper(), dp);
                return true;
            }
            else
            {
                WriteIncompatibleList(context, layer);
                context.getWorkbenchFrame().warnUser(sWarningSeeOutputWindow);
                return false;
            }
        }

//        if ((filename.toLowerCase()).endsWith(".cgd"))
//        {
//            //RFL(new CGDEFWriter()).write(layer.getFeatureCollectionWrapper(), dp);
//            return true;
//        }
//        
//        if ((filename.toLowerCase()).endsWith(".cgdef"))
//        {
//            //RFL(new CGDEFWriter()).write(layer.getFeatureCollectionWrapper(), dp);
//            return true;
//        }
        
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
        
        context.getWorkbenchFrame().getOutputFrame().addText(sCouldNotSaveLayer + " " + layer.getName());
        context.getWorkbenchFrame().warnUser(sWarningSeeOutputWindow);
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
            bitSet = new GeoUtils().setBit(bitSet, ((Feature) i.next()).getGeometry());

        return (bitSet.cardinality() < 2);
    }
    
//    private BitSet setBit(BitSet bitSet, Geometry geometry)
//    {
//        if      (geometry.isEmpty())                  bitSet.set(0);
//        else if (geometry instanceof Point)           bitSet.set(1);
//        else if (geometry instanceof MultiPoint)      bitSet.set(1);
//        else if (geometry instanceof LineString)      bitSet.set(2);
//        else if (geometry instanceof LinearRing)      bitSet.set(2);
//        else if (geometry instanceof MultiLineString) bitSet.set(2);
//        else if (geometry instanceof Polygon)         bitSet.set(3);
//        else if (geometry instanceof MultiPolygon)    bitSet.set(3);
//        else if (geometry instanceof GeometryCollection)
//        {
//            GeometryCollection geometryCollection = (GeometryCollection) geometry;
//            for (int i = 0; i < geometryCollection.getNumGeometries(); i++)
//                bitSet = setBit(bitSet, geometryCollection.getGeometryN(i));
//        }
//        return bitSet;
//    }
    
    private void WriteIncompatibleList(PlugInContext context, Layer layer)
    {    
        FeatureCollectionWrapper featureCollection = layer.getFeatureCollectionWrapper();
        List featureList = featureCollection.getFeatures();
        Geometry firstGeo = ((Feature) featureList.iterator().next()).getGeometry();
        BitSet layerBit = new BitSet();
        layerBit = new GeoUtils().setBit(layerBit, firstGeo);
        
        String layerType = "";
        if (firstGeo.isEmpty())
            layerType = "Empty";
        else
            layerType = firstGeo.getGeometryType();
 
        if (layerType.equalsIgnoreCase("Empty"))
        {
            context.getWorkbenchFrame().getOutputFrame().addText(sCouldNotSaveLayer + " " + layer.getName() + " " + sWithEmptyGeometry);
        }
        else if (layerBit.cardinality() > 1)
        {
            context.getWorkbenchFrame().getOutputFrame().addText(sCouldNotSave + " " + layerType + " " + sLayer + " " + layer.getName() + " " + sWithMixedGeometryTypes);
       }
        else
        {
            context.getWorkbenchFrame().getOutputFrame().addText(sCouldNotSave + " " + layerType + " " + sLayer + " " + layer.getName());
            
            for (Iterator i = featureList.iterator(); i.hasNext();)
            {
                Feature feature = (Feature) i.next();
                Geometry geometry = feature.getGeometry();
                BitSet geoBits = (BitSet) layerBit.clone();
                geoBits = new GeoUtils().setBit(geoBits, geometry);
                
                String geoType = "";
                if (geometry.isEmpty())
                   geoType = "Empty";
                else
                    geoType = geometry.getGeometryType();
                
                if (geoBits.cardinality() > 1)
                {
                    context.getWorkbenchFrame().getOutputFrame().addText(" FID = " + feature.getID() + " (" + geoType + ")");
                }
            }            
        }
    }
}
