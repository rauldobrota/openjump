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
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jts.util.Assert;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.coordsys.CoordinateSystemRegistry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

import java.awt.event.ComponentAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import javax.swing.SwingUtilities;

/**
 * Prompts the user to pick a dataset to load.
 * @see DataSourceQueryChooserDialog
 */
public class LoadDatasetPlugIn extends AbstractLoadDatasetPlugIn {
    private WorkbenchContext context;
    public void initialize(PlugInContext context) throws Exception {
        this.context = context.getWorkbenchContext();
        super.initialize(context);
    }
    private DataSourceQueryChooserDialog getDialog() {
        String KEY = getClass().getName() + " - DIALOG";
        if (null == context.getWorkbench().getBlackboard()
                               .get(KEY)) {
            context.getWorkbench().getBlackboard().put(KEY,
                new DataSourceQueryChooserDialog(DataSourceQueryChooserManager.get(
                        context.getWorkbench()
                               .getBlackboard()).getLoadDataSourceQueryChoosers(),
                    context.getWorkbench().getFrame(), getName(), true));
        }

        return (DataSourceQueryChooserDialog) context
                                                     .getWorkbench()
                                                     .getBlackboard().get(KEY);
    }
    
    public String getName() {
        //Suggest that multiple datasets may be loaded [Jon Aquino 11/10/2003]
        return I18N.get("datasource.LoadDatasetPlugIn.load-dataset");
    }

    protected void setSelectedFormat(String format) {
        getDialog().setSelectedFormat(format);
    }
    protected Collection showDialog(WorkbenchContext context) {
        GUIUtil.centreOnWindow(getDialog());
        getDialog().setVisible(true);
        return getDialog().wasOKPressed() ? getDialog().getCurrentChooser().getDataSourceQueries() : null;        
    }
    protected String getSelectedFormat() {
        return getDialog().getSelectedFormat();
    }
}
