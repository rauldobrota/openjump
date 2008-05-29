//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package de.latlon.deejump.base.plugin;

import static com.vividsolutions.jump.workbench.ui.MenuNames.LAYER;
import static com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn.get;
import static de.latlon.deejump.base.i18n.I18N.get;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.openjump.core.ui.plugin.style.ImportSLDPlugIn.importSLD;

import java.io.File;
import java.io.StringReader;

import javax.swing.JFileChooser;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.tools.shape.AVL2SLD;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import de.latlon.deejump.base.i18n.I18N;

/**
 * <code>AVLStylePlugin</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AVLStylePlugin extends AbstractPlugIn {

    private static final ILogger LOG = getLogger( AVLStylePlugin.class );

    @Override
    public void initialize( PlugInContext context ) {
        EnableCheckFactory enableCheckFactory = new EnableCheckFactory( context.getWorkbenchContext() );

        MultiEnableCheck enableCheck = new MultiEnableCheck();
        enableCheck.add( enableCheckFactory.createWindowWithLayerManagerMustBeActiveCheck() );
        enableCheck.add( enableCheckFactory.createExactlyNLayerablesMustBeSelectedCheck( 1, Layerable.class ) );

        context.getFeatureInstaller().addMainMenuItem( this, new String[] { LAYER },
                                                       get( "AVLStylePlugin.name" ) + "{pos:13}", false, null,
                                                       enableCheck );
    }

    @Override
    public boolean execute( PlugInContext context )
                            throws Exception {
        Blackboard bb = get( context.getWorkbenchContext() );

        String fileName = (String) bb.get( "AVLStylePlugin.filename" );

        JFileChooser chooser = new JFileChooser();
        if ( fileName != null ) {
            chooser.setCurrentDirectory( new File( fileName ).getParentFile() );
        }
        int res = chooser.showOpenDialog( context.getWorkbenchFrame() );
        if ( res == APPROVE_OPTION ) {
            File f = chooser.getSelectedFile();
            bb.put( "AVLStylePlugin.filename", f.getAbsoluteFile().toString() );

            int idx = f.toString().lastIndexOf( '.' );
            String base = f.toString().substring( 0, idx );

            if ( !new File( base + ".shp" ).exists() ) {
                showMessageDialog( context.getWorkbenchFrame(), I18N.get( "AVLStylePlugin.requiredfilemissing",
                                                                          base + ".shp" ), I18N.get( "General.error" ),
                                   ERROR_MESSAGE );
                return false;
            }

            if ( !new File( base + ".dbf" ).exists() ) {
                showMessageDialog( context.getWorkbenchFrame(), I18N.get( "AVLStylePlugin.requiredfilemissing",
                                                                          base + ".dbf" ), I18N.get( "General.error" ),
                                   ERROR_MESSAGE );
                return false;
            }

            if ( !new File( base + ".avl" ).exists() ) {
                showMessageDialog( context.getWorkbenchFrame(), I18N.get( "AVLStylePlugin.requiredfilemissing",
                                                                          base + ".avl" ), I18N.get( "General.error" ),
                                   ERROR_MESSAGE );
                return false;
            }

            AVL2SLD avl2sld = new AVL2SLD( base, f.getParent() );
            avl2sld.read();
            StyledLayerDescriptor sld = avl2sld.getStyledLayerDescriptor();

            XMLFragment doc = new XMLFragment( new StringReader( sld.exportAsXML() ), "http://www.systemid.org" );

            if ( LOG.isDebug() ) {
                LOG.logDebug( "Intermediate SLD document", doc.getAsPrettyString() );
            }

            importSLD( doc.getRootElement().getOwnerDocument(), context );
        }

        return false;
    }

    @Override
    public String getName() {
        return get( "MapInfoStylePlugin.name" );
    }

}
