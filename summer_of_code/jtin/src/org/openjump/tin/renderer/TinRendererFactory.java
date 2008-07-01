package org.openjump.tin.renderer;

import org.openjump.tin.TinLayer;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;
import com.vividsolutions.jump.workbench.ui.renderer.RendererFactory;

public class TinRendererFactory implements RendererFactory<TinLayer> {

	public Renderer create(final TinLayer layer, 
			final LayerViewPanel layerViewPanel, final int maxFeatures) {
		return new TinRenderer(layer, layerViewPanel);
	}

}
