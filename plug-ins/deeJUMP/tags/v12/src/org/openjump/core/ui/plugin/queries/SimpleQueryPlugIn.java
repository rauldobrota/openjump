package org.openjump.core.ui.plugin.queries;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MenuNames;

import org.openjump.core.ui.plugin.queries.SimpleQueryDialog;
import com.vividsolutions.jump.I18N;

/**
 * SimpleQueryPlugIn
 * @author Michaël MICHAUD
 * @version 0.1.0 (4 Dec 2004)
 */ 
public class SimpleQueryPlugIn extends AbstractPlugIn {
    static SimpleQueryDialog simplequeryDialog;
	
    public void initialize(PlugInContext context) throws Exception {

		    context.getFeatureInstaller().addMainMenuItem(this,
		        new String[]
	            {MenuNames.TOOLS, 
		 	    I18N.get("org.openjump.core.ui.plugin.queries.SimpleQuery.menu")},
		        I18N.get("org.openjump.core.ui.plugin.queries.SimpleQuery.menuitem"), false, null, null);   	   
    }
                                                      
    public boolean execute(PlugInContext context) throws Exception {
        if (simplequeryDialog==null) {
            simplequeryDialog = new SimpleQueryDialog(context);
        }
        simplequeryDialog.initUI();
        return false;
    }
    
}
