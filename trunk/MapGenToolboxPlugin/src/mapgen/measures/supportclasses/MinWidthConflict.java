/***********************************************
 * created on 		20.12.2004
 * last modified: 	
 * 
 * author:			sstein
 * 
 * description:
 *  describes a minimal distance conflict among
 *  a point and an egde or one point of the edge
 * 
 ***********************************************/
package mapgen.measures.supportclasses;

/**
 * @author sstein
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MinWidthConflict implements Cloneable{

    public int ptHoleIdx = 0;
    public int ptIdx = 0;
    public int edgeHoleIdx = 0;
    public int edgeStartPtIdx = 0;
    public int edgeEndPtIdx = 0;    
    public double distance = 0;
    public double ptDispDx = 0;
    public double ptDispDy = 0;
    public boolean ptEdgeConflict = true;

    public Object clone() {
        try {
          MinWidthConflict clone = (MinWidthConflict)super.clone();          
          return clone;
        }
        catch (CloneNotSupportedException e) {
          System.out.println("MinWidthConflict: Error in clone() method");
          return null;
        }
      }
    
}
