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
 */

package org.openjump.core.ui.plugin.tools;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;

import org.openjump.core.geomutils.GeoUtils;

import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.UniqueCoordinateArrayFilter;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;

public class ConvexHullPlugIn extends AbstractPlugIn {
    private WorkbenchContext workbenchContext;
    private final static String TOLERANCE = I18N.get("org.openjump.core.ui.plugin.tools.ConvexHullPlugIn.Tolerance");
    private MultiInputDialog dialog;
    private double blendTolerance = 0.1;
    private boolean exceptionThrown = false;
    final static String sJoin = I18N.get("org.openjump.core.ui.plugin.tools.ConvexHullPlugIn.Join");
    final static String sConvexHull = I18N.get("org.openjump.core.ui.plugin.tools.ConvexHullPlugIn.Convex-Hull");

    public void initialize(PlugInContext context) throws Exception
    {     
        workbenchContext = context.getWorkbenchContext();
        context.getFeatureInstaller().addMainMenuItem(this, new String[] {MenuNames.TOOLS, sJoin }, getName(), false, null, this.createEnableCheck(workbenchContext));

//        workbenchContext = context.getWorkbenchContext();
//        FeatureInstaller featureInstaller = new FeatureInstaller(workbenchContext);
//        JPopupMenu popupMenu = workbenchContext.getLayerViewPanel().popupMenu();
//        featureInstaller.addPopupMenuItem(popupMenu,
//            this, "Create Convex Hull",
//            false, null,  
//            this.createEnableCheck(workbenchContext));
    }
    
    public boolean execute(final PlugInContext context) throws Exception
    {
        reportNothingToUndoYet(context);
        Collection selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(); 
        Collection selectedCategories = context.getLayerNamePanel().getSelectedCategories();
        LayerManager layerManager = context.getLayerManager();
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        CoordinateList coords = new CoordinateList();
        
        
        for (Iterator j = selectedFeatures.iterator(); j.hasNext();)
        {
            Feature f = (Feature) j.next();
            Geometry geo = f.getGeometry();
            UniqueCoordinateArrayFilter filter = new UniqueCoordinateArrayFilter();
            geo.apply(filter);
            coords.add( filter.getCoordinates() ,false);
         }
         
        CoordinateList convexHullCoords = (new GeoUtils()).ConvexHullWrap( coords );
        LineString convexHull = new GeometryFactory().createLineString(convexHullCoords.toCoordinateArray());

        Feature newFeature = new BasicFeature(featureSchema);
        newFeature.setGeometry(convexHull);
        FeatureDataset newFeatures = new FeatureDataset(featureSchema);       
        newFeatures.add(newFeature);
        
        layerManager.addLayer(selectedCategories.isEmpty()
        ? StandardCategoryNames.WORKING
        : selectedCategories.iterator().next().toString(),
        layerManager.uniqueLayerName(sConvexHull),
        newFeatures);
        
        layerManager.getLayer(0).setFeatureCollectionModified(true);
        layerManager.getLayer(0).setEditable(true);
        
        return true;
    }
           
    public MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck()
            .add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck())
            .add(checkFactory.createAtLeastNFeaturesMustHaveSelectedItemsCheck(1))
            .add(new EnableCheck() {
            public String check(JComponent component) {
                Collection featuresWithSelectedItems =
                    workbenchContext
                        .getLayerViewPanel()
                        .getSelectionManager()
                        .getFeaturesWithSelectedItems();
                return null;
            }
        });
    }    
}
