/***********************************************
 * created on 		12.12.2005
 * last modified: 	
 * 
 * author:			sstein
 * 
 * description:
 *  describes a point which lies perceptual on a line of the
 *  previous and next point
 * 
 ***********************************************/
package mapgen.measures.supportclasses;

/**
 * @author sstein
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PointInLineConflict implements Cloneable{

	public int pointRingIdx = 0;
    public int pointIdx = 0;
    public double distance = 0; //point to line(previous-next)

    public Object clone() {
        try {
          PointInLineConflict clone = (PointInLineConflict)super.clone();          
          return clone;
        }
        catch (CloneNotSupportedException e) {
          System.out.println("ShortEdgeConflict: Error in clone() method");
          return null;
        }
      }
    
}
