/***********************************************
 * created on 		22 oct. 2004
 * last modified: 	
 * 
 * author:			marius (IGN)
 * 
 * description:
 * 	moves a polygon (with holes) or multi polygon
 * 
 ***********************************************/
package mapgen.algorithms.polygons;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @description:
 *   moves a polygon (with holes) or multi polygon
 * 
 * @author marius (IGN) + sstein
 *
 */
public class TranslatePolygons {
    
	public static void translate(Polygon geom, double dx, double dy){
		//scale the shell of the polygon
		Coordinate[] coord=geom.getExteriorRing().getCoordinates();
		for(int i=0;i<coord.length;i++){
			coord[i].x=coord[i].x+dx;
			coord[i].y=coord[i].y+dy;
		}
		//scale the holes of the polygon
		for(int j=0;j<geom.getNumInteriorRing();j++){
			Coordinate[] coord2=geom.getInteriorRingN(j).getCoordinates();
			for(int i=0;i<coord2.length;i++){
				coord2[i].x=coord2[i].x+dx;
				coord2[i].y=coord2[i].y+dy;
			}
		}
		return;
	}
	
	public static void translate(MultiPolygon geom, double dx, double dy){
		for(int i=0;i<geom.getNumGeometries();i++){
			translate((Polygon)geom.getGeometryN(i), dx, dy);
		}
		return;
	}
	
	public static void translate(Polygon geom, Point newCenter){
	    Point oldCenterPoint = geom.getCentroid(); 
	    double dx = newCenter.getX() - oldCenterPoint.getX(); 
	    double dy = newCenter.getY() - oldCenterPoint.getY();
	    translate(geom, dx, dy);
	}
	
	
}
