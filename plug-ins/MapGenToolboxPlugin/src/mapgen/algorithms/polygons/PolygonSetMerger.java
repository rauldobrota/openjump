/***********************************************
 * created on 		13.03.2006
 * last modified: 	
 * 
 * author:			sstein
 * 
 * description:
 * 	merges a set of polyons. The merge is done with 
 * the PolygonMerge class based on JTS-union#.
 ***********************************************/
package mapgen.algorithms.polygons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import mapgen.algorithms.jts17qtree.Quadtree;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * @description: 
 * 
 * @author sstein
 *  
 */
public class PolygonSetMerger {

	/**
	 * Does a union of all touching or intersection geometries.
	 * In geomsQtree and in polys are only geometries of type Polygon allowed.<p>
	 * the (jump)monitor can can be null. 
	 * @param polys
	 * @param geomsQtree
	 * @param monitor
	 * @return merged polygon geometries
	 */
	public static ArrayList mergeGeoms(ArrayList polys,	
									Quadtree geomsQtree, TaskMonitor monitor) {
		
		int totalsize = polys.size();
		ArrayList newgeoms = new ArrayList();
		ArrayList singlePolys = new ArrayList();
		while (polys.isEmpty() == false) {
			if (monitor != null){
				monitor.report(polys.size() + " / " + totalsize + " items left");
			}
			ArrayList tempList = new ArrayList();
			int count = 0;
			for (Iterator iter = polys.iterator(); iter.hasNext();) {
				Polygon g = (Polygon) iter.next();
				//-- remove from tree
				boolean foundInTree = geomsQtree.remove(
						g.getEnvelopeInternal(), g);
				if (foundInTree == false) {
					// item is not in tree anymore, so it has been merged already
					// so dont do anything; the item will not be on the new list
				} else {
					//-- get list of candidates
					List candidates = geomsQtree.query(g.getEnvelopeInternal());
					boolean oneFound = false;
					//-- check every candidate
					for (int i = 0; i < candidates.size(); i++) {
						Geometry cand = (Polygon) candidates.get(i);
						PolygonMerge merge = new PolygonMerge(g, cand);
						if (merge.isMergeSuccesfull() == 1) {
							oneFound = true;
							geomsQtree.remove(cand.getEnvelopeInternal(), cand); // remove from tree
							g = (Polygon) merge.getOutPolygon();
						}
						if (monitor != null){
							String msg = "candidate: " + i;
							monitor.report((polys.size() - count) + " / " + 
									totalsize + " items left. -- " + msg);
						}
					}
					if (oneFound == false) {
						//-- this is a single poly
						singlePolys.add(g);
					} else {//since the object may have further neighbours add
						// it again to the list and the tree
						geomsQtree.insert(g.getEnvelopeInternal(), g);
						tempList.add(g);
					}
				}
				count = count + 1;
				if (monitor != null){
					monitor.report((polys.size() - count) + " / " + totalsize + " items left");
				}
			}
			polys = tempList;
		}
		newgeoms = singlePolys;
		return newgeoms;
	}
	
	/**
	 * merges touching or overlapping polygons (only polygons) with similar attribute value.
	 * 
	 * @param features
	 * @param attrName Name of the Attribute
	 * @param context can be null
	 * @param monitor can be null
	 * @return
	 */
	public static FeatureCollection mergePolySetByType(Collection features, String attrName, 
			PlugInContext context,	TaskMonitor monitor){
		//-- if item selection is used, then items can have different Schemas!
		//	 we take the schema from the first item
		Iterator it = features.iterator();
		Feature firstF = (Feature)it.next();
		FeatureSchema fs = firstF.getSchema();
		
		//-----------------------------------
		//   check how many values for attribute do exists 
		//   and sort geoms in lists  
		//-----------------------------------
		ArrayList attrValues = new ArrayList(); //stores the attr values
		ArrayList geomCollList = new ArrayList();
		ArrayList tempList = null;
		Object val = null; Feature f = null; Object storedvalue = null;
		int idx=0; int index=0;
		for (Iterator iter = features.iterator(); iter.hasNext();) {
			f = (Feature) iter.next();
			val = f.getAttribute(attrName);			
			if (idx == 0){ //first time add value directly to valuelist
				attrValues.add(val);
				ArrayList newtype = new ArrayList();	//create new Arrayist
				newtype.add(f.getGeometry());			//store item in new list
				geomCollList.add(newtype);				//add List to TypeList (at the end)				
			}
			else{//compare if already exist in list 
				index=0; boolean found = false;
				// TODO: [sstein] eventually optimize by finish loop if value has been found
				//       but should play not such a big role, since their should not be so much 
				//		 attribute values for merging polygons 
				for (Iterator iterator = attrValues.iterator(); iterator.hasNext();) {
					storedvalue = (Object) iterator.next();
					if (val.equals(storedvalue)){
						found = true;
						tempList =(ArrayList)geomCollList.get(index); //get geom-list for the value
						tempList.add(f.getGeometry()); //add item
					}
					index=index+1;
				}
				if (found == false){
					//-- add value to list if not in list
					attrValues.add(val);
					ArrayList newtype = new ArrayList();	//create new Arrayist
					newtype.add(f.getGeometry());			//store item in new list
					geomCollList.add(newtype);				//add List to TypeList (at the end)
				}
				
			}			
			idx=idx+1;
		}
		int noOfValues = geomCollList.size();
		//-----------------------------------
		// merge the geoms 
		//-----------------------------------
		ArrayList resultGeomList = new ArrayList();
		idx = 0;
		for (Iterator iter = geomCollList.iterator(); iter.hasNext();) {
			idx=idx+1;
			if (monitor != null){
				monitor.report("processing set " + idx + " / " + noOfValues);
			}
			ArrayList geomsOfOneValue = (ArrayList) iter.next();
			// put all geoms in a tree for faster search
			// and check if polygon			
			Quadtree qtree = new Quadtree(); 
			for (Iterator iterator = geomsOfOneValue.iterator(); iterator.hasNext();) {
				Geometry element = (Geometry) iterator.next();				
				if (element instanceof Polygon){
					Polygon poly = (Polygon)element;
					qtree.insert(poly.getEnvelopeInternal(), poly);
				}
				else{
					if (context != null){
						context.getWorkbenchFrame().warnUser("no polygon");
					}
				}
			}
			ArrayList resultGeoms = PolygonSetMerger.mergeGeoms(geomsOfOneValue, qtree, monitor);			
			resultGeomList.add(resultGeoms);
		}				
		//-----------------------------------		
		// generate featuredataset for output
		// create a new featureSchema
		//-----------------------------------
		FeatureSchema newFs = new FeatureSchema();		
		newFs.addAttribute("Geometry", AttributeType.GEOMETRY);
		newFs.addAttribute(attrName,fs.getAttributeType(attrName));
		FeatureDataset resFeatures = new FeatureDataset(newFs);
		//-- create features
		f = null; idx=0;
		for (Iterator iter = resultGeomList.iterator(); iter.hasNext();) {
			ArrayList typegeoms = (ArrayList) iter.next();
			for (Iterator iterator = typegeoms.iterator(); iterator.hasNext();) {
				Geometry geom = (Geometry) iterator.next();
				f = new BasicFeature(newFs);
				f.setGeometry((Geometry)geom.clone()); // set geometry
				f.setAttribute(attrName,attrValues.get(idx)); // set attribute value
				resFeatures.add(f);
			}
			idx=idx+1;
		}	
		
		return resFeatures;
	}

}