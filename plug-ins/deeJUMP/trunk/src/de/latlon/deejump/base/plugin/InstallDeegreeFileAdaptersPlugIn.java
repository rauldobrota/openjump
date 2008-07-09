package de.latlon.deejump.base.plugin;

import static com.vividsolutions.jump.workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn.addCompressedFileFilter;
import static com.vividsolutions.jump.workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn.extensions;
import static java.util.Arrays.asList;
import static org.openjump.core.ui.io.file.FileLayerLoader.KEY;

import javax.swing.JFileChooser;

import org.openjump.core.ui.io.file.DataSourceFileLayerLoader;

import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.registry.Registry;

import de.latlon.deejump.base.io.DeegreeReaderWriterFileDataSource.DeeCSVFile;
import de.latlon.deejump.base.io.DeegreeReaderWriterFileDataSource.DeeGMLFile;
import de.latlon.deejump.base.io.DeegreeReaderWriterFileDataSource.DeeMapInfoFile;
import de.latlon.deejump.base.io.DeegreeReaderWriterFileDataSource.DeeShapefile;

/**
 * @author hamammi
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
public class InstallDeegreeFileAdaptersPlugIn extends AbstractPlugIn {

    @Override
    public void initialize( PlugInContext context )
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
                                                                  asList( extensions( DeeCSVFile.class ) ) ) );

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
