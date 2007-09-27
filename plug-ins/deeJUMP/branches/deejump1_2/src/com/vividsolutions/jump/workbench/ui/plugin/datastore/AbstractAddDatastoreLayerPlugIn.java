package com.vividsolutions.jump.workbench.ui.plugin.datastore;

import java.awt.Component;
import java.util.Collection;

import javax.swing.SwingUtilities;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public abstract class AbstractAddDatastoreLayerPlugIn extends
        ThreadedBasePlugIn {

    public void XXinitialize(PlugInContext context) throws Exception {
        createLayerMenu(1, context);
        createCategoryPopupMenu(4, context);
    }

    protected void createCategoryPopupMenu(int categoryPopupMenuPosition,
            PlugInContext context) {
        new FeatureInstaller(context.getWorkbenchContext()).addPopupMenuItem(
                context.getWorkbenchContext().getWorkbench().getFrame()
                        .getCategoryPopupMenu(), this, getName() + "...{pos:"
                        + categoryPopupMenuPosition + "}", false, null, null);
    }

    protected void createLayerMenu(int layerMenuPosition, PlugInContext context) {
        new FeatureInstaller(context.getWorkbenchContext())
                .addMainMenuItemWithJava14Fix(
                        this,
                        new String[] { "Layer" },
                        getName() + "...{pos:" + layerMenuPosition + "}",
                        false,
                        null,
                        new EnableCheckFactory(context.getWorkbenchContext())
                                .createWindowWithLayerManagerMustBeActiveCheck());
    }

    public boolean execute(final PlugInContext context) throws Exception {
        // The user may have added connections using the Connection Manager
        // Toolbox. So refresh the connectionComboBox.
        // [Jon Aquino 2005-03-15]
        panel(context).populateConnectionComboBox();
        getDialog(context).setVisible(true);
        if (!getDialog(context).wasOKPressed()) {
            return false;
        }
        return true;
    }

    public void run(TaskMonitor monitor, final PlugInContext context)
            throws Exception {
        final Layerable layer = createLayerable(panel(context), monitor,
                context);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Collection selectedCategories = context.getLayerNamePanel()
                        .getSelectedCategories();
                context
                        .getLayerManager()
                        .addLayerable(
                                selectedCategories.isEmpty() ? StandardCategoryNames.WORKING
                                        : selectedCategories.iterator().next()
                                                .toString(), layer);
            }
        });
    }

    protected abstract Layerable createLayerable(ConnectionPanel panel,
            TaskMonitor monitor, PlugInContext context) throws Exception;

    private OKCancelDialog getDialog(PlugInContext context) {
        if (dialog == null) {
            // Cache the dialog between invocations of this menu item,
            // to preserve the dialog's useful cache of dataset names.
            // [Jon Aquino 2005-03-11]
            dialog = createDialog(context);
        }
        return dialog;
    }

    protected ConnectionPanel panel(PlugInContext context) {
        return (ConnectionPanel) getDialog(context).getCustomComponent();
    }

    private OKCancelDialog dialog;

    private OKCancelDialog createDialog(PlugInContext context) {
        OKCancelDialog dialog = new OKCancelDialog(context.getWorkbenchFrame(),
                getName(), true, createPanel(context),
                new OKCancelDialog.Validator() {
                    public String validateInput(Component component) {
                        return ((ConnectionPanel) component).validateInput();
                    }
                });
        dialog.pack();
        GUIUtil.centreOnWindow(dialog);
        return dialog;
    }

    protected abstract ConnectionPanel createPanel(PlugInContext context);
}