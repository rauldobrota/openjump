/**
 * 
 */
package org.openjump.tin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Triangle;

/**
 * Wrapper class for JTS Triangle that includes extra methods applicible to TIN analysis
 * @author paradox
 *
 */
public class TinTriangle extends Triangle {

	/**
	 * @param p0
	 * @param p1
	 * @param p2
	 */
	public TinTriangle(Coordinate p0, Coordinate p1, Coordinate p2) {
		super(p0, p1, p2);
		// TODO Auto-generated constructor stub
	}

}
