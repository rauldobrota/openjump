package com.vividsolutions.jump.workbench.ui.plugin.datastore;

import javax.swing.JFrame;

import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.datastore.DataStoreException;

import com.vividsolutions.jump.datastore.postgis.PostgisDataStoreDriver;

import com.vividsolutions.jump.workbench.datastore.ConnectionManager;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.ApplicationExitHandler;

public class InstallDatastoreFrameworkPlugIn extends AbstractPlugIn {

    public void initialize(final PlugInContext context) throws Exception {
        context.getWorkbenchContext().getRegistry().
        createEntry(DataStoreDriver.REGISTRY_CLASSIFICATION,new PostgisDataStoreDriver());
        final ApplicationExitHandler oldApplicationExitHandler = context
                .getWorkbenchFrame().getApplicationExitHandler();
        context.getWorkbenchFrame().setApplicationExitHandler(
                new ApplicationExitHandler() {
                    public void exitApplication(JFrame mainFrame) {
                        try {
                            ConnectionManager.instance(
                                    context.getWorkbenchContext()
                                            .getBlackboard())
                                    .closeConnections();
                        } catch (DataStoreException e) {
                            throw new RuntimeException(e);
                        }
                        oldApplicationExitHandler.exitApplication(mainFrame);
                    }
                });
    }
}