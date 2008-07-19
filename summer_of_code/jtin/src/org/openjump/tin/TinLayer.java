/**
 * 
 */
package org.openjump.tin;

import org.openjump.tin.renderer.style.*;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



/**
 * @author paradox
 *
 */
public class TinLayer extends AbstractLayerable {

	private Blackboard blackboard = new Blackboard();
	private TriangulatedIrregularNetwork tin;	
	private int srid;
	private WorkbenchContext context;
	private ArrayList<TinStyle> styles = new ArrayList<TinStyle>();


	/**
	 * 
	 */
	public TinLayer(WorkbenchContext context, String name,
		    LayerManager layerManager, TriangulatedIrregularNetwork tin, int srid) {
	    super(name, layerManager);
	    this.context = context;
	    this.tin = tin;
	    this.srid = srid;
	    
		boolean firingEvents = layerManager.isFiringEvents();

		try {
			addStyle(new BasicTinStyle());
			addStyle(new TinLineStyle());
		} finally {
			layerManager.setFiringEvents(firingEvents);
		}
	}
	


	public void addStyle(TinStyle style) {
		styles.add(style);
		//fireAppearanceChanged();
	}	
	
	public BasicTinStyle getBasicStyle() {
		return (BasicTinStyle) getStyle(BasicTinStyle.class);
	}

	public TinLineStyle getLineStyle() {
		return (TinLineStyle) getStyle(TinLineStyle.class);
	}

	/**
	 * Styles do not notify the Layer when their parameters change. Therefore,
	 * after you modify a Style's parameters (for example, the fill colour of
	 * BasicStyle), be sure to call #fireAppearanceChanged
	 *
	 * @param c
	 *            Can even be the desired Style's superclass or interface
	 * @return The style value
	 */
	public TinStyle getStyle(Class c) {
		for (Iterator<TinStyle> i = styles.iterator(); i.hasNext();) {
			TinStyle p = (TinStyle) i.next();

			if (c.isInstance(p)) {
				return p;
			}
		}

		return null;
	}

	public List<TinStyle> getStyles() {
		return Collections.unmodifiableList(styles);
	}
	

	public Blackboard getBlackboard() {
		return this.blackboard;
	}
	
	public int getSRID() {
		return this.srid;
	}
	
	public TriangulatedIrregularNetwork getTin() {
		return this.tin;
	}

}
