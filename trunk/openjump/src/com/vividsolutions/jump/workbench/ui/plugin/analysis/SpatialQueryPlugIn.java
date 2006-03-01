
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
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;

/**
* Queries a layer by a spatial predicate.
*/
public class SpatialQueryPlugIn
    extends AbstractPlugIn
    implements ThreadedPlugIn
{
  private final static String MASK_LAYER = "Mask Layer";
  private final static String SRC_LAYER = "Source Layer";
  private final static String PREDICATE = "Relation";
  private final static String PARAM = "Parameter";
  private final static String DIALOG_COMPLEMENT = "Complement Result";

  private Collection functionNames;
  private MultiInputDialog dialog;
  private Layer maskLyr, srcLayer;
  private String funcNameToRun;
  private GeometryPredicate functionToRun = null;
  private boolean complementResult = false;
  private boolean exceptionThrown = false;

  private Geometry geoms[] = new Geometry[2];
  private double[] params = new double[2];

  public SpatialQueryPlugIn()
  {
    functionNames = GeometryPredicate.getNames();
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
    if (functionToRun == null) return;
    if (maskLyr == null) return;
    if (srcLayer == null) return;

    monitor.report("Executing query " + functionToRun.getName() + "...");

    FeatureCollection maskFC = maskLyr.getFeatureCollectionWrapper();
    FeatureCollection sourceFC = srcLayer.getFeatureCollectionWrapper();

    int nArgs = functionToRun.getGeometryArgumentCount();

    SpatialQueryExecuter executer = new SpatialQueryExecuter(maskFC, sourceFC);
    executer.setComplementResult(complementResult);
    FeatureCollection resultFC = executer.getResultFC();
    executer.execute(monitor, functionToRun, params, resultFC);

    if (monitor.isCancelRequested())
      return;

    context.getLayerManager().addCategory(StandardCategoryNames.RESULT, 0);
    // this will happen if plugin was cancelled
    context.addLayer(StandardCategoryNames.RESULT, "Query-" + funcNameToRun, resultFC);
    if (exceptionThrown)
      context.getWorkbenchFrame().warnUser("Errors found while executing query");
  }

  private JTextField paramField;

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context)
  {
    //dialog.setSideBarImage(new ImageIcon(getClass().getResource("DiffSegments.png")));
    dialog.setSideBarDescription(
        "Finds the Source features which have a given spatial relationship to some feature in the Mask layer"
        + " (i.e. where Source.Relationship(Mask) = true)");
    //Set initial layer values to the first and second layers in the layer list.
    //In #initialize we've already checked that the number of layers >= 1. [Jon Aquino]
    dialog.addLayerComboBox(SRC_LAYER, srcLayer, context.getLayerManager());
    JComboBox functionComboBox = dialog.addComboBox(PREDICATE, funcNameToRun, functionNames, null);
    functionComboBox.addItemListener(new MethodItemListener());
    dialog.addLayerComboBox(MASK_LAYER, maskLyr, context.getLayerManager());

    paramField = dialog.addDoubleField(PARAM, params[0], 10);
    dialog.addCheckBox(DIALOG_COMPLEMENT, false);

    updateUIForFunction(funcNameToRun);
  }

  private void getDialogValues(MultiInputDialog dialog) {
    maskLyr = dialog.getLayer(MASK_LAYER);
    srcLayer = dialog.getLayer(SRC_LAYER);
    funcNameToRun = dialog.getText(PREDICATE);
    functionToRun = GeometryPredicate.getPredicate(funcNameToRun);
    params[0] = dialog.getDouble(PARAM);
    complementResult = dialog.getBoolean(DIALOG_COMPLEMENT);
  }

  private void updateUIForFunction(String funcName)
  {
    boolean paramUsed = false;
    GeometryPredicate func = GeometryPredicate.getPredicate(funcName);
    if (func != null) {
      paramUsed = func.getParameterCount() > 0;
    }
    paramField.setEnabled(paramUsed);
    // this has the effect of making the background gray (disabled)
    paramField.setOpaque(paramUsed);
  }

  private class MethodItemListener
      implements ItemListener
  {
    public void itemStateChanged(ItemEvent e) {
      updateUIForFunction((String) e.getItem());
    }
  }

}



