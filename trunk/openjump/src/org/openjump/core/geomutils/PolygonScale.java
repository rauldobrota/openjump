/***********************************************
 * created on 		09.01.2005
 * last modified: 	
 * 
 * author:			sstein
 * 
 * description:
 *  Scales a polygon along x and y axis
 *  with the polygon centroid as center.<p>
 *  Uses StretchPolygon.stretchPolygon() and StretchPolygon.rotate() methods  
 * 
 ***********************************************/
package org.openjump.core.geomutils;


import com.vividsolutions.jts.geom.Polygon;

/**
 * @description:
 *  Scales a polygon along x and y axis
 *  with the polygon centroid as center. <p>
 *  Uses StretchPolygon.stretchPolygon() and StretchPolygon.rotate() methods  
 *
 * @author sstein
 *
 */
public class PolygonScale {
       
    /**
     *  Scales a polygon along x and y axis
     *  with the polygon centroid as center. 
     *
     * @param geom
     * @param scale factor (1 = no scaling)
     */
	public static void scalePolygon(Polygon geom, double scale){
	    //-- scale on horizontal axis
	    StretchGeometry.stretchPolygon(geom,0,scale);
	    //-- scale on vertical axis
	    StretchGeometry.stretchPolygon(geom,Math.PI/2.0,scale);	    
	}
}
