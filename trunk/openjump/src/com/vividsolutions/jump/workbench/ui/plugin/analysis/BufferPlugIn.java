
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
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;


public class BufferPlugIn
    extends AbstractPlugIn
    implements ThreadedPlugIn
{
  private final static String LAYER = "";
  private final static String DISTANCE = "";

  private MultiInputDialog dialog;
  private Layer layer;
  private double bufferDistance = 1.0;
  private boolean exceptionThrown = false;

  public BufferPlugIn()
  {
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

  public void run(TaskMonitor monitor, PlugInContext context)
      throws Exception
  {
    FeatureSchema featureSchema = new FeatureSchema();
    featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
    FeatureCollection resultFC = new FeatureDataset(featureSchema);

    Collection resultColl = runBuffer(layer.getFeatureCollectionWrapper());
    resultFC = FeatureDatasetFactory.createFromGeometry(resultColl);
    context.addLayer(StandardCategoryNames.WORKING, "Buffer-" + layer.getName(), resultFC);
    if (exceptionThrown)
      context.getWorkbenchFrame().warnUser(I18N.get("ui.plugin.analysis.BufferPlugIn.errors-found-while-executing-buffer"));
  }

  private Collection runBuffer(FeatureCollection fcA)
  {
    exceptionThrown = false;
    Collection resultColl = new ArrayList();
    for (Iterator ia = fcA.iterator(); ia.hasNext(); ) {
      Feature fa = (Feature) ia.next();
      Geometry ga = fa.getGeometry();
      Geometry result = runBuffer(ga);
      if (result != null)
        resultColl.add(result);
    }
    return resultColl;
  }

  private Geometry runBuffer(Geometry a)
  {
    Geometry result = null;
    try {
      result = a.buffer(bufferDistance);
      return result;
    }
    catch (RuntimeException ex) {
      // simply eat exceptions and report them by returning null
      exceptionThrown = true;
    }
    return null;
  }

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context)
  {
    //dialog.setSideBarImage(new ImageIcon(getClass().getResource("DiffSegments.png")));
    dialog.setSideBarDescription(I18N.get("ui.plugin.analysis.BufferPlugIn.buffers-all-geometries-in-the-input-layer"));
    //Initial layer value is null
    dialog.addLayerComboBox(I18N.get("ui.plugin.analysis.BufferPlugIn.layer"), layer, context.getLayerManager());
    dialog.addDoubleField(I18N.get("ui.plugin.analysis.BufferPlugIn.buffer-distance"), bufferDistance, 10, null);
  }

  private void getDialogValues(MultiInputDialog dialog) {
    layer = dialog.getLayer(LAYER);
    bufferDistance = dialog.getDouble(DISTANCE);
  }
}
