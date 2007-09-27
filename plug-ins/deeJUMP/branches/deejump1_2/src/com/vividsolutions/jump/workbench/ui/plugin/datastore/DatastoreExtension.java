package com.vividsolutions.jump.workbench.ui.plugin.datastore;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class DatastoreExtension extends Extension {

    public void configure(PlugInContext context) throws Exception {
        new InstallDatastoreFrameworkPlugIn().initialize(context);
        new AddDatastoreLayerPlugIn().initialize(context);
        new RunDatastoreQueryPlugIn().initialize(context);        
        new InstallDatastoreLayerRendererHintsPlugIn().initialize(context);
    }

    public String getVersion() {
        return "0.1 alpha1";
    }

}