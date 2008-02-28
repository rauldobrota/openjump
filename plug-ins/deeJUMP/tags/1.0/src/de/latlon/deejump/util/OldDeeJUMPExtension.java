/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/exse/
 lat/lon Fitzke/Fretter/Poth GbR
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon Fitzke/Fretter/Poth GbR
 Meckenheimer Allee 176
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Jens Fitzke
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: jens.fitzke@uni-bonn.de

 
 ---------------------------------------------------------------------------*/
package de.latlon.deejump.util;

import static org.apache.log4j.Logger.getLogger;
import static org.openjump.OpenJumpConfiguration.postExtensionInitialization;

import org.apache.log4j.Logger;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;

import de.latlon.deejump.plugin.InstallDeegreeFileAdaptersPlugIn;
import de.latlon.deejump.plugin.MapInfoStylePlugin;
import de.latlon.deejump.plugin.wms.LayerAndWMSFeatureInfoTool;

/**
 * ...
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class OldDeeJUMPExtension extends Extension {

    private static final Logger LOG = getLogger( OldDeeJUMPExtension.class );

    public void configure( PlugInContext context )
                            throws Exception {

        LOG.debug( "Initializing old DeeJUMP plugins" );
        LOG.debug( "Adding deegree file adapters..." );
        new InstallDeegreeFileAdaptersPlugIn().initialize( context );
        LOG.debug( "Adding MapInfo styles importer..." );
        new MapInfoStylePlugin().initialize( context );

        WorkbenchContext wbcontext = context.getWorkbenchContext();
        WorkbenchToolBar toolbar = wbcontext.getWorkbench().getFrame().getToolBar();

        LOG.debug( "Adding FeatureInfo tool for WMS GetFeatureInfo requests..." );
        toolbar.addCursorTool( new LayerAndWMSFeatureInfoTool() );

        postExtensionInitialization( context.getWorkbenchContext() );
    }

}
