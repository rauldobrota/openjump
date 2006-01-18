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

package org.openjump.core.ui.plugin.edittoolbox.cursortools;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openjump.core.geomutils.GeoUtils;
import org.openjump.core.geomutils.MathVector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

public class RotateSelectedItemTool extends DragTool {
	
    final static String rotateSelectedItem =I18N.get("org.openjump.core.ui.plugin.edittoolbox.cursortools.RotateSelectedItemTool.Rotate-Selected-Item");
	final static String angleST =I18N.get("org.openjump.core.ui.plugin.edittoolbox.cursortools.angle");
	final static String degrees =I18N.get("org.openjump.core.ui.plugin.edittoolbox.cursortools.degrees");	

    private EnableCheckFactory checkFactory;
    private Shape selectedFeatureShape;
    private GeometryFactory geometryFactory = new GeometryFactory();
    private List verticesToSnap = null;
    private Coordinate centerCoord;
    protected boolean clockwise = true;
    private double fullAngle = 0.0;
    
    public RotateSelectedItemTool(EnableCheckFactory checkFactory) {
        this.checkFactory = checkFactory;
        setStroke(
            new BasicStroke(
                1,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                0,
                new float[] { 3, 3 },
                0));
        allowSnapping();
    }

    protected void gestureFinished() throws java.lang.Exception {
        reportNothingToUndoYet();
        ArrayList transactions = new ArrayList();
        for (Iterator i = getPanel().getSelectionManager().getLayersWithSelectedItems().iterator();
            i.hasNext();
            ) {
            Layer layerWithSelectedItems = (Layer) i.next();
            transactions.add(createTransaction(layerWithSelectedItems));
        }
        EditTransaction.commit(transactions);
    }

    private EditTransaction createTransaction(Layer layer) {
        EditTransaction transaction =
            EditTransaction.createTransactionOnSelection(new EditTransaction.SelectionEditor() {
            public Geometry edit(Geometry geometryWithSelectedItems, Collection selectedItems) {
                for (Iterator j = selectedItems.iterator(); j.hasNext();) {
                    Geometry item = (Geometry) j.next();
                    rotate(item);
                }
                return geometryWithSelectedItems;
            }
        }, getPanel(), getPanel().getContext(), getName(), layer, isRollingBackInvalidEdits(), false);
        return transaction;
    }

    private void rotate(Geometry geometry) {
        geometry.apply(new CoordinateFilter() {
            public void filter(Coordinate coordinate) {
                double cosAngle = Math.cos(fullAngle);
                double sinAngle = Math.sin(fullAngle);
                double x = coordinate.x - centerCoord.x;
                double y = coordinate.y - centerCoord.y;
                coordinate.x = centerCoord.x + (x*cosAngle) + (y*sinAngle);
                coordinate.y = centerCoord.y + (y*cosAngle) - (x*sinAngle);
              }
        });
    }
    
    public Cursor getCursor() {
        return createCursor(new ImageIcon(getClass().getResource("RotateSelCursor.gif")).getImage());
    }

    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("RotateSel.gif"));
    }

    public String getName() {
        return rotateSelectedItem;
    }
    
    public void mousePressed(MouseEvent e) {
        try {
            if (!check(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(1))) {
                return;
            }

            if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
                return;
            }

            verticesToSnap = null;
            selectedFeatureShape = createSelectedItemsShape();
            super.mousePressed(e);
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    private Shape createSelectedItemsShape() throws NoninvertibleTransformException {
        Collection selectedGeos = (getPanel().getSelectionManager().getSelectedItems());
        Geometry geo = ((Geometry) selectedGeos.iterator().next());
        centerCoord = geo.getCentroid().getCoordinate(); 
        return getPanel().getJava2DConverter().toShape(geo);
    }

    protected Shape getShape() throws Exception {
        AffineTransform transform = new AffineTransform();
        Point2D centerPt = getPanel().getViewport().toViewPoint(new Point2D.Double(centerCoord.x, centerCoord.y));
        Point2D initialPt = getViewSource();
        Point2D currPt = getViewDestination();
        MathVector center = new MathVector(centerPt.getX(), centerPt.getY());
        MathVector initial = new MathVector(initialPt.getX(), initialPt.getY());
        MathVector curr = new MathVector(currPt.getX(), currPt.getY());
        MathVector initVec = initial.vectorBetween(center);
        MathVector currVec = curr.vectorBetween(center);
        double arcAngle = initVec.angleRad(currVec);
        Coordinate initialCoord = getPanel().getViewport().toModelCoordinate(initialPt);
        Coordinate currCoord = getPanel().getViewport().toModelCoordinate(currPt);
        
        boolean toRight = (new GeoUtils().pointToRight(currCoord, centerCoord, initialCoord));      
        boolean cwQuad = ((fullAngle >= 0.0) &&(fullAngle <= 90.0) && clockwise);
        boolean ccwQuad = ((fullAngle < 0.0) &&(fullAngle >= -90.0) && !clockwise);
        
        if ((arcAngle <= 90.0) && (cwQuad || ccwQuad))
        {
            if (toRight)
                clockwise = true;
            else
                clockwise = false;
        }

        if ((fullAngle > 90.0) || (fullAngle < -90))
        {
            if ((clockwise && !toRight) || (!clockwise && toRight))
                fullAngle = 360 - arcAngle;
            else
                fullAngle = arcAngle;
        }
        else
        {
            fullAngle = arcAngle;
        }
        
        if (!clockwise)
            fullAngle = -fullAngle;

        DecimalFormat df2 = new DecimalFormat("##0.0#");
        getPanel().getContext().setStatusMessage(angleST + ": " + df2.format(Math.toDegrees(fullAngle)) + " " + degrees);        
//        getPanel().getContext().setStatusMessage("angle = " + df2.format(Math.toDegrees(fullAngle)) + " degrees");
//        getPanel().getContext().setStatusMessage("angle = " + getPanel().format(Math.toDegrees(fullAngle)) + " degrees");
        transform.rotate(fullAngle, centerPt.getX(), centerPt.getY());
        return transform.createTransformedShape(selectedFeatureShape);
   }
}
