/**
 * @author Eric Lemesre eric.lemesre@gmail.com
 * Direction          : AGFFinanceConseil
 * Direction Régional : Paris Normandie Centre
 * 
 * 2 nov. 07
 */
package net.refractions.postgis;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.openjump.core.ui.plugin.file.open.ChooseProjectPanel;
import org.openjump.core.ui.swing.wizard.AbstractWizardGroup;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.coordsys.CoordinateSystemRegistry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;
import com.vividsolutions.jump.workbench.ui.wizard.WizardDialog;

/**
 * @author Eric Lemesre
 *
 */
public class PostGISOpenWizard extends AbstractWizardGroup {
	public static final String KEY = PostGISOpenWizard.class.getName();
	
	private AddPostGISLayerWizardPanel postGISWizardPanel;
	
	private WorkbenchContext workbenchContext;

	private ChooseProjectPanel chooseProjectPanel;
	/**
	 * 
	 */
	public PostGISOpenWizard(WorkbenchContext workbenchContext) {
		super(I18N.get(KEY),new ImageIcon(PostGISOpenWizard.class.getResource("stock_elephant_060.gif")),
				AddPostGISLayerWizardPanel.class.getName());
	    this.workbenchContext = workbenchContext;
		postGISWizardPanel = new AddPostGISLayerWizardPanel();
		addPanel(postGISWizardPanel);
	    chooseProjectPanel = new ChooseProjectPanel(workbenchContext,
	    		postGISWizardPanel.getID());
	    addPanel(chooseProjectPanel);
	}

	public String getFirstId() {
		String firstId = super.getFirstId();
		if (!chooseProjectPanel.hasActiveTaskFrame()
				&& chooseProjectPanel.hasTaskFrames()) {
			chooseProjectPanel.setNextID(firstId);
			return chooseProjectPanel.getID();
		} else {
			return firstId;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openjump.core.ui.swing.wizard.WizardGroup#run(com.vividsolutions.jump.workbench.ui.wizard.WizardDialog,
	 *      com.vividsolutions.jump.task.TaskMonitor)
	 */
	public void run(WizardDialog dialog, TaskMonitor monitor) {
		chooseProjectPanel.activateSelectedProject();

		if (dialog.wasFinishPressed()) {
			try {
				final Layer layer = createLayer(postGISWizardPanel, monitor);
	
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Collection<Category> selectedCategories = workbenchContext.getLayerNamePanel()
							.getSelectedCategories();
						LayerManager layerManager = workbenchContext.getLayerManager();
						String categoryName = StandardCategoryNames.WORKING;
						if (!selectedCategories.isEmpty()) {
							categoryName = selectedCategories.iterator().next().getName();
						}
						layerManager.addLayer(categoryName, layer);
					}
				});
			} catch (Exception e) {
				monitor.report(e);
			}
		}
	}

	private String getQuery() {
		StringBuffer sql = new StringBuffer();
		Map properties = postGISWizardPanel.getProperties();

		sql.append(
				"SELECT * " + 
				"FROM " + (String)properties.get(PostGISDataSource.TABLE_KEY)  
		);

		if (!properties.get(PostGISDataSource.WHERE_KEY).equals("")){
			sql.append(
					" WHERE " + (String)properties.get(PostGISDataSource.WHERE_KEY)
			);
		}
		return sql.toString();
	}
	
	private Layer createLayer (final AddPostGISLayerWizardPanel panel,TaskMonitor monitor) throws Exception {
		String datasetName = panel.getDatasetName();
		
	    LayerManager layerManager = workbenchContext.getLayerManager();
	    Color fillColor = layerManager.generateLayerFillColor();
	    FeatureCollection featureCollection = AddNewLayerPlugIn.createBlankFeatureCollection();
	    Layer layer = new Layer(datasetName, fillColor, featureCollection, layerManager);

		String query = getQuery();
	    PostGISDataSourceQuery pgQuery = new PostGISDataSourceQuery(new PostGISDataSource(),query,datasetName);
	    pgQuery.setProperties(panel.getProperties());

	    layer.setDataSourceQuery(pgQuery);

	    CoordinateSystemRegistry crsRegistry = CoordinateSystemRegistry.instance(workbenchContext.getBlackboard());
	    load(layer, crsRegistry);
	    return layer;
	}
	
	public static void load(Layer layer, CoordinateSystemRegistry registry) throws Exception {
		layer.setFeatureCollection(executeQuery(layer.getDataSourceQuery()
				.getQuery(), layer.getDataSourceQuery().getDataSource(), registry));
		layer.setFeatureCollectionModified(false);
	}

	private static FeatureCollection executeQuery (String query,
			DataSource dataSource, CoordinateSystemRegistry registry) throws Exception {
		PostGISConnection connection = (PostGISConnection) dataSource.getConnection();
		try {
			return dataSource.installCoordinateSystem(connection.executeQuery(query), registry);
		} finally {
			connection.close();
		}		
	}
}
