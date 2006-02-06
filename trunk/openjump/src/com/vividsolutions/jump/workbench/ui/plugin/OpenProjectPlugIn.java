/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI for
 * visualizing and manipulating spatial features with geometry and attributes.
 * 
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * For more information, contact:
 * 
 * Vivid Solutions Suite #1A 2328 Government Street Victoria BC V8T 5G5 Canada
 * 
 * (250)385-6040 www.vividsolutions.com
 */

package com.vividsolutions.jump.workbench.ui.plugin;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JFileChooser;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.coordsys.CoordinateSystemRegistry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.java2xml.XML2Java;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;

public class OpenProjectPlugIn extends ThreadedBasePlugIn {
    private JFileChooser fileChooser;

    private Task newTask;

    private Task sourceTask;

    public OpenProjectPlugIn() {
    }

	public String getName() {
		return I18N.get("ui.plugin.OpenProjectPlugIn.open-project");
	}

    public void initialize(PlugInContext context) throws Exception {
        //Don't initialize fileChooser in field declaration -- seems too early
        // because
        //we sometimes get a WindowsFileChooserUI NullPointerException [Jon
        // Aquino 12/10/2003]
        fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
        fileChooser.setDialogTitle(I18N.get("ui.plugin.OpenProjectPlugIn.open-project"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        GUIUtil.removeChoosableFileFilters(fileChooser);
        fileChooser
                .addChoosableFileFilter(SaveProjectAsPlugIn.JUMP_PROJECT_FILE_FILTER);
        fileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        fileChooser.setFileFilter(SaveProjectAsPlugIn.JUMP_PROJECT_FILE_FILTER);
    }

    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);

        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(context
                .getWorkbenchFrame())) {
            return false;
        }
        open(fileChooser.getSelectedFile(), context.getWorkbenchFrame());
        return true;
    }

    public void run(TaskMonitor monitor, PlugInContext context)
            throws Exception {
        loadLayers(sourceTask, newTask.getLayerManager(),
                CoordinateSystemRegistry.instance(context.getWorkbenchContext()
                        .getBlackboard()), monitor);
    }

    protected void open(File file, WorkbenchFrame workbenchFrame)
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
            newTask = new Task();
            newTask.setProjectFile(file);            
            sourceTask.setProjectFile(file);            
            initializeDataSources(sourceTask, workbenchFrame.getContext());
            newTask.setName(GUIUtil.nameWithoutExtension(file));
            workbenchFrame.addTaskFrame(newTask);
        } finally {
            reader.close();
        }
    }

    private void initializeDataSources(Task task, WorkbenchContext context) {
        
        for (Iterator i = task.getLayerManager().getLayers().iterator(); i
                .hasNext();) {
            Layer layer = (Layer) i.next();

            if (layer.getDataSourceQuery().getDataSource() instanceof WorkbenchContextReference) {
                
                ((WorkbenchContextReference)layer.getDataSourceQuery()
                        .getDataSource()).setWorkbenchContext(context);
            }
        }
    }

    private void loadLayers( Task sourceTask,
            LayerManager newLayerManager, CoordinateSystemRegistry registry,
            TaskMonitor monitor) throws Exception {
        
        LayerManager sourceLayerManager = sourceTask.getLayerManager();
        
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
                if ( monitor != null ){
                    monitor.report(I18N.get("ui.plugin.OpenProjectPlugIn.loading") + " " + layerable.getName());
                }
                layerable.setLayerManager(newLayerManager);

                if (layerable instanceof Layer) {
                    Layer layer = (Layer) layerable;
                    load( sourceTask.getProjectFile(), layer, registry, monitor);
                }

                newLayerManager.addLayerable(sourceLayerCategory.getName(),
                        layerable);
            }
        }
    }

    public static void load( File parentFile, Layer layer, CoordinateSystemRegistry registry, TaskMonitor monitor) throws Exception {
        
        DataSource dataSource = layer.getDataSourceQuery().getDataSource();
        
        String filename = (String) dataSource.getProperties().get(DataSource.FILE_KEY);
        
        //this is to keep backwards compatibility
        File f = new File( filename );
        if ( !f.isAbsolute() ) {
            filename = parentFile.getParentFile() + File.separator + filename;
            dataSource.getProperties().put( DataSource.FILE_KEY, 
                     filename );
        }
        
        layer.setFeatureCollection(executeQuery(layer
                .getDataSourceQuery().getQuery(), dataSource, registry,
                monitor));
        layer.setFeatureCollectionModified(false);
    }

    private static FeatureCollection executeQuery(String query, DataSource dataSource,
            CoordinateSystemRegistry registry, TaskMonitor monitor)
            throws Exception {
        Connection connection = dataSource.getConnection();
        try {
            return dataSource.installCoordinateSystem(connection.executeQuery(
                    query, monitor), registry);
        } finally {
            connection.close();
        }
    }
    
}