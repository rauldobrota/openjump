/***********************************************
 * created on 		15.07.2005
 * last modified: 	21.07.2005 : handle for conftype=1 and edgePos = 2 change of last point
 * 					23.11.2005 : outPoly=inPoly(clone) for second constructor to avoid errors if not calculating anything 
 * 								 confType 6 and PolyRingIndex introduced, clone of inPoly    
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
import mapgen.measures.supportclasses.ShortEdgeConflict;
import mapgen.measures.supportclasses.ShortEdgeConflictList;

import ch.unizh.geo.geomutilities.TangentAngleFunction;

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
        this.inPolygon = (Polygon)geom.clone();
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
        this.inPolygon = (Polygon)geom.clone();
    	this.outPolygon = (Polygon)geom.clone();
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
        //-- make a second copy since if we delete a conflict which is not solvable
        //   he can not be used anymore to discover adjacent conflicts        
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
	         * 		confType = 6 : a straight element (start or end point have curvature ~0)
	         * 		confType = 7 : other type : angle to far away from 0°, 90°, 180°
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
			/**
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
			**/
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
	        // 1 : previous has also conflict
	        // 2 : next has conflict
	        // 3 : previous and next have conflict
	        //-----------------------------------------------
	        int neighbourConflict = -1; //used to save edge index
	        int idx=0;
	        for (Iterator iter = seList.iterator(); iter.hasNext();) {
	        	ShortEdgeConflict element = (ShortEdgeConflict) iter.next();
				//--check edge ahead
	        		//--normal check
					if ((element.edgeRingIdx == sec.edgeRingIdx) && 
							(element.edgeLineIdx == sec.edgeLineIdx-1)){
						neighbourConflict = 1;
					}
					//--check if first edge
					if (edgePos == 1){
						if((element.edgeRingIdx == sec.edgeRingIdx) && (element.edgeLineIdx == ls.getNumPoints()-2)){							
							neighbourConflict = 1;
						}						
					}				
				//--check edge after	
	        		//--normal check							
					if ((element.edgeRingIdx == sec.edgeRingIdx) && 
							(element.edgeLineIdx == sec.edgeLineIdx+1)){
						if(neighbourConflict == 1){
							neighbourConflict = 3;
						}				
						else{
							neighbourConflict = 2;
						}
					}
					//-- check if last edge
					if (edgePos == 3){
						if((element.edgeRingIdx == sec.edgeRingIdx) && (element.edgeLineIdx == 0)){							
							if(neighbourConflict == 1){
								neighbourConflict = 3;
							}				
							else{
								neighbourConflict = 2;
							}
						}
					}					
	        	idx=idx+1;
			}//end check of adjacent problem edges	        
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
        		confType = 7;
        	}
        	//-- set at the end since the rest uses excluding "else if()"
        	double curv=curvValues[sec.edgeStartPtIdx];
        	if((confType == 1 ) && (Math.abs(curvValues[sec.edgeStartPtIdx]) < Math.PI/4)){
        		confType = 5; // flat stair 
        	}
        	if((Math.abs(curvValues[sec.edgeStartPtIdx]) < this.flexInRad) ||
        			(Math.abs(curvValues[sec.edgeEndPtIdx]) < this.flexInRad)){
        		confType = 6; // start or end point is vertex on a line 
        					  // no edge direction change
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
					lengthValues, curvValues, edgePos);
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
     * @param neighbourConflict : occure neighbour conflicts: where? prev:1 next:2 both:3 
     * @param edgeAngle : the edge angle to the horizontal (obtained from TAF) 
     * @param distances : the distances of every edge
     * @param curvs : the curvature values
     * @param edgePos	: the edge position
     * @return the modified polygon
     */
    private Polygon deleteEdge(ShortEdgeConflict sec, Polygon p, 
    		int confType, int neighbourConflict, 
			double edgeAngle, double[] distances, double[] curvs, int edgePos){    	  	

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
    	//-- new: 23.11.2005 
    	PolyRingIndex idxE = new PolyRingIndex(sec.edgeEndPtIdx,ls0.getNumPoints());
    	PolyRingIndex idxS = new PolyRingIndex(sec.edgeStartPtIdx,ls0.getNumPoints());
    	//-----
        /**===================
         * solve for confType == 1 : stair shape
         *===================**/
	    if((confType==1) && (neighbourConflict==-1)){
	    	//-- do a weighting since the affected edges might be very long compared to
	    	//   our too short edge
        	// lengths to weight point displacement stay always the same
        	double sideLengthPrev = distances[idxS.getPrevious()]; //idx:e.g. 3-1=2
        	double sideLengthAfter = 0;
        	if (idxE.i < distances.length){
        		sideLengthAfter=distances[idxE.i]; //idx:e.g. 4
        	}
        	else{
        		sideLengthAfter=distances[0];
        	}
			double lsum = sideLengthPrev+ sideLengthAfter;				
        	Coordinate p2 = null;
        	Coordinate p1 = ls0.getCoordinateN(idxS.getPrevious()); //idx: e.g. 3-1=2
            //-- length weighting between 0 .. 0.5 
            //	 to obtain an appropriate change of area  
            double x1 = p1.x + (sideLengthAfter/lsum)*sec.length*Math.cos(edgeAngle);
            double y1 = p1.y + (sideLengthAfter/lsum)*sec.length*Math.sin(edgeAngle);
            this.setRingCoordinate(idxS.getPrevious(),ls0,x1,y1);

       		p2 = ls0.getCoordinateN(idxE.getNext()); //idx:e.g. 4
           	double x2 = p2.x - (sideLengthPrev/lsum)*sec.length*Math.cos(edgeAngle);
           	double y2 = p2.y - (sideLengthPrev/lsum)*sec.length*Math.sin(edgeAngle);
            this.setRingCoordinate(idxE.getNext(),ls0,x2,y2);
            //-- delete points
            newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.i);
            newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxS.i);
        	this.couldNotSolve = false;
    	}
    	else if ((confType==1) && (neighbourConflict!=-1)){
    			//-- stair shape but at least two adjacent edges are to small
    		    //   no weighting required, since all edges are to short
    			//   solution: (corner) flip the inner point
    			if(neighbourConflict == 1){    				
    				//-- previous edge has also problem
        			//--calc intersection; ls0 is obtained from newP not from p
    				//-- check first if prev edge and next edge have nearly right angle (k=pi/2+pi/2)
    				double kappa = Math.abs(curvs[idxS.getPrevious()]) + Math.abs(curvs[idxE.i]);
    				if (Math.abs(kappa-Math.PI/2) < this.flexInRad){
    					//lines ar nearly parallel
    					this.couldNotSolve = true;
    				}
    				else{//lines are nearly orthogonal
	        			Coordinate coord0 = ls0.getCoordinateN(idxS.getPP());
	        			Coordinate coord1 = ls0.getCoordinateN(idxS.getPrevious());
	        			Coordinate coord2 = ls0.getCoordinateN(idxE.i);
	        			Coordinate coord3 = ls0.getCoordinateN(idxE.getNext());    			 
	        			try{
	        				LineIntersection lint = LineIntersection.intersectionPoint(coord0, coord1, coord2, coord3);
	        				Coordinate intersection = lint.getCoordinate();
	        				//-- assign coordinates to first point-1
	        				this.setRingCoordinate(idxS.i, ls0,intersection.x, intersection.y);
	        				//-- and delete second and first point (starting with point of later pos)   				
	        				newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.i);
	        				// point coord1 (now in the line of coord0 and cord2) will hopefully be deleted 
	        				// later in a second loop
	        				//--eliminate spikes by checking if shape was something like a house merged with garage
	        				//			 ____
	        				//	 _______|   _|
	        				//	|          |
	        				//  curvs with same sign and argument 
	        				if ( this.sign(curvs[idxS.i]) == this.sign(curvs[idxS.getPrevious()])){
	        					//   not first     +     not last => not the ring node
	        					if ((idxS.i != 0) && (idxE.i != ls0.getNumPoints()-1)){
	        						newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxS.getPrevious());
	        					}
	        					else{//since point E has been deleted, the index of last point has changed 
	        						newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxS.getPrevious()-1);
	        					}					
	        				}							
	        			}
	        			catch(Exception e){
	        				System.out.println("BuildingOutlineSimplify.deleteEdge: cant calc intersection for confType=1ab");
	        			}      				
	    				this.couldNotSolve = false;
    				}
    			}
    			else if(neighbourConflict == 2){
    				//-- next edge has also problem
        			//--calc intersection; ls0 is obtained from newP not from p
    				double kappa = Math.abs(curvs[idxS.i]) + Math.abs(curvs[idxE.getNext()]);
    				if (Math.abs(kappa-Math.PI/2) < this.flexInRad){
    					//lines ar nearly parallel
    					this.couldNotSolve = true;
    				}
    				else{//lines are nearly orthogonal
	        			Coordinate coord0 = ls0.getCoordinateN(idxS.getPrevious());
	        			Coordinate coord1 = ls0.getCoordinateN(idxS.i);
	        			Coordinate coord2 = ls0.getCoordinateN(idxE.getNext());
	        			Coordinate coord3 = ls0.getCoordinateN(idxE.getNN());    			 
	        			try{
	        				LineIntersection lint = LineIntersection.intersectionPoint(coord0, coord1, coord2, coord3);
	        				Coordinate intersection = lint.getCoordinate();
	        				//-- assign coordinates to first point-1        				
	        				this.setRingCoordinate(idxE.i, ls0,intersection.x, intersection.y);
	        				//-- and delete second and first point (starting with point of later pos)   				
	        				newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.getNext());
	        				// point coord1 (now in the line of coord0 and cordxxx) will hopefully be deleted 
	        				// later in a second loop        				
	        			}
	        			catch(Exception e){
	        				System.out.println("BuildingOutlineSimplify.deleteEdge: cant calc intersection for confType=3 edgePos= 2");
	        			}      				
	    				this.couldNotSolve = false;
    				}
    			}
    			else if(neighbourConflict == 3){
    				//-- previous and next edge have a problem
    				//   delete the detail of the building but check configuration
    				//  if edge corners have same sign (=> convex/concave, => dont use single ABS(curv)) 
    				//  and are both about pi/2 it is a oriel (Erker)  
    				double kappa = Math.abs(curvs[idxS.i] + curvs[idxE.i]);
    				if (Math.abs(kappa-Math.PI) < this.flexInRad){
    					//-- lines are nearly parallel: delete the edge: hopefully the rest is 
    					//   solved in a later iteration using confType=7 
        				newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.i);
        				newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxS.i);
    					this.couldNotSolve = false;
    				}
    				else if(Math.abs(kappa-0) < this.flexInRad){
    					//-- lines are like a stair : flip start corner
        				//-- check first if prev edge and next edge have nearly right angle (k=pi/2+pi/2)
        				double kappa2 = Math.abs(curvs[idxS.getPrevious()]) + Math.abs(curvs[idxE.i]);
        				if (Math.abs(kappa2-Math.PI/2) < this.flexInRad){
        					//lines ar nearly parallel
        					this.couldNotSolve = true;
        					System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented ConfType=1 neighbourConflict=3a");
        				}
        				else{//lines are nearly orthogonal
        					//weighting is not really necessary
        		        	// lengths to weight point displacement stay always the same
        		        	double sideLengthPrev = distances[idxS.getPrevious()]; //idx:e.g. 3-1=2
        		        	double sideLengthAfter = 0;
        		        	if (idxE.i < distances.length){
        		        		sideLengthAfter=distances[idxE.i]; //idx:e.g. 4
        		        	}
        		        	else{
        		        		sideLengthAfter=distances[0];
        		        	}
        					double lsum = sideLengthPrev+ sideLengthAfter;				
        		        	Coordinate p2 = null;
        		        	Coordinate p1 = ls0.getCoordinateN(idxS.getPrevious()); //idx: e.g. 3-1=2
        		            //-- length weighting between 0 .. 0.5 
        		            //	 to obtain an appropriate change of area  
        		            double x1 = p1.x + (sideLengthAfter/lsum)*sec.length*Math.cos(edgeAngle);
        		            double y1 = p1.y + (sideLengthAfter/lsum)*sec.length*Math.sin(edgeAngle);
        		            this.setRingCoordinate(idxS.getPrevious(),ls0,x1,y1);

        		       		p2 = ls0.getCoordinateN(idxE.getNext()); //idx:e.g. 4
        		           	double x2 = p2.x - (sideLengthPrev/lsum)*sec.length*Math.cos(edgeAngle);
        		           	double y2 = p2.y - (sideLengthPrev/lsum)*sec.length*Math.sin(edgeAngle);
        		            this.setRingCoordinate(idxE.getNext(),ls0,x2,y2);
        		            //-- delete points
        		            newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.i);
        		            newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxS.i);
        		        	this.couldNotSolve = false;
        				}    					
    				}
    				else{
    					System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented ConfType=1 neighbourConflict=3");
    					this.couldNotSolve = true;
    				}
    			}    			   			
    	}
        /**===================
         * solve for confType == 3 : right angle
         *===================**/
    	else if(((confType == 3) || (confType==4)) && (neighbourConflict==-1)){
    		//System.out.println("BuildingOutlineSimplify.deleteEdge: not implemented solution for right angles");
    		//calc right angle by intersection
    		{
    			//--calc intersection  
    			//-- ls0 is obtained from newP not from p
    			Coordinate coord0 = ls0.getCoordinateN(idxS.getPrevious());
    			Coordinate coord1 = ls0.getCoordinateN(idxS.i);
    			Coordinate coord2 = ls0.getCoordinateN(idxE.i);
    			Coordinate coord3 = ls0.getCoordinateN(idxE.getNext());    			 
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
    		this.couldNotSolve = false;
    	}
        /**===================
         * solve for confType == 4 : semi acute intersection
         *===================**/
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
             *===================**/
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
             *===================**/
    		//-- check if edge before and after have same length
    		//   if yes => full u-shape
    		double lengthTreshold = 0;
   			lengthTreshold = distances[idxS.getPrevious()]*0.1; //10 percent flexibility of one Edge
			//--	d1: previous edge, d2: subsequent edge
    		double d1 = 0; double d2 = 0;
   			d1 = distances[idxS.getPrevious()];
			d2 = distances[idxE.i];
    		double edgediff = Math.abs(d1-d2); 
    		if(edgediff < lengthTreshold){ 
			    	//-- attention: delete point with higher index first
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.i);		    	
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxS.i);
			    	this.couldNotSolve = false;
	    	}
    		// half u-turn : previous side is longer   		
    		else if(d2 < d1){
	    			//-- calc intersection point 
	    			//-- ls0 is obtained from newP not from p
	    			Coordinate coord0 = ls0.getCoordinateN(idxS.getPrevious());
	    			Coordinate coord1 = ls0.getCoordinateN(idxS.i);
	    			Coordinate coord2 = ls0.getCoordinateN(idxE.getNext());
	    			Coordinate coord3 = ls0.getCoordinateN(idxE.getNN());    			 
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
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.getNext());		    	
			    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.i);
			    	this.couldNotSolve = false;
    		}
    		else if(d2 > d1){
    			Coordinate coord0=null; Coordinate coord1=null;
    			Coordinate coord2=null; Coordinate coord3=null;
	    			//-- calc intersection point 
	    			//-- ls0 is obtained from newP not from p
	    			coord0 = ls0.getCoordinateN(idxS.getPP()); 
	    			coord1 = ls0.getCoordinateN(idxS.getPrevious());
	    			coord2 = ls0.getCoordinateN(idxE.i);	
	    			coord3 = ls0.getCoordinateN(idxE.getNext());
    			try{
    				LineIntersection lint = LineIntersection.intersectionPoint(coord0, coord1, coord2, coord3);
    				Coordinate intersection = lint.getCoordinate();
    				//-- assign coordinates to first point = idx1
    				coord1.x = intersection.x;
    				coord1.y = intersection.y;
    			}
    			catch(Exception e){
    				System.out.println("BuildingOutlineSimplify.deleteEdge: cant calc intersection for confType=2c");
    			}    				    			
		    	//-- attention: delete point with higher index first
		    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.i);		    	
		    	newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxS.i);
		    	this.couldNotSolve = false;	    				    		
    		}
    		else{// distances[idx2] < = > distances[idx1-1]
    			//should never reach here
	    		this.couldNotSolve = true;
    		}
    	}    	
    	else if(confType == 6){
            /**===================
             * solve for confType == 6 : start or end point are a vertex on the line curv ~ 0  
             *===================**/
    		//-- check which point has curv=0
    		//   delete this point : since length is (too) small the displacement error will be
    		//                       hardly visible
    		if (Math.abs(curvs[idxS.i]) < this.flexInRad){
    			newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxS.i);
		    	this.couldNotSolve = false;
    		}
    		else if(Math.abs(curvs[idxE.i]) < this.flexInRad){
    			newP = ModifyPolygonPoints.deletePoint(newP,sec.edgeRingIdx, idxE.i);
		    	this.couldNotSolve = false;
    		}
    		else{
    			System.out.println("BuildingOutlineSimplify.deleteEdge: classification misstake no confType 6!!!");
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
        
    private void setRingCoordinate(int index, LineString ls, double x, double y){
    	int numPoints= ls.getNumPoints();
		ls.getCoordinateN(index).x = x;
		ls.getCoordinateN(index).y = y;
		if(index == 0){
			ls.getCoordinateN(numPoints-1).x = x;
			ls.getCoordinateN(numPoints-1).y = y;
		}
		if(index == numPoints-1){
			ls.getCoordinateN(0).x = x;
			ls.getCoordinateN(0).y = y;			
		}
    }  
    
    /**
     * 
     * @param value
     * @return signum(x)=-1 smaller zero and signum(x)=+1 larger zero; 
     */
    private int sign(double value){
    	int retval = 0;
    	if(value > 0){
    		retval = 1;   		
    	}
    	if(value < 0){
    		retval = -1;   		
    	}    	
    	return retval;
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
