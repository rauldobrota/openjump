package org.openjump.tin.renderer.style;

import org.openjump.tin.TriangulatedIrregularNetwork;
import java.awt.Graphics2D;
//import javax.swing.Icon;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;


/**
 * Must have a parameterless constructor so it can be created by Java2XML.
 */
public interface TinStyle extends Cloneable {
    public void paint(TriangulatedIrregularNetwork tin, Graphics2D g, Viewport viewport)
        throws Exception;

    /**
     * Called before #paint is applied to each Feature.
     * @return false if #paint should not be called e.g. because vertices are not
     * shown. Don't need to check whether the layer is visible.
     */
    public void initialize(Layer layer);

    public Object clone();

    public void setEnabled(boolean enabled);

    public boolean isEnabled();

}
