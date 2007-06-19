/***********************************************
 * created on 		07.07.2005
 * last modified: 	
 * 
 * author:			sstein
 * 
 * description:
 *  describes a minimal length conflict of an edge
 * 
 ***********************************************/
package mapgen.measures.supportclasses;

/**
 * @author sstein
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ShortEdgeConflict implements Cloneable{

	public int edgeRingIdx = 0;
    public int edgeLineIdx = 0;
    public int edgeStartPtIdx = 0;
    public int edgeEndPtIdx = 0;    
    public double length = 0;

    public Object clone() {
        try {
          ShortEdgeConflict clone = (ShortEdgeConflict)super.clone();          
          return clone;
        }
        catch (CloneNotSupportedException e) {
          System.out.println("ShortEdgeConflict: Error in clone() method");
          return null;
        }
      }
    
}
