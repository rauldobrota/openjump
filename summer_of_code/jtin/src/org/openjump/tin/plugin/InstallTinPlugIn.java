/**
 * 
 */
package org.openjump.tin.plugin;

import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.InstallRendererPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer.Factory;

/**
 * @author paradox
 *
 */
public class InstallTinPlugIn extends InstallRendererPlugIn {

	/**
	 * @param contentID
	 * @param aboveLayerables
	 */
	public InstallTinPlugIn(Object contentID, boolean aboveLayerables) {
		super(contentID, aboveLayerables);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.ui.plugin.InstallRendererPlugIn#createFactory(com.vividsolutions.jump.workbench.ui.TaskFrame)
	 */
	@Override
	protected Factory createFactory(TaskFrame frame) {
		// TODO Auto-generated method stub
		return null;
	}

}
