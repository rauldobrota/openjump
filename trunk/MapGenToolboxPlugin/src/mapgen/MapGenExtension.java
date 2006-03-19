/*
 * Created on 10.01.2005
 *
 */
package mapgen;
import mapgen.ui.onselecteditems.ChangeElongationSelectedBuildingPlugIn;
import mapgen.ui.onselecteditems.DisplaceLinesPlugIn;
import mapgen.ui.onselecteditems.EliminatePointsInLineOfBuildingPlugIn;
import mapgen.ui.onselecteditems.EliminateSmallBuildingsPlugIn;
import mapgen.ui.onselecteditems.BuildingSpreadNarrowPartsPlugIn;
import mapgen.ui.onselecteditems.EnlargeBuildingToRectanglePlugIn;
import mapgen.ui.onselecteditems.LineSimplifyJTS15AlgorithmPlugIn;
import mapgen.ui.onselecteditems.LineSmoothSimpleVersionPlugIn;
import mapgen.ui.onselecteditems.MergeSelectedPolygonsPlugIn;
import mapgen.ui.onselecteditems.SimplifyBuildingToRectanglePlugIn;
import mapgen.ui.onselecteditems.SimplifyOutlineSelectedBuildingPlugIn;
import mapgen.ui.onselecteditems.SquareBuildingPlugIn;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * @author sstein
 *
 *  - this class loads the PlugIn into Jump 
 *
 *  - class has to be called "Extension" on the end of classname
 *    to use the PlugIn in Jump 
 */
public class MapGenExtension extends Extension{

	/**
	 * calls PlugIn using class method xplugin.initialize() 
	 */
	public void configure(PlugInContext context) throws Exception{
		
		new BuildingSpreadNarrowPartsPlugIn().initialize(context);
		new EnlargeBuildingToRectanglePlugIn().initialize(context);
		new SquareBuildingPlugIn().initialize(context);
		new EliminateSmallBuildingsPlugIn().initialize(context);
		new EliminatePointsInLineOfBuildingPlugIn().initialize(context);
		new SimplifyOutlineSelectedBuildingPlugIn().initialize(context);
		new SimplifyBuildingToRectanglePlugIn().initialize(context);
		new ChangeElongationSelectedBuildingPlugIn().initialize(context);
		new DisplaceLinesPlugIn().initialize(context);
		new LineSmoothSimpleVersionPlugIn().initialize(context);
		new LineSimplifyJTS15AlgorithmPlugIn().initialize(context);
		new MergeSelectedPolygonsPlugIn().initialize(context);
		
	}
	
}
