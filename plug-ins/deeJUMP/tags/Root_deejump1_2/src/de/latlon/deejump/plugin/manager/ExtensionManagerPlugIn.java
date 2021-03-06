/* This file is *not* under GPL or any other public license
 * Copyright 2005 Ugo Taddei 
 */
package de.latlon.deejump.plugin.manager;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.MenuNames;

public class ExtensionManagerPlugIn extends ThreadedBasePlugIn {
    
    private ExtensionManagerDialog managerDialog;
    
    public ExtensionManagerPlugIn() {
        //nuffin to do
    }

    public boolean execute( PlugInContext context ) throws Exception {
        if ( managerDialog == null ) {
            managerDialog = new ExtensionManagerDialog(
                    context.getWorkbenchFrame(), 
                    context.getWorkbenchContext(),
                    "http://jump-pilot.sourceforge.net/downloads/"
                    //"file:///e:/proj/openjump/plugins/"
                    );
        }
        
        managerDialog.setVisible( true );
        
        return managerDialog.isOkClicked();
    }

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        managerDialog.updateExtensions( monitor );
        context.getWorkbenchFrame().setStatusMessage( " Plug-ins will only be removed after next start" );
    }
    
    
    public void install( PlugInContext context ){
        context.getFeatureInstaller().addMainMenuItem(
            this, MenuNames.TOOLS,
    		this.getName(), null,
    		null);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
