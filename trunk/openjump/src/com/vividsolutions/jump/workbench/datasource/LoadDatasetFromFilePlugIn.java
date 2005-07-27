package com.vividsolutions.jump.workbench.datasource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

public class LoadDatasetFromFilePlugIn extends AbstractLoadDatasetPlugIn {
    private static final String LAST_DIRECTORY_KEY = LoadDatasetFromFilePlugIn.class
            .getName()
            + " - LAST DIRECTORY";
    protected void setSelectedFormat(String selectedFormat) {
        this.selectedFormat = selectedFormat;
    }
    private String selectedFormat = "";
    protected String getSelectedFormat() {
        return selectedFormat;
    }
    public String getName() {
        //Suggest that multiple datasets may be loaded [Jon Aquino 2005-07-26
        return I18N.get("datasource.LoadDatasetFromFilePlugIn.load-dataset-from-file");
    }
    protected Collection showDialog(WorkbenchContext context) {
        try {
            return showDialogProper(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Collection showDialogProper(WorkbenchContext context)
            throws IOException {
        final JFileChooser fileChooser = GUIUtil
                .createJFileChooserWithExistenceChecking();
        fileChooser.setCurrentDirectory(new File(
                (String) PersistentBlackboardPlugIn.get(context).get(
                        LAST_DIRECTORY_KEY,
                        fileChooser.getCurrentDirectory().getCanonicalPath())));
        fileChooser.setDialogTitle(getName());
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        GUIUtil.removeChoosableFileFilters(fileChooser);
        final Map fileFilterToLoadFileDataSourceQueryChooserMap = new HashMap();
        for (Iterator i = DataSourceQueryChooserManager.get(
                context.getWorkbench().getBlackboard())
                .getLoadDataSourceQueryChoosers().iterator(); i.hasNext();) {
            DataSourceQueryChooser dataSourceQueryChooser = (DataSourceQueryChooser) i
                    .next();
            if (!(dataSourceQueryChooser instanceof LoadFileDataSourceQueryChooser)) {
                continue;
            }
            LoadFileDataSourceQueryChooser loadFileDataSourceQueryChooser = (LoadFileDataSourceQueryChooser) dataSourceQueryChooser;
            fileChooser.addChoosableFileFilter(loadFileDataSourceQueryChooser
                    .getFileFilter());
            fileFilterToLoadFileDataSourceQueryChooserMap.put(
                    loadFileDataSourceQueryChooser.getFileFilter(),
                    loadFileDataSourceQueryChooser);
        }
        for (Iterator i = fileFilterToLoadFileDataSourceQueryChooserMap
                .keySet().iterator(); i.hasNext();) {
            FileFilter fileFilter = (FileFilter) i.next();
            if (fileFilter.getDescription().equals(selectedFormat)) {
                fileChooser.setFileFilter(fileFilter);
            }
        }
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(context
                .getWorkbench().getFrame())) {
            return null;
        }
        PersistentBlackboardPlugIn.get(context).put(LAST_DIRECTORY_KEY,
                fileChooser.getCurrentDirectory().getCanonicalPath());
        selectedFormat = fileChooser.getFileFilter().getDescription();
        return CollectionUtil.collect(Arrays.asList(fileChooser
                .getSelectedFiles()), new Block() {
            public Object yield(Object file) {
                return ((LoadFileDataSourceQueryChooser) fileFilterToLoadFileDataSourceQueryChooserMap
                        .get(fileChooser.getFileFilter()))
                        .toDataSourceQuery((File) file);
            }
        });
    }

}
