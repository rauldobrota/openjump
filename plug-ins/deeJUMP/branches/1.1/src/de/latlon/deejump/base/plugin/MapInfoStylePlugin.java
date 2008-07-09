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

package de.latlon.deejump.base.plugin;

import static com.vividsolutions.jump.workbench.ui.MenuNames.LAYER;
import static com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn.get;
import static de.latlon.deejump.base.i18n.I18N.get;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.openjump.core.ui.plugin.style.ImportSLDPlugIn.importSLD;
import static org.openjump.util.SLDImporter.NSCONTEXT;
import static org.openjump.util.SLDImporter.OGCNS;
import static org.openjump.util.SLDImporter.SLDNS;
import static org.openjump.util.XPathUtils.getElement;
import static org.openjump.util.XPathUtils.getElements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JFileChooser;

import org.deegree.framework.log.ILogger;
import org.deegree.io.mapinfoapi.MIFStyle2SLD;
import org.deegree.io.mapinfoapi.MapInfoReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * <code>MapInfoStylePlugin</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class MapInfoStylePlugin extends AbstractPlugIn {

    private static final ILogger LOG = getLogger( MapInfoStylePlugin.class );

    private static File POINTS;

    static {
        try {
            POINTS = File.createTempFile( "points", ".zip" );
            POINTS.deleteOnExit();

            InputStream in = MIFStyle2SLD.class.getResourceAsStream( "points.zip" );
            FileOutputStream out = new FileOutputStream( POINTS );

            byte[] buf = new byte[16384];

            int read;
            if ( in != null ) {
                while ( ( read = in.read( buf ) ) != -1 ) {
                    out.write( buf, 0, read );
                }
                in.close();
            }
            out.close();
        } catch ( IOException e ) {
            LOG.logError( "Could not find the points zip file", e );
        }
    }

    @Override
    public void initialize( PlugInContext context ) {
        EnableCheckFactory enableCheckFactory = new EnableCheckFactory( context.getWorkbenchContext() );

        MultiEnableCheck enableCheck = new MultiEnableCheck();
        enableCheck.add( enableCheckFactory.createWindowWithLayerManagerMustBeActiveCheck() );
        enableCheck.add( enableCheckFactory.createExactlyNLayerablesMustBeSelectedCheck( 1, Layerable.class ) );

        context.getFeatureInstaller().addMainMenuItem( this, new String[] { LAYER },
                                                       get( "MapInfoStylePlugin.name" ) + "{pos:13}", false, null,
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

            MapInfoReader reader = new MapInfoReader( f.getAbsolutePath() );
            reader.parseFeatures();

            MIFStyle2SLD mif2sld = new MIFStyle2SLD( MIFStyle2SLD.class.getResource( "MAPSYM.TTF" ) );
            Document doc = mif2sld.getStyle( reader.getStyles(), f.getName() ).getRootElement().getOwnerDocument();

            context.getLayerNamePanel().getSelectedLayers()[0].getBasicStyle().setEnabled( false );

            // rework the SLD to be able to import it...
            Element root = doc.getDocumentElement();
            Element style = getElement( "//sld:FeatureTypeStyle", root, NSCONTEXT );

            List<Element> lines = getElements( "//sld:Rule[sld:LineSymbolizer]", root, NSCONTEXT );
            List<Element> polys = getElements( "//sld:Rule[sld:PolygonSymbolizer]", root, NSCONTEXT );
            for ( Element e : lines ) {
                Element filter = getElement( "ogc:Filter/ogc:Or", e, NSCONTEXT );
                filter.getParentNode().removeChild( filter );
                style.removeChild( e );
            }
            for ( Element e : polys ) {
                Element filter = getElement( "ogc:Filter/ogc:Or", e, NSCONTEXT );
                filter.getParentNode().removeChild( filter );
                style.removeChild( e );
            }

            for ( Element line : lines ) {
                for ( Element poly : polys ) {
                    Element rule = doc.createElementNS( SLDNS, "sld:Rule" );
                    Element name = doc.createElementNS( SLDNS, "sld:Name" );
                    String styleid = getElement( "sld:Name", line, NSCONTEXT ).getTextContent() + "_"
                                     + getElement( "sld:Name", poly, NSCONTEXT ).getTextContent();
                    name.setTextContent( styleid );
                    rule.appendChild( name );
                    Element filter = doc.createElementNS( OGCNS, "ogc:Filter" );
                    Element cond = doc.createElementNS( OGCNS, "ogc:PropertyIsEqualTo" );
                    filter.appendChild( cond );
                    rule.appendChild( filter );
                    Element pn = doc.createElementNS( OGCNS, "ogc:PropertyName" );
                    pn.setTextContent( "app:styleid" );
                    cond.appendChild( pn );
                    pn = doc.createElementNS( OGCNS, "ogc:Literal" );
                    pn.setTextContent( styleid );
                    cond.appendChild( pn );

                    rule.appendChild( getElement( "sld:LineSymbolizer", line, NSCONTEXT ).cloneNode( true ) );
                    rule.appendChild( getElement( "sld:PolygonSymbolizer", poly, NSCONTEXT ).cloneNode( true ) );

                    style.appendChild( rule );
                }
            }

            importSLD( doc, context );
        }

        return false;
    }

    @Override
    public String getName() {
        return get( "MapInfoStylePlugin.name" );
    }

}
