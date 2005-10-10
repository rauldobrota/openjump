/***********************************************
 * created on 		20.12.2004
 * last modified: 	
 * 
 * author:			sstein
 * 
 * description:
 *  Special ArrayList for objects of type MinWidthConflict
 * 
 ***********************************************/
package mapgen.measures.supportclasses;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author sstein
 *
 * Special ArrayList for objects of type MinWidthConflict  
 * 
 */
public class MWConflictList{

    ArrayList conflicts;
    
    /**
     * Constructs a new, empty MinWidthConflict list.
     * 
     * All elements inserted into the List must implement the Comparable interface. Furthermore, 
     * all such elements must be mutually comparable: e1.compareTo(e2) must not throw a ClassCastException 
     * for any elements e1 and e2 in the List.
     */    
    public MWConflictList(){
        conflicts = new ArrayList();
    }
    
    /**
     * Adds the specified element to this List.
     * 
     * All elements inserted into the PriorityQueue must implement the Comparable interface. Furthermore, 
     * all such elements must be mutually comparable: e1.compareTo(e2) must not throw a ClassCastException 
     * for any elements e1 and e2 in the List.
     * 
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(MinWidthConflict conflict){
        return conflicts.add(conflict);
    }
    
    /** 
     * Removes all elements from the priority queue.
     * @see java.util.Collection#clear()
     */
    public void clear(){
        conflicts.clear();
    }    
    
    public int size(){
        return conflicts.size();
    }
    

    public boolean remove(MinWidthConflict conflict){
        return conflicts.remove(conflict);
    }
    
    public ArrayList getList(){        
        return conflicts;
    }    

    public MinWidthConflict get(int index){
        return (MinWidthConflict)conflicts.get(index);        
    }
 
    public Iterator iterator(){
        return conflicts.iterator();
    }

    /**
     * 
     * @return strongest conflict, means smallest distance
     */
    public MinWidthConflict getStrongestConflict(){
    	MinWidthConflict smallestMc = null;
    	try{
	        //--init
	        smallestMc = (MinWidthConflict)this.conflicts.get(0);
	        double smallestDist = smallestMc.distance;
	        //--search
	        for (int i = 1; i < this.conflicts.size(); i++) {
	            MinWidthConflict mc = (MinWidthConflict)this.conflicts.get(i);
	            if(mc.distance < smallestDist){
	                smallestDist = mc.distance;
	                smallestMc = (MinWidthConflict)mc.clone();
	            }
	        }
		}
		catch(Exception e){
			System.out.println("MWConflictList: getStrongestConflict .. not possible => no conflicts");
		}
        return smallestMc;
    }
}
