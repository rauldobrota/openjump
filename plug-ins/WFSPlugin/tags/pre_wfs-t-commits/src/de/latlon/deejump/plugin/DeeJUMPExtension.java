/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.latlon.deejump.plugin;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import de.latlon.deejump.plugin.wfs.WFSPlugIn;


/**
 * General extension classes for installing deeJump plug-ins. This extension
 * installs the follwoing plug-ins:
 * <blockquote>
 * - WFSPlugin
 * </blockquote>
 * 
 * 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class DeeJUMPExtension extends Extension {

    /**
     * @see com.vividsolutions.jump.workbench.plugin.Configuration#configure(com.vividsolutions.jump.workbench.plugin.PlugInContext)
     */
    public void configure( PlugInContext context ) throws Exception {
        System.out.println( "Installing latlon WFSPlugin" );
        new WFSPlugIn(null).install(context); 
        
    }

}
