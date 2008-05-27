package de.latlon.deejump.plugin;

import static com.vividsolutions.jump.workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn.addCompressedFileFilter;
import static com.vividsolutions.jump.workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn.extensions;

import javax.swing.JFileChooser;

import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.LoadFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import de.latlon.deejump.io.DeegreeReaderWriterFileDataSource.DeeGMLFile;
import de.latlon.deejump.io.DeegreeReaderWriterFileDataSource.DeeMapInfoFile;
import de.latlon.deejump.io.DeegreeReaderWriterFileDataSource.DeeShapefile;

/**
 * @author hamammi
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class InstallDeegreeFileAdaptersPlugIn extends AbstractPlugIn {

    @Override
    public void initialize( PlugInContext context )
                            throws Exception {
        final String shpDescrip = "deegree Shapefile adapter";
        final String gmlDescrip = "deegree GML adapter";
        final String mapinfoDescrip = "deegree MapInfo adapter";
        // final String csvDescrip = "deegree CSV adapter";

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

        // for LOADing
        manager.addLoadDataSourceQueryChooser( new LoadFileDataSourceQueryChooser( DeeShapefile.class, shpDescrip,
                                                                                   extensions( DeeShapefile.class ),
                                                                                   context.getWorkbenchContext() ) {
            @Override
            protected void addFileFilters( JFileChooser chooser ) {
                super.addFileFilters( chooser );
                addCompressedFileFilter( shpDescrip, chooser );
            }
        } );

        manager.addLoadDataSourceQueryChooser( new LoadFileDataSourceQueryChooser( DeeGMLFile.class, gmlDescrip,
                                                                                   extensions( DeeGMLFile.class ),
                                                                                   context.getWorkbenchContext() ) {
            @Override
            protected void addFileFilters( JFileChooser chooser ) {
                super.addFileFilters( chooser );
                addCompressedFileFilter( gmlDescrip, chooser );
            }
        } );

        manager.addLoadDataSourceQueryChooser( new LoadFileDataSourceQueryChooser( DeeMapInfoFile.class,
                                                                                   mapinfoDescrip,
                                                                                   extensions( DeeMapInfoFile.class ),
                                                                                   context.getWorkbenchContext() ) {
            @Override
            protected void addFileFilters( JFileChooser chooser ) {
                super.addFileFilters( chooser );
                addCompressedFileFilter( mapinfoDescrip, chooser );
            }
        } );

//        manager.addLoadDataSourceQueryChooser( new LoadFileDataSourceQueryChooser( DeeCSVFile.class, csvDescrip,
//                                                                                   extensions( DeeCSVFile.class ),
//                                                                                   context.getWorkbenchContext() ) {
//            @Override
//            protected void addFileFilters( JFileChooser chooser ) {
//                super.addFileFilters( chooser );
//                addCompressedFileFilter( csvDescrip, chooser );
//            }
//        } );

    }

    /**
     * This function does nothing, all the setup is completed in initialize().
     */
    @Override
    public boolean execute( PlugInContext context ) {
        return false;
    }
}
