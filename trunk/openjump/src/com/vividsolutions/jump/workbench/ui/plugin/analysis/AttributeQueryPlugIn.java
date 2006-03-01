/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
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
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jump.workbench.ui.plugin.analysis;

import java.util.*;

import java.awt.Color;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;

/**
* Queries a layer by a spatial predicate.
*/
public class AttributeQueryPlugIn
    extends AbstractPlugIn
    implements ThreadedPlugIn
{
  private final static String ATTR_GEOMETRY_AREA = "Geometry.Area";
  private final static String ATTR_GEOMETRY_LENGTH = "Geometry.Length";
  private final static String ATTR_GEOMETRY_NUMPOINTS = "Geometry.NumPoints";
  private final static String ATTR_GEOMETRY_NUMCOMPONENTS = "Geometry.NumComponents";
  private final static String ATTR_GEOMETRY_TYPE = "Geometry.Type";

  private final static String LAYER = "Source Layer";
  private final static String ATTRIBUTE = "Attribute";
  private final static String PREDICATE = "Relation";
  private final static String VALUE = "Value";
  // MD - could easily add this later
  //private final static String DIALOG_COMPLEMENT = "Complement Result";

  private Collection functionNames;
  private MultiInputDialog dialog;
  private Layer srcLayer;
  private String attrName;
  private String funcNameToRun;
  private String value;
  private boolean complementResult = false;
  private boolean exceptionThrown = false;

  public AttributeQueryPlugIn()
  {
    functionNames = AttributePredicate.getNames();
  }

  /*
  // MD - for some reason this is now done in JUMPConfiguration
    public void initialize(PlugInContext context) throws Exception {
      context.getFeatureInstaller().addMainMenuItem(
          this, "Tools", "Find Unaligned Segments...", null, new MultiEnableCheck()
        .add(context.getCheckFactory().createWindowWithLayerNamePanelMustBeActiveCheck())
          .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(1)));
    }
  */

  public boolean execute(PlugInContext context) throws Exception {
    dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), getName(), true);
    setDialogValues(dialog, context);
    GUIUtil.centreOnWindow(dialog);
    dialog.setVisible(true);
    if (! dialog.wasOKPressed()) { return false; }
    getDialogValues(dialog);
    return true;
  }

  public void run(TaskMonitor monitor, PlugInContext context)
      throws Exception
  {
    monitor.allowCancellationRequests();

    // input-proofing
    if (value == null) return;
    if (srcLayer == null) return;
    if (attrName == null) return;

    monitor.report("Executing query...");

    FeatureCollection sourceFC = srcLayer.getFeatureCollectionWrapper();


//    SpatialQueryExecuter executer = new SpatialQueryExecuter(maskFC, sourceFC);
//    executer.setComplementResult(complementResult);
//    FeatureCollection resultFC = executer.getResultFC();
//    executer.execute(monitor, functionToRun, params, resultFC);

    if (monitor.isCancelRequested())
      return;

    FeatureCollection resultFC = executeQuery(sourceFC, attrName, value);
    // this will happen if plugin was cancelled
    context.getLayerManager().addCategory(StandardCategoryNames.RESULT, 0);
    context.addLayer(StandardCategoryNames.RESULT, "Query-" + funcNameToRun, resultFC);
    if (exceptionThrown)
      context.getWorkbenchFrame().warnUser("Errors found while executing query");
  }

  private FeatureCollection executeQuery(FeatureCollection sourceFC, String attrName,
      String value)
  {
    AttributePredicate pred = AttributePredicate.getPredicate(funcNameToRun);
    FeatureCollection resultFC = new FeatureDataset(sourceFC.getFeatureSchema());
    for (Iterator i = sourceFC.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      Object fVal = getValue(f, attrName);
//      Object fVal = f.getAttribute(attrName);

      if (pred.isTrue(fVal, value))
      //if (fVal.equals(value))
        resultFC.add(f.clone(true));
    }
    return resultFC;
  }

  private Object getValue(Feature f, String attrName)
  {
    if (attrName == ATTR_GEOMETRY_AREA) {
      Geometry g = f.getGeometry();
      double area = (g == null) ? 0.0 : g.getArea();
      return new Double(area);
    }
    if (attrName == ATTR_GEOMETRY_LENGTH) {
      Geometry g = f.getGeometry();
      double len = (g == null) ? 0.0 : g.getLength();
      return new Double(len);
    }
    if (attrName == ATTR_GEOMETRY_NUMPOINTS) {
      Geometry g = f.getGeometry();
      double len = (g == null) ? 0.0 : g.getNumPoints();
      return new Double(len);
    }
    if (attrName == ATTR_GEOMETRY_NUMCOMPONENTS) {
        Geometry g = f.getGeometry();
        double len = (g == null) ? 0.0 : g.getNumGeometries();
        return new Double(len);
      }
    if (attrName == ATTR_GEOMETRY_TYPE) {
        Geometry g = f.getGeometry();
        return StringUtil.classNameWithoutQualifiers(g.getClass().getName());
      }
    return f.getAttribute(attrName);
  }

  private JComboBox attrComboBox;

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context)
  {
    //dialog.setSideBarImage(new ImageIcon(getClass().getResource("DiffSegments.png")));
    dialog.setSideBarDescription(
        "Finds the Source features which have attribute values satisfying a given condition");

    //Set initial layer values to the first and second layers in the layer list.
    //In #initialize we've already checked that the number of layers >= 1. [Jon Aquino]
    JComboBox lyrCombo = dialog.addLayerComboBox(LAYER, srcLayer, context.getLayerManager());
    lyrCombo.addItemListener(new LayerItemListener());
    attrComboBox = dialog.addComboBox(ATTRIBUTE, attrName, functionNames, null);
    dialog.addComboBox(PREDICATE, funcNameToRun, functionNames, null);

    dialog.addTextField(VALUE, value, 10, null, null);
    //dialog.addCheckBox(DIALOG_COMPLEMENT, false);

    updateUI(srcLayer);
  }

  private void getDialogValues(MultiInputDialog dialog) {
    srcLayer = dialog.getLayer(LAYER);
    attrName = dialog.getText(ATTRIBUTE);
    funcNameToRun = dialog.getText(PREDICATE);
    value = dialog.getText(VALUE);
    //complementResult = dialog.getBoolean(DIALOG_COMPLEMENT);
  }

  private void updateUI(Layer lyr)
  {
    List attrNames = null;
    if (lyr != null) {
      FeatureCollection fc = lyr.getFeatureCollectionWrapper();
      FeatureSchema fs = fc.getFeatureSchema();

      attrNames = getAttributeNames(fs);
    }
    else {
      attrNames = new ArrayList();
    }
    attrComboBox.setModel(new DefaultComboBoxModel(new Vector(attrNames)));
  }

  private static List getAttributeNames(FeatureSchema fs)
  {
    List names = new ArrayList();
    for (int i = 0; i < fs.getAttributeCount(); i++) {
      if (fs.getAttributeType(i) != AttributeType.GEOMETRY)
        names.add(fs.getAttributeName(i));
    }
    names.add(ATTR_GEOMETRY_AREA);
    names.add(ATTR_GEOMETRY_LENGTH);
    names.add(ATTR_GEOMETRY_NUMPOINTS);
    names.add(ATTR_GEOMETRY_NUMCOMPONENTS);
    names.add(ATTR_GEOMETRY_TYPE);

    return names;
  }

  private class LayerItemListener
      implements ItemListener
  {
    public void itemStateChanged(ItemEvent e) {
      updateUI((Layer) e.getItem());
    }
  }

}



