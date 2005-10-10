/***********************************************
 * created on 		15.07.2005
 * last modified: 	21.07.2005 : handle for conftype=1 and edgePos = 2 change of last point   
 * 
 * author:			sstein
 * 
 * description:
 *  solves short edge conflict - for smallest edge only 
 *	Algorithm proposed in Agent Delivery D1 but without details<p>
 *  Get resulting geometry using getOutPolygon().
 * *****************
 * TODO: 
 ***********************************************/
package mapgen.algorithms.polygons;


import java.util.Iterator;

import mapgen.geomutilities.LineIntersection;
import mapgen.geomutilities.ModifyPolygonPoints;
import mapgen.geomutilities.TangentAngleFunction;
import mapgen.measures.supportclasses.ShortEdgeConflict;
import mapgen.measures.supportclasses.ShortEdgeConflictList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @description:
 * 
 * @author sstein
 *
 */
public class BuildingOutlineSimplify {
    
    private Polygon inPolygon = null;
    private Polygon outPolygon = null;
    private ShortEdgeConflictList seList = null;
    private double flexInRad = 10.0*Math.PI/180;
    private boolean couldNotSolve = false;
    private boolean problemsEncountered = false;
    private boolean alreadySimple = false;

    //private ArrayList intersectionPoints = new ArrayList();
    
    /**
     * calls calculate() automatically
     * @param geom
     * @param conflictList
     * @param xxx
     */
    public BuildingOutlineSimplify(Polygon geom, ShortEdgeConflictList conflictList, double flexibilityInDegree){
        this.inPolygon = geom;
    	this.outPolygon = (Polygon)geom.clone();
        this.seList = conflictList;
        this.flexInRad = flexibilityInDegree*Math.PI/180;
        //=========
        if ((this.inPolygon.getNumInteriorRing() == 0) &&
        		this.inPolygon.getExteriorRing().getNumPoints() <= 5){
        	System.out.println("BuildingOutlineSimplify: Polygon is already simple!");
        	this.couldNotSolve = true;
        	this.alreadySimple = true;
        }
        else{
        	this.calculate();
        }
    }
    
    public BuildingOutlineSimplify(Polygon geom, ShortEdgeConflictList conflictList){
        this.inPolygon = geom;
        this.seList = conflictList;
        //=========
        if ((this.inPolygon.getNumInteriorRing() == 0) &&
        		this.inPolygon.getExteriorRing().getNumPoints() <= 5){
        	System.out.println("BuildingOutlineSimplify: Polygon is already simple!");
        	this.couldNotSolve = true;
        	this.alreadySimple = true;
        }
        else{
        	this.calculate();
        }
    }
    
    
    /**
     *
     *  
     */
    public void calculate(){
        Polygon tempGeom = (Polygon)this.inPolygon.clone();
        ShortEdgeConflictList unsolvedConflicts = new ShortEdgeConflictList();
        unsolvedConflicts = (ShortEdgeConflictList)seList.clone();
        /*
        for (Iterator iter = seList.iterator(); iter.hasNext();) {
			ShortEdgeConflict element = (ShortEdgeConflict) iter.next();
			unsolvedConflicts.add((ShortEdgeConflict)element.clone());
		}
		*/
        double dx,dy;
        boolean oneMoreTry = true; //needed below to delete crucial situations from list
        						   //but to go on with solveable situations
        while (oneMoreTry == true){ 
        //=> cant use loop over all conflicts since pt-index values 
        //    will change if points are deleted
	        //-- get smallest conflict        
	        ShortEdgeConflict sec = unsolvedConflicts.getStrongestConflict();
	        int index = unsolvedConflicts.getShortestEdgeConflictListIndex();
	        /***************************
	         *  detect edge configuration
	         *  	confType = 1 : stair shape 
	         * 		confType = 2 : u-turn shape
	         *		confType = 3 : perpendicular shape
	         * 		confType = 4 : semi acute angle (spitzer winkel?)  
	         * 		confType = 5 : stair shape but with flat angle middle part
	         * 		confType = 6 : other type : angle to far away from 0°, 90°, 180°
	         * 					   this might be necessary for round buildings	  
	         ***************************/
	        int confType = 0;
	        //-- detect edge configuration using the TWF
	        int edgePos = 0;	
	        int maxPoints = 0; LineString ls = null;
	        	//-- get index of last point
		        if (sec.edgeRingIdx > 0){
		        	ls = this.inPolygon.getInteriorRingN(sec.edgeRingIdx-1);
		        	maxPoints = ls.getNumPoints()-1;
		        }
		        else{//edgeRingIdx == 0
		        	ls = this.inPolygon.getExteriorRing();
		        	maxPoints = ls.getNumPoints()-1;
		        }
		    //---------------------
			// get TAF
			TangentAngleFunction taf = new TangentAngleFunction(ls.getCoordinates());		        
			double[] tafValues = taf.getTaf();
			double[] curvValues = taf.getCurv();
			double[] lengthValues = taf.getDistances();
			/*
			//-- print values			
			System.out.println("TAF of Ring:" + sec.edgeRingIdx);
			for (int i = 0; i < tafValues.length; i++) {
				System.out.print(tafValues[i] + "  ");
			}
			System.out.println(""); System.out.println("Curv");
			for (int i = 0; i < curvValues.length; i++) {
				System.out.print(curvValues[i] + "  ");
			}			
			System.out.println("");
			System.out.println("---");
			*/
			//----------------------
			//  set edge pos type
			//  1 = first edge, 3 = last edge, 2 = middle edge 
			//----------------------
			double edgeChange = 0;
	        if (sec.edgeStartPtIdx == 0){
	        	edgePos = 1;
	    		//first can be used like for normal case since first and last curv
	    		// are stored twice (in beginning and end) 
	        	edgeChange = curvValues[sec.edgeStartPtIdx]+curvValues[sec.edgeEndPtIdx];	    			    		
	        }
	        else if (sec.edgeEndPtIdx == maxPoints){
	        	edgePos = 3;	        	
	    		//last can be used like for normal case since first and last curv
	    		// are stored twice (in beginning and end) 
	        	edgeChange = curvValues[sec.edgeStartPtIdx]+curvValues[sec.edgeEndPtIdx];
	        }
	        else{
	        	edgePos = 2;
	        	edgeChange = curvValues[sec.edgeStartPtIdx]+curvValues[sec.edgeEndPtIdx];
	        }
	        //------------------------------------------------
	        // check if neigbouring edges have also a conflict
	        //-----------------------------------------------
	        int neighbourConflict = -1; //used to save edge index
	        int idx=0;
	        for (Iterator iter = seList.iterator(); iter.hasNext();) {
	        	ShortEdgeConflict element = (ShortEdgeConflict) iter.next();
				//--check edge ahead
	        		//--normal check
					if ((element.edgeRingIdx == sec.edgeRingIdx) && 
							(element.edgeLineIdx == sec.edgeLineIdx-1)){
						neighbourConflict = idx;
					}
					//--check if first edge
					if (edgePos == 1){
						if((element.edgeRingIdx == sec.edgeRingIdx) && (element.edgeLineIdx == ls.getNumPoints()-2)){							
							neighbourConflict = idx;
						}						
					}				
				//--check edge after	
	        		//--normal check							
					if ((element.edgeRingIdx == sec.edgeRingIdx) && 
							(element.edgeLineIdx == sec.edgeLineIdx+1)){
						neighbourConflict = idx;						
					}
					//-- check if last edge
					if (edgePos == 3){
						if((element.edgeRingIdx == sec.edgeRingIdx) && (element.edgeLineIdx == 0)){							
							neighbourConflict = idx;
						}						
					}					
	        	idx=idx+1;
			}	        
			//----------------------
			//  set edge config type
			//----------------------
        	if (Math.abs(edgeChange-0) < this.flexInRad){
        		confType = 1; //stair
        	}
        	else if(Math.abs(edgeChange-Math.PI) < this.flexInRad){
        		confType = 2; //u-turn
        	}
        	else if(Math.abs(edgeChange-Math.PI/2) < this.flexInRad){
        		confType = 3; //perpendicular = 90°
        	}
        	else if((Math.abs(edgeChange) < (3.0/4.0*Math.PI-this.flexInRad))
        			&& (Math.abs(edgeChange) > Math.PI/2)){
        		confType = 4; //semi acute angle 90°-180%
        	}
        	else{
        		confType = 6;
        	}
        	//-- set at the end since the rest uses excluding "else if()"
        	double curv=curvValues[sec.edgeStartPtIdx];
        	if((confType == 1 ) && (Math.abs(curvValues[sec.edgeStartPtIdx]) < Math.PI/4)){
        		confType = 5; // flat stair 
        	}        	
        	System.out.println("RingNo: " + sec.edgeRingIdx);
        	System.out.println("StartPt: " + sec.edgeStartPtIdx + " -- EndPoint: " + sec.edgeEndPtIdx);
        	System.out.println("ConfType: " + confType + " -- EdgePos: " + edgePos);	
        	System.out.println("EdgeChange (rad): " + edgeChange + " -- TwoEdgesInvolved: " + neighbourConflict);
	        /***************************
	         *  delete too short edge
	         ***************************/
        	tempGeom = this.deleteEdge(sec,tempGeom,confType,
        			neighbourConflict, tafValues[sec.edgeStartPtIdx], 
					lengthValues, edgePos);
        	//-- determine how to go on
        	if(this.couldNotSolve == true){
        		this.problemsEncountered = true;
        		System.out.println("conflict could not be solved");
        		//delete conflict from list to proceed with next
    	        unsolvedConflicts.removeByIndex(index);
    	        if (unsolvedConflicts.size() > 0){
    	        	oneMoreTry = true;
    	        }
    	        else{
    	        	oneMoreTry = false;
    	        }
        	}
        	else{ //terminate loop since after solving a new polygon exists
        		//and egde indices have changes 
        		oneMoreTry = false;
        	}
        } //end while
        this.outPolygon = (Polygon)tempGeom.clone();        
    } 
    
    /**
     * does the calculations to solve the problem 
     * deletes edges and calculates new intersections 
     * @param sec : the conflict
     * @param p : the polygon with conflicts 
     * @param confType : the edge configuration
     * @param neighbourConflict : occure neighbour conflicts: where? 
     * @param edgeAngle : the edge angle to the horizontal (obtained from TAF) 
     * @param distances : the distances of every edge
     * @param edgePos	: the edge position
     * @return the modified polygon
     */
    private Polygon deleteEdge(ShortEdgeConflict sec, Polygon p, 
    		int confType, int neighbourConflict, 
			double edgeAngle, double[] distances, int edgePos){    	  	

    	Polygon newP = (Polygon)p.clone();
    	//-- get ring / LineString
    	LineString ls0 = null;
        if (sec.edgeRingIdx > 0){            	
        	ls0 = newP.getInteriorRingN(sec.edgeRingIdx-1);
        }
        else{//edgeRingIdx == 0
        	ls0 = newP.getExteriorRing();
        }
    	int idx2 = sec.edgeEndPtIdx; //idx:e.g. 4
    	int idx1 = sec.edgeStartPtIdx; //idx:e.g. 3 
        /**===================
         * solve for confType == 1 : stair shape
         ===================**/
    	if((confType==1) && (neighbourConflict==-1)){   		 
	    	//-- attention: delete point with higher index first
	    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);
	    	//-- if last point is deleted the second gets the new first:
	    	//   the order changes with idx-1 
	    	if (idx2 == ls0.getNumPoints()-1 ){ //ls0(newP) has now one point less
	    		newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx1-1);
	    	}
	    	else{//normal case
	    		newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx1);
	    	}
	    	//--if number of Rings has changes, don't do the following
	    	if (newP.getNumInteriorRing() == p.getNumInteriorRing()){
		    	//-- displace the other points
		    	LineString ls = null;	    	
	            if (sec.edgeRingIdx > 0){            	
	            	ls = newP.getInteriorRingN(sec.edgeRingIdx-1);
	            }
	            else{//edgeRingIdx == 0
	            	ls = newP.getExteriorRing();
	            }
	            //--
	            if (idx1 > 0){
	            	//lengths to weight point displacement stay always the same
	            	double sideLengthPrev = distances[idx1-1]; //idx:e.g. 3-1=2
	            	double sideLengthAfter = 0;
	            	if (idx2 < distances.length){
	            		sideLengthAfter=distances[idx2]; //idx:e.g. 4
	            	}
	            	else{
	            		sideLengthAfter=distances[0];
	            	}
					double lsum = sideLengthPrev+ sideLengthAfter;				
	            	Coordinate p2 = null;
	            	if (idx1 < ls.getNumPoints()){
	                	Coordinate p1 = ls.getCoordinateN(idx1-1); //idx: e.g. 3-1=2
	                	//-- length weighting between 0 .. 0.5 
	                	//	 to obtain an appropriate change of area  
	                	p1.x = p1.x + (sideLengthAfter/lsum)*sec.length*Math.cos(edgeAngle);
	                	p1.y = p1.y + (sideLengthAfter/lsum)*sec.length*Math.sin(edgeAngle);
	
	            		p2 = ls.getCoordinateN(idx1); //idx:e.g. 4
	                	p2.x = p2.x - (sideLengthPrev/lsum)*sec.length*Math.cos(edgeAngle);
	                	p2.y = p2.y - (sideLengthPrev/lsum)*sec.length*Math.sin(edgeAngle);
	                	//--check if last point has been modified
	                	if(idx1 == ls.getNumPoints()-1){
	                		//modify first point as well
	                		ls.getCoordinateN(0).x = p2.x;
	                		ls.getCoordinateN(0).y = p2.y;
	                	}
		            	//--check if first point has been moved .. if yes move also last point
	                	//  to close ring
		            	if (idx1-1 == 0){
		            		ls.getCoordinateN(ls.getNumPoints()-1).x=ls.getCoordinateN(0).x;
		            		ls.getCoordinateN(ls.getNumPoints()-1).y=ls.getCoordinateN(0).y;
		            	}
	            	}
	            	else{//last edge has been deleted
	                	Coordinate p1 = ls.getCoordinateN(idx1-2); 
	                	//-- length weighting between 0 .. 0.5 
	                	//	 to obtain an appropriate change of area  
	                	p1.x = p1.x + (sideLengthAfter/lsum)*sec.length*Math.cos(edgeAngle);
	                	p1.y = p1.y + (sideLengthAfter/lsum)*sec.length*Math.sin(edgeAngle);
	
	            		//-- modify first point
	            		p2 = ls.getCoordinateN(0);
	                	p2.x = p2.x - (sideLengthPrev/lsum)*sec.length*Math.cos(edgeAngle);
	                	p2.y = p2.y - (sideLengthPrev/lsum)*sec.length*Math.sin(edgeAngle);
	                	//-- reset last point
	                	ls.getCoordinateN(ls.getNumPoints()-1).x = p2.x;
	                	ls.getCoordinateN(ls.getNumPoints()-1).y = p2.y;
	            	}
	            	/**********
	            	 * set variable
	            	 *********/
	            	this.couldNotSolve = false;
	            }
		    	else{
		    		//TODO: implementation if first edge has to be deleted
		    		System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented 1");
		    		this.couldNotSolve = true;
		    	}
	    	}//end if inner Rings has been not deleted
    	}
        /**===================
         * solve for confType == 3 : right angle
         ===================**/
    	else if(((confType == 3) || (confType==4)) && (neighbourConflict==-1)){
    		//System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented solution for right angles");
    		//calc right angle by intersection
    		if(edgePos == 2){ //edge is not first or last edge
    			//--calc intersection  
    			//-- ls0 is obtained from newP not from p
    			Coordinate coord0 = ls0.getCoordinateN(idx1-1);
    			Coordinate coord1 = ls0.getCoordinateN(idx1);
    			Coordinate coord2 = ls0.getCoordinateN(idx2);
    			Coordinate coord3 = ls0.getCoordinateN(idx2+1);    			 
    			try{
    				LineIntersection lint = LineIntersection.intersectionPoint(coord0, coord1, coord2, coord3);
    				Coordinate intersection = lint.getCoordinate();
    				//-- assign coordinates to first point = idx1
    				coord1.x = intersection.x;
    				coord1.y = intersection.y;
    				//-- and delete second point   				
    				newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);
    			}
    			catch(Exception e){
    				System.out.println("BuildingOutlineSimplify.deleteEdge: cant calc intersection for confType=3 edgePos= 2");
    			}    			
    		}
    		else if(edgePos == 3){ //edge is last edge
    			//--calc intersection  
    			//-- ls0 is obtained from newP not from p
    			Coordinate coord0 = ls0.getCoordinateN(idx1-1);
    			Coordinate coord1 = ls0.getCoordinateN(idx1);
    			Coordinate coord2 = ls0.getCoordinateN(idx2); //is last point = is first point (idx=0) 
    			Coordinate coord3 = ls0.getCoordinateN(1);    			 
    			try{
    				LineIntersection lint = LineIntersection.intersectionPoint(coord0, coord1, coord2, coord3);
    				Coordinate intersection = lint.getCoordinate();
    				//-- assign coordinates to first point = idx1
    				coord1.x = intersection.x;
    				coord1.y = intersection.y;
    				//-- and delete second point   				
    				newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);
    			}
    			catch(Exception e){
    				System.out.println("BuildingOutlineSimplify.deleteEdge: cant cals intersection for confType=3 edgePos= 1");
    			}    			
    		}
    		else if(edgePos == 1){ //edge is first edge
    			//--calc intersection  
    			//-- ls0 is obtained from newP not from p
    			Coordinate coord0 = ls0.getCoordinateN(ls0.getNumPoints()-2);
    			Coordinate coord1 = ls0.getCoordinateN(idx1);
    			Coordinate coord2 = ls0.getCoordinateN(idx2); //is last point = is first point (idx=0) 
    			Coordinate coord3 = ls0.getCoordinateN(idx2+1);    			 
    			try{
    				LineIntersection lint = LineIntersection.intersectionPoint(coord0, coord1, coord2, coord3);
    				Coordinate intersection = lint.getCoordinate();
    				//-- assign coordinates to first point = idx1
    				coord1.x = intersection.x;
    				coord1.y = intersection.y;
    				//-- assign to last polygon point = first poly point
    				ls0.getCoordinateN(ls0.getNumPoints()-1).x = coord1.x;
    				ls0.getCoordinateN(ls0.getNumPoints()-1).y = coord1.y;
    				//-- and delete second point   				
    				newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);
    			}
    			catch(Exception e){
    				System.out.println("BuildingOutlineSimplify.deleteEdge: cant cals intersection for confType=3 edgePos= 1");
    			}    			
    		}    		
    		else{
    			System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented solution for confType=3 edgePos= 1");
    		}
    		this.couldNotSolve = false;
    	}
        /**===================
         * solve for confType == 4 : semi acute intersection
         ===================**/
    	//solved upt to now like case 3
    	/*
    	else if(confType == 4){ //this is similar to confType = 3
    		System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented solution for semi acute angles");
    		//TODO semi acute intersection  
    		this.couldNotSolve = true;
    	}
    	*/
    	else if(confType == 5){
            /**===================
             * solve for confType == 5 : flat stair
             ===================**/
    		if(edgePos == 2){ //normal case
		    	//-- attention: delete point with higher index first
		    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);		    	
		    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx1);
    		}
    		if(edgePos == 3){ //last edge
		    	//-- attention: delete point with higher index first
		    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);
		    	//-- if last point is deleted the second gets the new first:
		    	//   the order changes with idx-1 (.deltePoint() closes ring automatically)
	    		newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx1-1);
    		}    		
    		this.couldNotSolve = false;
    	}
    	else if(confType == 2){
            /**===================
             * solve for confType == 2 : u-shape 
             ===================**/
    		//-- check if edge before and after have same length
    		//   if yes => full u-shape
    		double lengthTreshold = distances[idx1-1]*0.1; //10 percent flexibility of one Edge
    							//--	previous edge			subsequent edge
    		double d1 = 0; double d2 = 0;
    		if(edgePos == 2){
    			d1 = distances[idx1-1];
				d2 = distances[idx2];
    		}
    		else if(edgePos == 1){
    			d1 = distances[distances.length-1];
				d2 = distances[idx2];    			
    		}
    		else if(edgePos == 3){
    			d1 = distances[idx1-1];
				d2 = distances[0];    			
    		}
    		double edgediff = Math.abs(d1-d2); 
    		if(edgediff < lengthTreshold){ 
	    		if(edgePos == 2){ //normal case
			    	//-- attention: delete point with higher index first
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);		    	
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx1);
			    	this.couldNotSolve = false;
	    		}
	    		else if(edgePos == 3){ //last edge
			    	//-- attention: delete point with higher index first
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);
			    	//-- if last point is deleted the second gets the new first:
			    	//   the order changes with idx-1 (.deltePoint() closes ring automatically)
		    		newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx1-1);
		    		this.couldNotSolve = false;
	    		}
	    		else if(edgePos == 1){ //first edge
			    	//-- attention: delete point with higher index first
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);
		    		newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, 0);
		    		this.couldNotSolve = false;
	    		}    			    			    		
	    		else{
	    			System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented solution for confType=2a edgePos= ? ");
	    			this.couldNotSolve = true;
	    		}
	    	}
    		// half u-turn : previous side is longer   		
    		else if(d2 < d1){
	    		if(edgePos == 2){ //normal case
	    			//-- calc intersection point 
	    			//-- ls0 is obtained from newP not from p
	    			Coordinate coord0 = ls0.getCoordinateN(idx1-1);
	    			Coordinate coord1 = ls0.getCoordinateN(idx1);
	    			Coordinate coord2 = ls0.getCoordinateN(idx2+1);
	    			Coordinate coord3 = ls0.getCoordinateN(idx2+2);    			 
	    			try{
	    				LineIntersection lint = LineIntersection.intersectionPoint(coord0, coord1, coord2, coord3);
	    				Coordinate intersection = lint.getCoordinate();
	    				//-- assign coordinates to first point = idx1
	    				coord1.x = intersection.x;
	    				coord1.y = intersection.y;
	    			}
	    			catch(Exception e){
	    				System.out.println("BuildingOutlineSimplify.deleteEdge: cant calc intersection for confType=2b edgePos= 2");
	    			}    				    			
			    	//-- attention: delete point with higher index first
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2+1);		    	
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);
			    	this.couldNotSolve = false;
	    		}
	    		else{
	    			System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented solution for confType=2b edgePos= 1&3");
	    			this.couldNotSolve = true;
	    		}	    		
    		}
    		else if(d2 > d1){
	    		if(edgePos == 2){ //normal case
	    			//-- calc intersection point 
	    			//-- ls0 is obtained from newP not from p
	    			Coordinate coord0 = ls0.getCoordinateN(idx1-2);
	    			Coordinate coord1 = ls0.getCoordinateN(idx1-1);
	    			Coordinate coord2 = ls0.getCoordinateN(idx2);
	    			Coordinate coord3 = ls0.getCoordinateN(idx2+1);    			 
	    			try{
	    				LineIntersection lint = LineIntersection.intersectionPoint(coord0, coord1, coord2, coord3);
	    				Coordinate intersection = lint.getCoordinate();
	    				//-- assign coordinates to first point = idx1
	    				coord1.x = intersection.x;
	    				coord1.y = intersection.y;
	    			}
	    			catch(Exception e){
	    				System.out.println("BuildingOutlineSimplify.deleteEdge: cant calc intersection for confType=2c edgePos= 2");
	    			}    				    			
			    	//-- attention: delete point with higher index first
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx2);		    	
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idx1);
			    	this.couldNotSolve = false;
	    		}
	    		else{
	    			System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented solution for confType=2c edgePos= 1&3");
	    			this.couldNotSolve = true;
	    		}
    		}
    		else{// distances[idx2] < = > distances[idx1-1]
    			//should never reach here
	    		this.couldNotSolve = true;
    		}
    	}    	
        /**===================
         * solve for confType == x
         ===================**/
    	else{//conf type
    		System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented 2");
    		this.couldNotSolve = true;
    	}
    	return newP;
    }
        
    /*************** getters and setters ****************/
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
        
    /*
    public ArrayList getIntersectionPoints() {
        return intersectionPoints;
    }
    */
	/**
	 * @return Returns the flexInRad.
	 */
	public double getFlexInRad() {
		return flexInRad;
	}
	/**
	 * @param flexInRad The flexInRad to set.
	 */
	public void setFlexInRad(double flexInRad) {
		this.flexInRad = flexInRad;
	}
	/**
	 * @return Returns boolean couldNotSolve.
	 * 		   used to delete not solveable conflicts from list 
	 */
	public boolean isProblemsEncountered() {
		return problemsEncountered;
	}
	/**
	 * is simple means the polygon has only four edges 
	 * @return
	 */
    public boolean isAlreadySimple() {
        return alreadySimple;
    }
}
