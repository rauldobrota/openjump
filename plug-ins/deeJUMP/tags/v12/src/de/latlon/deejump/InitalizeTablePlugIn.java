/*----------------    FILE HEADER  ------------------------------------------

This file is part of deegree.
Copyright (C) 2001 by:
EXSE, Department of Geography, University of Bonn
http://www.giub.uni-bonn.de/exse/
lat/lon Fitzke/Fretter/Poth GbR
http://www.lat-lon.de

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Contact:

Andreas Poth
lat/lon Fitzke/Fretter/Poth GbR
Meckenheimer Allee 176
53115 Bonn
Germany
E-Mail: poth@lat-lon.de

Jens Fitzke
Department of Geography
University of Bonn
Meckenheimer Allee 166
53115 Bonn
Germany
E-Mail: jens.fitzke@uni-bonn.de

                 
 ---------------------------------------------------------------------------*/
package de.latlon.deejump;

import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JSplitPane;
import javax.swing.JTable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.coordsys.CoordinateSystemRegistry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.java2xml.XML2Java;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.OneLayerAttributeTab;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.OpenProjectPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.WorkbenchContextReference;

/**
 * ...
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class InitalizeTablePlugIn extends AbstractPlugIn {
    

    private Task newTask;

    private Task sourceTask;
    
    private TaskFrame tf;
    
    private LayerViewPanel lvp;
    
    private Layer layer;
        
    private ComponentListener componentListener;
    public void initialize(final PlugInContext context) throws Exception {
        
        componentListener = new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                //Two reasons wait until the frame is shown before adding the task frame:
                //(1) Otherwise the task frame won't be selected (2) Otherwise GUIUtil.setLocation
                //will throw an IllegalComponentStateException. [Jon Aquino]
                
                String fn = (String)context.getWorkbenchContext().getBlackboard().get( JUMPWorkbench.INITIAL_PROJECT_FILE );
                if( fn == null ){//create empty task
                    context.getWorkbenchFrame().addTaskFrame();
                    
                } else {
                    
                File f = new File( fn );

	        		try {
	                    open( f, context.getWorkbenchFrame());
	                    initialize(context);
	                    run(null, context);
	                    initTable(context);
	                } catch (Exception e1) {
	                    // TODO Auto-generated catch block
	                    e1.printStackTrace();
	                }
                }
                
                
                context.getWorkbenchFrame().removeComponentListener(componentListener);
            }
        };
        context.getWorkbenchFrame().addComponentListener(componentListener);
        
        
    }
    

    
    public boolean execute(PlugInContext context) throws Exception {

        return true;
    }

    public void run(TaskMonitor monitor, PlugInContext context)
            throws Exception {
        loadLayers(sourceTask.getLayerManager(), newTask.getLayerManager(),
                CoordinateSystemRegistry.instance(context.getWorkbenchContext()
                        .getBlackboard()), monitor);
    }

    private void open(File file, WorkbenchFrame workbenchFrame)
            throws Exception {
        FileReader reader = new FileReader(file);

        try {
            sourceTask = (Task) new XML2Java(workbenchFrame.getContext()
                    .getWorkbench().getPlugInManager().getClassLoader()).read(
                    reader, Task.class);
            //I can't remember why I'm creating a new Task instead of using
            //sourceTask. There must be a good reason. [Jon Aquino]
            // Probably to reverse the order of the layerables. See comments.
            // Probably also to set the LayerManager coordinate system.
            // [Jon Aquino 2005-03-16]
            initializeDataSources(sourceTask, workbenchFrame.getContext());
            newTask = new Task();
            newTask.setName(GUIUtil.nameWithoutExtension(file));
            newTask.setProjectFile(file);
            tf = workbenchFrame.addTaskFrame(newTask);
        } finally {
            reader.close();
        }
    }

    private void initializeDataSources(Task task, WorkbenchContext context) {
        for (Iterator i = task.getLayerManager().getLayers().iterator(); i
                .hasNext();) {
            Layer layer = (Layer) i.next();
            if (layer.getDataSourceQuery().getDataSource() instanceof WorkbenchContextReference) {
                ((WorkbenchContextReference) layer.getDataSourceQuery()
                        .getDataSource()).setWorkbenchContext(context);
            }
        }
    }

    private void loadLayers(LayerManager sourceLayerManager,
            LayerManager newLayerManager, CoordinateSystemRegistry registry,
            TaskMonitor monitor) throws Exception {
        for (Iterator i = sourceLayerManager.getCategories().iterator(); i
                .hasNext();) {
            Category sourceLayerCategory = (Category) i.next();
            // Explicitly add categories. Can't rely on
            // LayerManager#addLayerable to add the categories, because a
            // category might not have any layers. [Jon Aquino]
            newLayerManager.addCategory(sourceLayerCategory.getName());

            // LayerManager#addLayerable adds layerables to the top. So reverse
            // the order. [Jon Aquino]
            ArrayList layerables = new ArrayList(sourceLayerCategory
                    .getLayerables());
            Collections.reverse(layerables);

            for (Iterator j = layerables.iterator(); j.hasNext();) {
                Layerable layerable = (Layerable) j.next();
                if( monitor != null ){
                    monitor.report(I18N.get("ui.plugin.OpenProjectPlugIn.loading") + " " + layerable.getName());
                }
                layerable.setLayerManager(newLayerManager);

                if (layerable instanceof Layer) {
                    Layer layer = (Layer) layerable;
                    OpenProjectPlugIn.load(layer, registry, monitor);
                }

                newLayerManager.addLayerable(sourceLayerCategory.getName(),
                        layerable);
            }
        }
    }
    
    private void initTable( PlugInContext context ){
        
        Container c = tf.getContentPane();
        Container child = (Container )c.getComponent(0);
        JSplitPane pane = (JSplitPane)child;
//        pane.setDividerSize( 5 );
        
        lvp = (LayerViewPanel)pane.getRightComponent();
        
        /*List layers = lvp.getLayerManager().getLayers();
        for (Iterator iter = layers.iterator(); iter.hasNext();) {
            Layer layer = (Layer) iter.next();
            System.out.println( layer.getName() );
            
        }*/
        
        layer = lvp.getLayerManager().getLayer(0);
        
        OneLayerAttributeTab attributeTab = 
            new OneLayerAttributeTab(context.getWorkbenchContext(),tf, tf)
                	.setLayer(layer);
       
        JTable table = attributeTab.getPanel().getTablePanel( layer ).getTable();
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectGeometry( e );
            }
        });
        
        pane.setLeftComponent( attributeTab );
        
    }
	
    private void selectGeometry(MouseEvent e ){
        JTable table = (JTable)e.getSource();
        LayerTableModel layerTableModel = (LayerTableModel)table.getModel();
        try {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) {
                return;
            }
            Feature f = layerTableModel.getFeature(row);
            if ( f != null ){
                
                Envelope env = lvp.getViewport().getEnvelopeInModelCoordinates();
                double midX = ( env.getMaxX() + env.getMinX() ) / 2d;
                double midY = ( env.getMaxY() + env.getMinY() ) / 2d;
                
                
                GeometryFactory gf = new GeometryFactory();
                
                f.setGeometry( gf.createPoint(new Coordinate(midX,midY)) );
                //layer.fireLayerChanged( LayerEventType.APPEARANCE_CHANGED );
                layer.fireAppearanceChanged();
                
            }
            
            
        } catch (Throwable t) {
            t.printStackTrace();
//            workbenchContext.getErrorHandler().handleThrowable(t);
        }
    }
    
}
