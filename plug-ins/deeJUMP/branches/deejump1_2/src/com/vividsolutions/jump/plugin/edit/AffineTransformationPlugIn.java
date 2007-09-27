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


package com.vividsolutions.jump.plugin.edit;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.geom.*;
import com.vividsolutions.jump.util.ColorUtil;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.*;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.*;

/**
 * Applies an {@link AffineTransformation} to a layer.
 *
 * @author Martin Davis
 */
public class AffineTransformationPlugIn
  extends ThreadedBasePlugIn
{

  private MultiInputDialog dialog;
  private Layer layer;
  private double originX = 0.0;
  private double originY = 0.0;
  private double transX = 0.0;
  private double transY = 0.0;
  private double scaleX = 1.0;
  private double scaleY = 1.0;
  private double shearX = 0.0;
  private double shearY = 0.0;
  private double rotationAngle = 0.0;

  public AffineTransformationPlugIn() { }

  public String getName() { return "Affine Transformation"; }

  public EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
      EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
      return new MultiEnableCheck()
          .add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck())
          .add(checkFactory.createAtLeastNLayersMustExistCheck(1));
  }

  public boolean execute(PlugInContext context) throws Exception {
    dialog = new MultiInputDialog(
        context.getWorkbenchFrame(), getName(), true);
    setDialogValues(dialog, context);
    GUIUtil.centreOnWindow(dialog);
    dialog.setVisible(true);
    if (! dialog.wasOKPressed()) { return false; }
    getDialogValues(dialog);
    //perform(dialog, context);
    return true;
  }

  public void run(TaskMonitor monitor, PlugInContext context)
       throws Exception
  {
    AffineTransformation trans = new AffineTransformation();
    
    AffineTransformation toOriginTrans 
    = AffineTransformation.translationInstance(-originX, -originY);
    trans.compose(toOriginTrans);

    if (scaleX != 1.0 || scaleY != 1.0) {
      AffineTransformation scaleTrans 
        = AffineTransformation.scaleInstance(scaleX, scaleY);
      //trans.compose(scaleTrans);
      trans.scale(scaleX, scaleY);
    }
    if (shearX != 0.0 || shearY != 0.0) {
      trans.shear(shearX, shearY);
    }
    if (rotationAngle != 0.0) {
      AffineTransformation rotTrans 
        = AffineTransformation.rotationInstance(Math.toRadians(rotationAngle));
//      trans.compose(rotTrans);
      trans.rotate(Math.toRadians(rotationAngle));
    }
    
    AffineTransformation fromOriginTrans 
    = AffineTransformation.translationInstance(originX, originY);
    trans.compose(fromOriginTrans);

    if (transX != 0.0 || transY != 0.0) {
      AffineTransformation translateTrans 
        = AffineTransformation.translationInstance(transX, transY);
      trans.compose(translateTrans);
    }

    FeatureCollection fc = layer.getFeatureCollectionWrapper();
    
    FeatureCollection resultFC = new FeatureDataset(fc.getFeatureSchema());
    
    for (Iterator i = fc.iterator(); i.hasNext();) {
      Feature f = (Feature) i.next();
      Feature f2 = f.clone(true);
      f2.getGeometry().apply(trans);
      f2.getGeometry().geometryChanged();
      resultFC.add(f2);
    }

    createLayers(context, resultFC);
  }


  private void createLayers(PlugInContext context,
                            FeatureCollection transFC)
  {
    Layer lyr = context.addLayer(StandardCategoryNames.RESULT,
                                    "Affine-" + layer.getName(), transFC);
    lyr.fireAppearanceChanged();
  }

  private final static String LAYER = "Layer";
  private final static String ORIGIN = "Anchor Point";
  private final static String ORIGIN_FROM_LL = "Set to Lower Left";
  private final static String ORIGIN_FROM_MIDPOINT = "Set to Midpoint";
  private final static String ORIGIN_X = "X";
  private final static String ORIGIN_Y = "Y";
  private final static String TRANS_DX = "DX";
  private final static String TRANS_DY = "DY";
  private final static String TRANS_DX_DY = "Translate by (X,Y)";
  private final static String SCALE_X = "X Factor";
  private final static String SCALE_Y = "Y Factor";
  private final static String ROTATE_ANGLE = "Angle";
  private final static String SHEAR_X = "X Shear";
  private final static String SHEAR_Y = "Y Shear";
  private final static String SRC_BASE_LAYER = "Source Layer";
  private final static String DEST_BASE_LAYER = "Destination Layer";
  private final static String BASELINE_BUTTON = "Compute Parameters";

//  private JRadioButton matchSegmentsRB;
  private JTextField originXField;
  private JTextField originYField;
  private JTextField transXField;
  private JTextField transYField;
  private JTextField scaleXField;
  private JTextField scaleYField;
  private JTextField shearXField;
  private JTextField shearYField;
  private JTextField rotateAngleField;

  private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
    dialog.setSideBarImage(new ImageIcon(getClass().getResource("AffineTransformation.png")));
    dialog.setSideBarDescription(
        "Applies an Affine Transformation to all features in a layer."
        + "  The transformation is specified by a combination of scaling, rotation, shearing and translation."
        + "  Transformation parameters may be computed from two layers containing baseline vectors.");

    dialog.addLayerComboBox(LAYER, context.getCandidateLayer(0),
        context.getLayerManager());

    dialog.addLabel("<HTML><B>Anchor Point</B></HTML>");

    originXField = dialog.addDoubleField(ORIGIN_X, originX, 20,
    "Anchor Point X value");
    originYField = dialog.addDoubleField(ORIGIN_Y, originY, 20,
    "Anchor Point Y value");

    JButton buttonOriginLL = dialog.addButton(ORIGIN_FROM_LL);
    buttonOriginLL.addActionListener(new OriginLLListener(true));
    
    JButton buttonOriginMid = dialog.addButton(ORIGIN_FROM_MIDPOINT);
    buttonOriginMid.addActionListener(new OriginLLListener(false));
    
    dialog.addLabel("<HTML><B>Scaling</B></HTML>");
    scaleXField = dialog.addDoubleField(SCALE_X, scaleX, 20, "Scale X Factor");
    scaleYField = dialog.addDoubleField(SCALE_Y, scaleY, 20, "Scale Y Factor");

    dialog.addLabel("<HTML><B>Rotation</B></HTML>");
    rotateAngleField = dialog.addDoubleField(ROTATE_ANGLE, rotationAngle, 20,
        "Rotation Angle in degrees");

    dialog.addLabel("<HTML><B>Shearing</B></HTML>");
    shearXField = dialog.addDoubleField(SHEAR_X, shearX, 20, "Shear X Factor");
    shearYField = dialog.addDoubleField(SHEAR_Y, shearY, 20, "Shear Y Factor");

    dialog.addLabel("<HTML><B>Translation</B></HTML>");
    transXField = dialog.addDoubleField(TRANS_DX, transX, 20,
       "Translation X value");
    transYField = dialog.addDoubleField(TRANS_DY, transY, 20,
       "Translation Y value");

    dialog.startNewColumn();
    JButton setIdentityButton = dialog.addButton("Set to Identity");
    setIdentityButton.addActionListener(new SetIdentityListener());
    dialog.addSeparator();
    
    dialog.addLabel("<HTML><B>Baseline Vectors</B></HTML>");
    dialog.addLayerComboBox(SRC_BASE_LAYER, context.getLayerManager().getLayer(0),
        context.getLayerManager());
    dialog.addLayerComboBox(DEST_BASE_LAYER, context.getLayerManager().getLayer(0),
        context.getLayerManager());
    JButton buttonParam = dialog.addButton(BASELINE_BUTTON);
    buttonParam.addActionListener(new UpdateParamListener());

  }

  private void getDialogValues(MultiInputDialog dialog) {
    layer = dialog.getLayer(LAYER);
    originX = dialog.getDouble(ORIGIN_X);
    originY = dialog.getDouble(ORIGIN_Y);
    transX = dialog.getDouble(TRANS_DX);
    transY = dialog.getDouble(TRANS_DY);
    scaleX = dialog.getDouble(SCALE_X);
    scaleY = dialog.getDouble(SCALE_Y);
    shearX = dialog.getDouble(SHEAR_X);
    shearY = dialog.getDouble(SHEAR_Y);
    rotationAngle = dialog.getDouble(ROTATE_ANGLE);
  }

  /*
  private void XXXupdateUI()
  {
    boolean doTranslate = dialog.getBoolean(DO_TRANS);
    transXField.setEnabled(doTranslate);
    transXField.setOpaque(doTranslate);
    transYField.setEnabled(doTranslate);
    transYField.setOpaque(doTranslate);

    boolean doScale = dialog.getBoolean(DO_SCALE);
    scaleFactorField.setEnabled(doScale);
    scaleFactorField.setOpaque(doScale);

    boolean doRotate = dialog.getBoolean(DO_ROTATE);
    rotateAngleField.setEnabled(doRotate);
    rotateAngleField.setOpaque(doRotate);
  }
*/
  private void updateOriginLL(boolean isLowerLeft)
  {
    Layer lyr = dialog.getLayer(LAYER);
    FeatureCollection fc = lyr.getFeatureCollectionWrapper();
    Envelope env = fc.getEnvelope();
    
    double x = env.getMinX();
    double y = env.getMinY();
    if (! isLowerLeft) {
      x = (env.getMinX() + env.getMaxX()) / 2;
      y = (env.getMinY() + env.getMaxY()) / 2;
    }
    originXField.setText(x + "");
    originYField.setText(y + "");
  }
  
  private void updateParams()
  {
    Layer layerSrc = dialog.getLayer(SRC_BASE_LAYER);
    Layer layerDest = dialog.getLayer(DEST_BASE_LAYER);
    Coordinate[] srcVector = getVector(layerSrc);
    Coordinate[] destVector = getVector(layerDest);
    if (srcVector == null || destVector == null) 
      return;
    
    originXField.setText(srcVector[0].x + "");
    originYField.setText(srcVector[0].y + "");
    
    TransRotScaleBuilder trsBuilder = new TransRotScaleBuilder(srcVector, destVector);
    
    scaleXField.setText(trsBuilder.getScale() + "");
    scaleYField.setText(trsBuilder.getScale() + "");

    transXField.setText(trsBuilder.getTranslateX() + "");
    transYField.setText(trsBuilder.getTranslateY() + "");
    
    rotateAngleField.setText(trsBuilder.getAngle() + "");
  }
  
  private void setToIdentity()
  {
    scaleXField.setText("1.0");
    scaleYField.setText("1.0");

    shearXField.setText("0.0");
    shearYField.setText("0.0");

    transXField.setText("0.0");
    transYField.setText("0.0");
    
    rotateAngleField.setText("0.0");
  }
  
  /**
   * Gets the first two points from the first geometry on the layer (if any).
   * @param lyr the layer to extract from
   * @return a coordinate array of length 2 (the two points may be equal)
   * @return null if points could not be determined
   */
  private Coordinate[] getVector(Layer lyr)
  {
    FeatureCollection fc = lyr.getFeatureCollectionWrapper();
    Iterator i = fc.iterator();
    if (! i.hasNext()) 
      return null;
    Feature f = (Feature) i.next();
    Geometry gFull = f.getGeometry();
    Geometry g = gFull.getGeometryN(0);
    
    if (g instanceof com.vividsolutions.jts.geom.Polygon)
      g = ((com.vividsolutions.jts.geom.Polygon) g).getExteriorRing();
    Coordinate[] pts = g.getCoordinates();
    if (pts.length < 1) 
      return null;
    int index2 = 1;
    if (pts.length == 1)
      index2 = 0;
    return new Coordinate[] { pts[0], pts[index2] };
  }
  
  private class OriginLLListener implements ActionListener 
  {
    private boolean isLowerLeft;
    
    OriginLLListener(boolean isLowerLeft)
    {
      this.isLowerLeft = isLowerLeft;
    }
    
    public void actionPerformed(ActionEvent e) {
      updateOriginLL(isLowerLeft);
    }
  }
  private class UpdateParamListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      updateParams();
    }
  }
  private class SetIdentityListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      setToIdentity();
    }
  }
}

class TransRotScaleBuilder
{
  private double scale = 0.0;
  private double dx = 0.0;
  private double dy = 0.0;
  private double angle = 0.0;  // in degrees
  
  TransRotScaleBuilder(Coordinate[] srcVector, Coordinate[] destVector)
  {
    init(srcVector, destVector);
  }
  
  private void init(Coordinate[] srcVector, Coordinate[] destVector)
  {
    double srcLen = srcVector[0].distance(srcVector[1]);
    double destLen = destVector[0].distance(destVector[1]);
    
    boolean isZeroLength = (srcLen == 0.0 || destLen == 0.0);
    
    if (! isZeroLength) {
      scale = destLen / srcLen;
      
      double angleSrc = Angle.angle(srcVector[0], srcVector[1]);
      double angleDest = Angle.angle(destVector[0], destVector[1]);
      /*
      Coordinate vecSrc = getUnitVector(srcVector);
      Coordinate vecDest = getUnitVector(destVector);
      */
      double angleRad = angleDest - angleSrc;
      angle = Math.toDegrees(angleRad);
    }
    
    dx = destVector[0].x - srcVector[0].x;
    dy = destVector[0].y - srcVector[0].y;
  }
  
  public boolean isScale() { return scale > 0.0; }
  public double getScale() { return scale; }
  
  public boolean isTranslate() { return dx != 0.0 | dy != 0.0; }
  public double getTranslateX() { return dx; }
  public double getTranslateY() { return dy; }
  
  public double getAngle() { return angle; }
}
