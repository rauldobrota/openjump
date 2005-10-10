/***********************************************
 * created on 		07.01.2005
 * last modified: 	
 * 
 * author:			sstein
 * 
 * description:
 *  change elongation of a polygon through scaling along
 *  one axis only. Size of geom is changed along the length axis.<p>
 *	Algorithm by Mats Bader (Agent Delivery D1).<p>
 *  Get resulting geometry using getOutPolygon().
 * 
 ***********************************************/
package mapgen.ui.onselecteditems;

import mapgen.measures.OrientationMBR;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @description:
 *  change elongation of a polygon through scaling along
 *  one axis only. Size of geom is changed along the length axis.<p>
 *	Algorithm by Mats Bader (Agent Delivery D1).<p>
 *  Get resulting geometry using getOutPolygon().
 * 
 * @author sstein
 *
 */
public class PolygonChangeElongation {
    
    private Polygon inPolygon = null;
    private Polygon outPolygon = null;
    private Point center = null;
    private double scale = 1;
    private double angle = 0;
    
    /**
     * calls calculate() automatically; center is polygon centroid;
     * angle is given by "wall statistical weigth".
     * @param geom
     * @param scalefactor : amount of elongation
     */
    public PolygonChangeElongation(Polygon geom, double scaleFactor){
        this.inPolygon = geom;
        this.scale = scaleFactor;
        //=== calcualte angle and center point ===
        OrientationMBR orient = new OrientationMBR(this.inPolygon);
        this.angle = orient.getStatOrientation();
        this.center = this.inPolygon.getCentroid();
        //=========
        this.calculate();
    }

    /**
     * calls calculate() automatically; center is polygon centroid
     * @param geom
     * @param scaleFactor : amount of elongation
     * @param angleInRad : axis of elongation, to horizontal
     */
    public PolygonChangeElongation(Polygon geom, double scaleFactor, double angleInRad){
        this.inPolygon = geom;
        this.scale = scaleFactor;
        this.angle = angleInRad;
        this.center = this.inPolygon.getCentroid();        
        this.calculate();        
    }
    /**
     * calls calculate() automatically 
     * @param geom
     * @param scaleFactor : amount of elongation
     * @param angleInRad : axis of elongation, to horizontal
     * @param center : fixed point
     */
    public PolygonChangeElongation(Polygon geom, double scaleFactor, double angleInRad, Point center){
        this.inPolygon = geom;
        this.scale = scaleFactor;
        this.angle = angleInRad;
        this.center = center;
        this.calculate();
    }
    
    public void calculate(){
        Polygon tempGeom = (Polygon)this.inPolygon.clone();
        Coordinate[] coords = tempGeom.getCoordinates();
        double x, y, k, dx,dy;
        for (int i = 0; i < coords.length; i++) {
            x = coords[i].x;
            y = coords[i].y;
            if (this.angle < Math.PI/2.0){
                k = (y - center.getY())*Math.sin(this.angle) + (x - center.getX())*Math.cos(this.angle);
                dx = k*Math.cos(this.angle);
                dy = k*Math.sin(this.angle);
            }
            else{
                k = -1*(y - center.getY())*Math.cos(this.angle - Math.PI/2.0) + 
                		(x - center.getX())*Math.sin(this.angle-Math.PI/2.0);
                dx = k*Math.cos(Math.PI - this.angle);
                dy = -1*k*Math.sin(Math.PI - this.angle);                
            }
			coords[i].x=x+ (dx * (this.scale -1));
			coords[i].y=y+ (dy * (this.scale -1));            
            
        }
        this.outPolygon = (Polygon)tempGeom.clone();
    }    
    /*************** getters and setters ****************/
    public double getAngle() {
        return angle;
    }
    public void setAngle(double angle) {
        this.angle = angle;
    }
    public Point getCenter() {
        return center;
    }
    public void setCenter(Point center) {
        this.center = center;
    }
    public Polygon getInPolygon() {
        return inPolygon;
    }
    public void setInPolygon(Polygon inPolygon) {
        this.inPolygon = inPolygon;
    }
    public Polygon getOutPolygon() {
        return outPolygon;
    }
    public void setOutPolygon(Polygon outPolygon) {
        this.outPolygon = outPolygon;
    }
    public double getScale() {
        return scale;
    }
    public void setScale(double scale) {
        this.scale = scale;
    }
}
