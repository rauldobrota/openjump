/*
 * Created on 06.12.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.latlon.deejump.plugin;

import javax.swing.JFileChooser;

import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.InstallStandardDataSourceQueryChoosersPlugIn;
import com.vividsolutions.jump.workbench.datasource.LoadFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import de.latlon.deejump.io.DeegreeReaderWriterFileDataSource.DeeGMLFile;
import de.latlon.deejump.io.DeegreeReaderWriterFileDataSource.DeeShapefile;

/**
 * @author hamammi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InstallDeegreeFileAdaptersPlugIn extends AbstractPlugIn {
	
	public void initialize(PlugInContext context) throws Exception {

			final String shpDescrip = "deegree Shapefile adapter";
			final String gmlDescrip = "deegree GML adapter";

			// for SAVing
			
			DataSourceQueryChooserManager.get(
					context.getWorkbenchContext().getWorkbench().getBlackboard())
					.addSaveDataSourceQueryChooser(
							new SaveFileDataSourceQueryChooser(
									DeeShapefile.class,
									shpDescrip,
									InstallStandardDataSourceQueryChoosersPlugIn.extensions(DeeShapefile.class),
									context.getWorkbenchContext()) {
								
								protected void addFileFilters(JFileChooser chooser) {
									super.addFileFilters(chooser);
									InstallStandardDataSourceQueryChoosersPlugIn
											.addCompressedFileFilter(shpDescrip,
													chooser);
								}
							})
							
							.addSaveDataSourceQueryChooser(
							new SaveFileDataSourceQueryChooser(
									DeeGMLFile.class,
									gmlDescrip,
									InstallStandardDataSourceQueryChoosersPlugIn.extensions(DeeGMLFile.class),
									context.getWorkbenchContext()) {
								
								protected void addFileFilters(JFileChooser chooser) {
									super.addFileFilters(chooser);

									InstallStandardDataSourceQueryChoosersPlugIn
											.addCompressedFileFilter(gmlDescrip,
													chooser);
								}
							});

			// for LOADing
			DataSourceQueryChooserManager.get(
					context.getWorkbenchContext().getWorkbench().getBlackboard())
					.addLoadDataSourceQueryChooser(
							new LoadFileDataSourceQueryChooser(
									DeeShapefile.class,
									shpDescrip,
									InstallStandardDataSourceQueryChoosersPlugIn.extensions(DeeShapefile.class),
									context.getWorkbenchContext()) {
								protected void addFileFilters(JFileChooser chooser) {
									super.addFileFilters(chooser);
									InstallStandardDataSourceQueryChoosersPlugIn.
										addCompressedFileFilter(shpDescrip,
													chooser);
								}
							})
							
							.addLoadDataSourceQueryChooser(
							new LoadFileDataSourceQueryChooser(
									DeeGMLFile.class,
									gmlDescrip,
									InstallStandardDataSourceQueryChoosersPlugIn.extensions(DeeGMLFile.class),
									context.getWorkbenchContext()) {
								protected void addFileFilters(JFileChooser chooser) {
									super.addFileFilters(chooser);

									InstallStandardDataSourceQueryChoosersPlugIn
											.addCompressedFileFilter(gmlDescrip,
													chooser);
								}
							});


		}

		/**
		 * This function does nothing, all the setup is completed in initialize().
		 */
		public boolean execute(PlugInContext context) {
			return false;
		}
}
