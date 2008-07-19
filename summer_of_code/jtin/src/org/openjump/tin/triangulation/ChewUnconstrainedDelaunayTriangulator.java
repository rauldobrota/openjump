/**
 * 
 */
package org.openjump.tin.triangulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.openjump.tin.io.JTFLayout;
import org.openjump.tin.triangulation.delaunaySimplexInsert.DelaunayTriangulation;
import org.openjump.tin.triangulation.delaunaySimplexInsert.Simplex;
import org.openjump.tin.triangulation.delaunaySimplexInsert.Pnt;
import org.openjump.tin.ImmutableTin;
import org.openjump.tin.TriangulatedIrregularNetwork;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.Assert;




/**
 * @author paradox
 *
 */
public class ChewUnconstrainedDelaunayTriangulator implements
		DelaunayTriangulator {

    private DelaunayTriangulation dt;     // The Delaunay triangulation
    private Simplex initialTriangle;      // The large initial triangle
//    private int initialSize = 10000;      // Controls size of initial triangle
    private double dx=0;
    private double dy=0;
    private Pnt lowerLeftPnt = null;
    public boolean debug = false;         // True iff printing info for debugging
    private HashMap<Pnt, Double> zValues;

    
    
    public ChewUnconstrainedDelaunayTriangulator(ArrayList<Point> pointList) {
        double argmaxx = 0; double argmaxy = 0;
        double argminx = 0; double argminy = 0;
        int count = 0;
        //-- calc coordinates of initial symplex
        for (Iterator<Point> iter = pointList.iterator(); iter.hasNext();) {
            Point pt = iter.next();
            if (count==0){
                argmaxx = pt.getX(); argminx = pt.getX();
                argmaxy = pt.getY(); argminy = pt.getY();
            }
            else{
                if (pt.getX() < argminx){argminx = pt.getX();}
                if (pt.getX() > argmaxx){argmaxx = pt.getX();}
                if (pt.getY() < argminy){argminy = pt.getY();}
                if (pt.getY() > argmaxy){argmaxy = pt.getY();}                
            }
            count++;
        }
        this.dx=argmaxx-argminx;
        this.dy=argmaxy-argminy;
        //-- the initial simplex must contain all points
        //-- take the bounding box, move the diagonals (sidewards) 
        //	 the meeting point will be the mirrored bbox-center on the top edge  
        this.initialTriangle = new Simplex(new Pnt[] {
    	        				new Pnt(argminx-(1.5*dx), argminy-dy), //lower left
    	        				new Pnt(argmaxx+(1.5*dx), argminy-dy), //lower right
        						new Pnt(argminx+(dx/2.0), argmaxy+1.5*dy)});	//center, top
        
        this.lowerLeftPnt = new Pnt(argminx-(1.5*dx), argminy-dy);
        this.zValues = new HashMap<Pnt,Double>(pointList.size());
        this.dt = new DelaunayTriangulation(initialTriangle);
        this.addPoints(pointList);
    }
    
    
    public ChewUnconstrainedDelaunayTriangulator(ArrayList<Point> pointList, Envelope envelope){  
        double argmaxx = 0; double argmaxy = 0;
        double argminx = 0; double argminy = 0;
        int count = 0;
        //-- calc coordinates of initial symplex
        for (Iterator<Point> iter = pointList.iterator(); iter.hasNext();) {
            Point pt = iter.next();
            if (count==0){
                argmaxx = pt.getX(); argminx = pt.getX();
                argmaxy = pt.getY(); argminy = pt.getY();
            }
            else{
                if (pt.getX() < argminx){argminx = pt.getX();}
                if (pt.getX() > argmaxx){argmaxx = pt.getX();}
                if (pt.getY() < argminy){argminy = pt.getY();}
                if (pt.getY() > argmaxy){argmaxy = pt.getY();}                
            }
            count++;
        }
        //-- do check also for the delivered envelope
        	if (envelope.getMinX() < argminx){argminx = envelope.getMinX();}
        	if (envelope.getMaxX() > argmaxx){argmaxx = envelope.getMaxX();}
        	if (envelope.getMinY() < argminy){argminy = envelope.getMinY();}
        	if (envelope.getMaxY() > argmaxy){argmaxy = envelope.getMaxY();}                        
        //--
        this.dx=argmaxx-argminx;
        this.dy=argmaxy-argminy;
        //-- the initial simplex must contain all points
        //-- take the bounding box, move the diagonals (sidewards) 
        //	 the meeting point will be the mirrored bbox-center on the top edge  
        this.initialTriangle = new Simplex(new Pnt[] {
    	        				new Pnt(argminx-(1.5*dx), argminy-dy), //lower left
    	        				new Pnt(argmaxx+(1.5*dx), argminy-dy), //lower right
        						new Pnt(argminx+(dx/2.0), argmaxy+1.5*dy)});	//center, top
        
        this.lowerLeftPnt = new Pnt(argminx-(1.5*dx), argminy-dy);
        this.zValues = new HashMap<Pnt,Double>(pointList.size());
        this.dt = new DelaunayTriangulation(initialTriangle);
        this.addPoints(pointList);
    }
    
    
    
	public void addPoint(double x, double y) {       
		Pnt point = new Pnt(x, y);	   
		if (debug) System.out.println("Click " + point);	   
		dt.delaunayPlace(point);
	}

	public void addPoint(double x, double y, double z) {
		Pnt point = new Pnt(x, y);	   
		this.zValues.put(point, new Double(z));
		if (debug) System.out.println("Click " + point);	   
		dt.delaunayPlace(point);
	}

	public void addPoints(ArrayList<Point> pointList) {
       for (Point jtsPt : pointList) {
            try{
            	Coordinate jtsCoord = jtsPt.getCoordinate();
            	if (debug) System.out.println("current Coordinate: "+jtsCoord.toString());
            	Pnt point = new Pnt(jtsPt.getX(), jtsPt.getY());
            	this.zValues.put(point, new Double(jtsCoord.z));

            	if (debug) System.out.println("current Pnt: "+point.toString());
            	dt.delaunayPlace(point);
             }
            catch(Exception e){
                if (debug) System.out.println("no geometry: "+e);
            }
        }
	}


	public TriangulatedIrregularNetwork getTin() {
		return getTin(0);
	}


	public TriangulatedIrregularNetwork getTin(int SRID) {
		int[][] triTable = new int[dt.size()][JTFLayout.NUM_TRIANGLE_INT_FIELDS];
		HashSet<Pnt> pntSet = new HashSet<Pnt>(dt.size());
		HashMap<Pnt, Integer> pntToIndexMap = new HashMap<Pnt, Integer>(dt.size());
		HashMap<Simplex, Integer> simplexToIndexMap = new HashMap<Simplex, Integer>(dt.size());
		
		//For each simplex: 
		///for each vertex: 
		////add Pnt to pntSet
		for (Iterator<Simplex> it = dt.iterator(); it.hasNext();) {
			Simplex tmpTri = it.next();
			for (Iterator<Pnt> itTwo = tmpTri.iterator(); itTwo.hasNext();) {
				pntSet.add(itTwo.next());
			}
		}
		
		//convert pntSet to an pntArray
		Pnt[] pntArray = new Pnt[pntSet.size()];
		pntSet.toArray(pntArray);

		//go through pntArray  
		///add the <Pnt, index> to pntToIndexMap
		///make points[i] = to pntArray[i]		
		Coordinate[] points = new Coordinate[pntArray.length];
		for (int i=0; i<pntArray.length; i++) {
			pntToIndexMap.put(pntArray[i], new Integer(i));
			if (this.zValues.containsKey(pntArray[i]))
				points[i] = new Coordinate(pntArray[i].coord(0), pntArray[i].coord(1), this.zValues.get(pntArray[i]).doubleValue());
			else
				points[i] = new Coordinate(pntArray[i].coord(0), pntArray[i].coord(1), 0);
		}
		
		//For each simplex
		///add <Simplex, index> to simplexToIndexMap
		///add vertexes to triTable[index] through lookup in pntToIndexMap
		int index = 0;
		for (Iterator<Simplex> it = dt.iterator(); it.hasNext(); index++) {
			Simplex tmpTri = it.next();
			Assert.isTrue(tmpTri.size()==3, "Simplex #"+index+": illegal number of vertexes - "+tmpTri.size());
			simplexToIndexMap.put(tmpTri, index);
			
			Iterator<Pnt> itTwo = tmpTri.iterator();

			Assert.isTrue(itTwo.hasNext(), "No vertexes in Simplex #"+index);
			if (debug) System.out.println("Simplex #"+index);
			triTable[index][JTFLayout.TRITABLE_VERTEX_0] = pntToIndexMap.get(itTwo.next()).intValue();
			Assert.isTrue(itTwo.hasNext(), "Only one vertex in Simplex #"+index);
			triTable[index][JTFLayout.TRITABLE_VERTEX_1] = pntToIndexMap.get(itTwo.next()).intValue();
			Assert.isTrue(itTwo.hasNext(), "Only two vertexes in Simplex #"+index);
			triTable[index][JTFLayout.TRITABLE_VERTEX_2] = pntToIndexMap.get(itTwo.next()).intValue();
		}
			
		//For each simplex
		///add neighbors to triTable[index] through lookup in simplexToIndexMap
		index = 0;
		for (Iterator<Simplex> it = dt.iterator(); it.hasNext(); index++) {
			Simplex tmpTri = it.next();
			
			Iterator<Pnt> itTwo = tmpTri.iterator();
			
			Simplex n0 = dt.neighborOpposite(itTwo.next(), tmpTri);
			Simplex n1 = dt.neighborOpposite(itTwo.next(), tmpTri);
			Simplex n2 = dt.neighborOpposite(itTwo.next(), tmpTri);

			int n0idx = (n0 == null) ? -1 : simplexToIndexMap.get(n0).intValue();
			int n1idx = (n1 == null) ? -1 : simplexToIndexMap.get(n1).intValue();
			int n2idx = (n2 == null) ? -1 : simplexToIndexMap.get(n2).intValue();
			
			if (debug) System.out.println("Simplex #"+index+": n0idx = "+n0idx+"\tn1idx = "+n1idx+"\tn2idx = "+n2idx);
			
			triTable[index][JTFLayout.TRITABLE_NEIGHBOR_0] = n0idx;
			triTable[index][JTFLayout.TRITABLE_NEIGHBOR_1] = n1idx;
			triTable[index][JTFLayout.TRITABLE_NEIGHBOR_2] = n2idx;
		}
		
		return new ImmutableTin(points, triTable, null, null, SRID);
	}
	
	public TriangulatedIrregularNetwork getTin(int SRID, Envelope envelope) {
		TriangulatedIrregularNetwork largeTin = getTin(SRID);
		ImmutableTin returnTin = new ImmutableTin(largeTin.getStrictSubsetTriangles(envelope),
				largeTin.getBreaklines(), largeTin.getBoundaries(), SRID);
		return returnTin;
	}


}
