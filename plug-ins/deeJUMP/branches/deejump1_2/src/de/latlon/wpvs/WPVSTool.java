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

package de.latlon.wpvs;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;


public class WPVSTool extends MultiClickTool {
    
//  TODO remove from here and put perhaps the curosr tool in the context of the WPVS client/plug-in
    public static final String WPVS_CURSOR_HELPER_KEY = "WPVS_CURSOR_HELPER_KEY"; 
    
    private int numberOfPoints = 0;
    
    private Shape shape;
    
    private Coordinate[] coords = new Coordinate[2];
    
    public WPVSTool() {
        //empty
    }

    /*
    public void activate( LayerViewPanel layerViewPanel ) {
        super.activate( layerViewPanel );
    }
    
    public void deactivate() {
        super.deactivate();
    }
    */
    public void mousePressed(MouseEvent e) {
        try {
            super.mousePressed(e);
            Assert.isTrue(e.getClickCount() > 0);

            drawShapeXOR( shape, (Graphics2D) getPanel().getGraphics() );
            shape = null;
            //Don't add more than one point for double-clicks. A double-click will
            //generate two events: one with click-count = 1 and another with
            //click-count = 2. Handle the click-count = 1 event and ignore the rest.
            //[Jon Aquino]
            
            //getCoordinates().size() == 2 ????????
            numberOfPoints++;
            if ( numberOfPoints == 2 ) {
//                gestureFinished();
                return;
            }
            if (e.getClickCount() != 1 ) {
                return;
            }
            add(snap(e.getPoint()));
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }
    
    protected boolean isFinishingRelease(MouseEvent e) {
        return e.getClickCount() == 1 && numberOfPoints == 2;
    }    
    
    protected void gestureFinished() throws Exception {
        this.numberOfPoints = 0;
    }
    
    public Icon getIcon() {
        return IconLoader.icon("DrawLineString.gif");
    }

    protected void finishGesture() throws Exception {
        
        // I'm putting this here this way, because before the gesture finishes and i can "hear" it
        // cords is empty. so i don't care, I use my own class members
        // when i find time, I'll do it properly, if I know how to ;-)
        
        Iterator iter = getCoordinates().iterator();
        coords[0] = (Coordinate) iter.next();
        // skip repeated
        if ( iter.hasNext() ){
        	iter.next();       
        }
        if ( iter.hasNext() ){
        	coords[1] = (Coordinate) iter.next();
        }
        shape = getShape();
        super.cancelGesture();
        try {
            fireGestureFinished();
        } finally {
            //If exception occurs, cancel. [Jon Aquino]
//            coordinates.clear();
        }
        drawShapeXOR( shape, (Graphics2D)getPanel().getGraphics() );
    }
    

    public String getName() {
        return I18N.get("ui.cursortool.DummyTool.dummy-cursor-tool");
    }
    
    public Coordinate[] getCordinates(){
        return this.coords;
    }
    
    public double[] getPOI(){
    	return new double[]{coords[0].x, coords[0].y, 50};
    }
    
}
