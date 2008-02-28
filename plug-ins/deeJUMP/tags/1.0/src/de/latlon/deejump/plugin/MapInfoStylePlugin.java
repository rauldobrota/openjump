//$HeadURL: https://sushibar/svn/deegree/base/trunk/resources/eclipse/svn_classfile_header_template.xml $
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

package de.latlon.deejump.plugin;

import static com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn.get;
import static de.latlon.deejump.i18n.I18N.get;
import static java.awt.Color.decode;
import static java.lang.Integer.parseInt;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static org.deegree.io.mapinfoapi.MapInfoReader.parseFeatures;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFileChooser;

import org.deegree.framework.util.Pair;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;

import de.latlon.deejump.plugin.style.BitmapVertexStyle;

/**
 * <code>MapInfoStylePlugin</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class MapInfoStylePlugin extends AbstractPlugIn {

    @Override
    public void initialize( PlugInContext context ) {
        EnableCheckFactory enableCheckFactory = new EnableCheckFactory( context.getWorkbenchContext() );

        MultiEnableCheck enableCheck = new MultiEnableCheck();
        enableCheck.add( enableCheckFactory.createWindowWithLayerManagerMustBeActiveCheck() );
        enableCheck.add( enableCheckFactory.createExactlyNLayerablesMustBeSelectedCheck( 1, Layerable.class ) );

        context.getFeatureInstaller().addMainMenuItem( this, new String[] { MenuNames.FILE },
                                                       get( "MapInfoStylePlugin.name" ) + "{pos:5}", false, null,
                                                       enableCheck );
    }

    @Override
    public boolean execute( PlugInContext context )
                            throws Exception {
        Blackboard bb = get( context.getWorkbenchContext() );

        String fileName = (String) bb.get( "MapInfoStylePlugin.filename" );

        JFileChooser chooser = new JFileChooser();
        if ( fileName != null ) {
            chooser.setCurrentDirectory( new File( fileName ).getParentFile() );
        }
        int res = chooser.showOpenDialog( context.getWorkbenchFrame() );
        if ( res == APPROVE_OPTION ) {
            File f = chooser.getSelectedFile();
            bb.put( "MapInfoStylePlugin.filename", f.getAbsoluteFile().toString() );
            Layer l = context.getSelectedLayer( 0 );

            Pair<org.deegree.model.feature.FeatureCollection, HashMap<String, HashSet<HashMap<String, String>>>> pair;
            pair = parseFeatures( f.getAbsolutePath() );
            pair.first = null; // to enable gc when needed, yes it IS bad

            for ( String type : pair.second.keySet() ) {
                if ( "symbol".equals( type ) ) {
                    for ( HashMap<String, String> map : pair.second.get( type ) ) {
                        System.out.println( "could use this style" + map );
                        VertexStyle style = null;
                        Color c = decode( map.get( "color" ) );
                        int size = parseInt( map.get( "size" ) );
                        int symbol = parseInt( map.get( "shape" ) );

                        style = new BitmapVertexStyle( "/home/stranger/svgpoints/" + ( symbol - 30 ) + ".svg" );
                        style.setSize( size );
                        style.setFillColor( c );
                        style.setLineColor( c );
                        // switch ( symbol ) {
                        // case 31:
                        // break;
                        // case 32:
                        // style = new SquareVertexStyle();
                        // style.setSize( size / 2 );
                        // l.getBasicStyle().setFillColor( c );
                        // l.getBasicStyle().setLineColor( black );
                        // break;
                        // case 34:
                        // style = new CircleVertexStyle();
                        // style.setSize( size / 2 );
                        // l.getBasicStyle().setFillColor( c );
                        // l.getBasicStyle().setLineColor( black );
                        // break;
                        // case 35:
                        // style = new StarVertexStyle();
                        // style.setSize( size / 2 );
                        // l.getBasicStyle().setFillColor( c );
                        // l.getBasicStyle().setLineColor( black );
                        // break;
                        // case 36:
                        // style = new TriangleVertexStyle();
                        // style.setSize( size / 2 );
                        // l.getBasicStyle().setFillColor( c );
                        // l.getBasicStyle().setLineColor( black );
                        // break;
                        // case 38:
                        // style = new SquareVertexStyle();
                        // style.setSize( size / 2 );
                        // l.getBasicStyle().setLineColor( c );
                        // style.setFilling( false );
                        // l.getBasicStyle().setRenderingFill( false );
                        // break;
                        // case 40:
                        // style = new CircleVertexStyle();
                        // style.setSize( size / 2 );
                        // l.getBasicStyle().setLineColor( c );
                        // style.setFilling( false );
                        // l.getBasicStyle().setRenderingFill( false );
                        // break;
                        // case 41:
                        // style = new StarVertexStyle();
                        // style.setSize( size / 2 );
                        // l.getBasicStyle().setLineColor( c );
                        // style.setFilling( false );
                        // l.getBasicStyle().setRenderingFill( false );
                        // break;
                        // case 42:
                        // style = new TriangleVertexStyle();
                        // style.setSize( size / 2 );
                        // l.getBasicStyle().setLineColor( c );
                        // style.setFilling( false );
                        // l.getBasicStyle().setRenderingFill( false );
                        // break;
                        // case 49:
                        // style = new CrossVertexStyle();
                        // style.setSize( size / 2 );
                        // l.getBasicStyle().setLineColor( c );
                        // style.setFilling( false );
                        // l.getBasicStyle().setRenderingFill( false );
                        // break;
                        // }

                        if ( style != null ) {
                            style.setEnabled( true );
                            l.removeStyle( l.getVertexStyle() );
                            l.addStyle( style );
                            l.getBasicStyle().setEnabled( false );
                        }
                    }
                }

                l.fireAppearanceChanged();
            }

        }

        return false;
    }

    @Override
    public String getName() {
        return get( "MapInfoStylePlugin.name" );
    }

}
