
package de.latlon.deejump.plugin.cursortool;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;



public class CutPlugIn extends AbstractPlugIn
{

    public CutPlugIn()
    {
    }

    public void initialize(PlugInContext plugincontext)
        throws Exception
    {
        plugincontext.getWorkbenchFrame().getToolBar().addCursorTool("Découper à partir d'un polygone", CutTool.create(plugincontext.getWorkbenchContext()));
    }

    public boolean execute(PlugInContext plugincontext)
        throws Exception
    {
        return true;
    }
    
    public Icon getIcon()
    {
        return new ImageIcon(CutTool.class.getResource("cutpolygon.gif"));
    }
}