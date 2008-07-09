package de.latlon.deejump.base.plugin;

import static com.vividsolutions.jump.workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn.addCompressedFileFilter;
import static com.vividsolutions.jump.workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn.extensions;
import static de.latlon.deejump.base.i18n.I18N.get;
import static java.util.Arrays.asList;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.openjump.core.ui.io.file.FileLayerLoader.KEY;

import java.awt.Dialog;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.deegree.framework.log.ILogger;
import org.deegree.io.csv.CSVReader;
import org.openjump.core.ui.io.file.DataSourceFileLayerLoader;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.registry.Registry;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.OKCancelDialog;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;

import de.latlon.deejump.base.io.DeegreeReaderWriterFileDataSource.DeeCSVFile;
import de.latlon.deejump.base.io.DeegreeReaderWriterFileDataSource.DeeGMLFile;
import de.latlon.deejump.base.io.DeegreeReaderWriterFileDataSource.DeeMapInfoFile;
import de.latlon.deejump.base.io.DeegreeReaderWriterFileDataSource.DeeShapefile;
import de.latlon.deejump.base.ui.CSVSelectionPanel;

/**
 * @author hamammi
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class InstallDeegreeFileAdaptersPlugIn extends AbstractPlugIn {

    static final ILogger LOG = getLogger( InstallDeegreeFileAdaptersPlugIn.class );

    @Override
    public void initialize( final PlugInContext context )
                            throws Exception {

        final String shpDescrip = "deegree Shapefile adapter";
        final String gmlDescrip = "deegree GML adapter";

        Registry registry = context.getWorkbenchContext().getRegistry();

        registry.createEntry( KEY,
                              new DataSourceFileLayerLoader( context.getWorkbenchContext(), DeeShapefile.class,
                                                             shpDescrip, asList( extensions( DeeShapefile.class ) ) ) );

        registry.createEntry( KEY, new DataSourceFileLayerLoader( context.getWorkbenchContext(), DeeGMLFile.class,
                                                                  gmlDescrip, asList( extensions( DeeGMLFile.class ) ) ) );

        registry.createEntry( KEY, new DataSourceFileLayerLoader( context.getWorkbenchContext(), DeeMapInfoFile.class,
                                                                  "deegree MapInfo adapter",
                                                                  asList( extensions( DeeMapInfoFile.class ) ) ) );

        registry.createEntry( KEY, new DataSourceFileLayerLoader( context.getWorkbenchContext(), DeeCSVFile.class,
                                                                  "deegree CSV adapter",
                                                                  asList( extensions( DeeCSVFile.class ) ) ) {
            @Override
            public boolean open( TaskMonitor monitor, URI uri, Map<String, Object> options ) {

                CSVReader reader;
                try {
                    String fileRoot = uri.toURL().getFile();
                    reader = new CSVReader( ',', fileRoot );
                } catch ( IOException e ) {
                    LOG.logError( "Unknown error", e );
                    WorkbenchFrame workbenchFrame = context.getWorkbenchFrame();
                    HTMLFrame outputFrame = workbenchFrame.getOutputFrame();
                    outputFrame.createNewDocument();
                    workbenchFrame.warnUser( I18N.get( "datasource.LoadDatasetPlugIn.problems-were-encountered" ) );
                    return false;
                }

                List<String[]> header = reader.getHeader();

                CSVSelectionPanel panel = new CSVSelectionPanel( header );
                Dialog parent = new JDialog();
                parent.setLocationRelativeTo( context.getWorkbenchFrame() );
                OKCancelDialog dlg = new OKCancelDialog( parent, get( "General.question" ), true, panel, null );
                dlg.setVisible( true );

                if ( dlg.wasOKPressed() ) {
                    // have to put Strings here, else the Hashtable#putAll does not copy them...
                    int wkt = panel.getWKTColumn();
                    if ( wkt != -1 ) {
                        options.put( "wkt", "" + wkt );
                    }
                    options.put( "xcol", "" + panel.getXColumn() );
                    options.put( "ycol", "" + panel.getYColumn() );
                    return super.open( monitor, uri, options );
                }

                return false;
            }
        } );

        // for SAVing
        DataSourceQueryChooserManager manager = DataSourceQueryChooserManager.get( context.getWorkbenchContext().getWorkbench().getBlackboard() );

        manager.addSaveDataSourceQueryChooser( new SaveFileDataSourceQueryChooser( DeeShapefile.class, shpDescrip,
                                                                                   extensions( DeeShapefile.class ),
                                                                                   context.getWorkbenchContext() ) {

            @Override
            protected void addFileFilters( JFileChooser chooser ) {
                super.addFileFilters( chooser );
                addCompressedFileFilter( shpDescrip, chooser );
            }
        } );

        manager.addSaveDataSourceQueryChooser( new SaveFileDataSourceQueryChooser( DeeGMLFile.class, gmlDescrip,
                                                                                   extensions( DeeGMLFile.class ),
                                                                                   context.getWorkbenchContext() ) {

            @Override
            protected void addFileFilters( JFileChooser chooser ) {
                super.addFileFilters( chooser );
                addCompressedFileFilter( gmlDescrip, chooser );
            }
        } );

    }

}
