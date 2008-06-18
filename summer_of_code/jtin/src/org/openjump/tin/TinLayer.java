/**
 * 
 */
package org.openjump.tin;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.WorkbenchContext;


/**
 * @author paradox
 *
 */
public class TinLayer extends AbstractLayerable {

	private Blackboard blackboard = new Blackboard();
	private TriangulatedIrregularNetwork tin;	
	private int srid;
	private WorkbenchContext context;

	/**
	 * 
	 */
	public TinLayer(WorkbenchContext context, String name,
		    LayerManager layerManager, TriangulatedIrregularNetwork tin, int srid) {
	    super(name, layerManager);
	    this.context = context;
	    this.tin = tin;
	    this.srid = srid;
	}
	


	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.workbench.model.Layerable#getBlackboard()
	 */
	public Blackboard getBlackboard() {
		return this.blackboard;
	}
	
	public int getSrid() {
		return this.srid;
	}
	
	public TriangulatedIrregularNetwork getTin() {
		return this.tin;
	}

}
