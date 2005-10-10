/*****************************************************
 * created:  		07.09.2005
 * last modified:  	04.10.2005 append attributes
 * 
 * author: sstein
 * 
 * description: 
 * 		merges two selected Polygons and appends the
 * 		attributes to the merged object 
 *****************************************************/

package mapgen.ui.onselecteditems;

import java.util.Collection;
import java.util.Iterator;

import mapgen.algorithms.polygons.PolygonMerge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

/**
 * @description: 
 * 		merges two selected Polygons and appends the
 * 		attributes to the merged object
 * 
 * @author sstein
 *
 **/
public class MergeTwoSelectedPolygonsWithAttributesPlugIn extends AbstractPlugIn{


    public void initialize(PlugInContext context) throws Exception {
        FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
    	featureInstaller.addMainMenuItem(
    	        this,								//exe
                new String[] {"Map Generalisation","Not Scale Dependent Algorithms", "Polygons"}, 	//menu path
                this.getName(), //name methode .getName recieved by AbstractPlugIn 
                false,			//checkbox
                null,			//icon
                createEnableCheck(context.getWorkbenchContext())); //enable check        
    }
    
    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

        return new MultiEnableCheck()
                        .add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck())
                        .add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(2));
    }
    
	public boolean execute(PlugInContext context) throws Exception{       
    	        		
	    Collection features = context.getWorkbenchContext().getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems();
	    if (features.size() == 2){
	    	Iterator iter = features.iterator();
	    	Feature f1 = (Feature)iter.next();
	    	Feature f2 = (Feature)iter.next();
	    	PolygonMerge merge = new PolygonMerge(f1.getGeometry(), f2.getGeometry()); 
	    	if(merge.isMergeSuccesfull()){
	    		Geometry g = merge.getOutPolygon();
	    		Feature newF = this.appendAttributesFromSecondFeature(f1,f2);
	    		newF.setGeometry(g);
	    		FeatureDataset myCollA = new FeatureDataset(newF.getSchema()); 
	    		myCollA.add(newF);
	    		context.addLayer(StandardCategoryNames.WORKING, "mergedPolygonFeature", myCollA);
	    	}
	    }
		else{
			context.getWorkbenchFrame().warnUser("more than 2 objects selected");
		}
    	//context.getWorkbenchContext().getLayerViewPanel().getSelectionManager().clear();
	    return true;
    }	
	
	/**
	 * append attributes of given second feature to the first feature
	 * @param f1
	 * @param f2
	 * @return
	 */
	private Feature appendAttributesFromSecondFeature(Feature f1, Feature f2){
		//-- create newSchema
		FeatureSchema fs = this.mergeFSchemas(f1.getSchema(),f2.getSchema());
		//-- create a new Feature which has already the attributes of the
		//   the first feature
		Feature newF = this.copyFeature(f1, fs);
		//-- append the other attribute Values from Feature 2
		//   but without a second Geometry
		FeatureSchema secondSchema = f2.getSchema();
		int indx = f1.getSchema().getAttributeCount();
		for (int i = 0; i < secondSchema.getAttributeCount(); i++) {
			AttributeType at = secondSchema.getAttributeType(i);
			Object value = f2.getAttribute(i);
			//-- don't store a second geometry
			if(at != AttributeType.GEOMETRY){
				newF.setAttribute(indx,value);
				indx++;
			} 
		}
		return newF;
	}
	
	/**
	 * Append a second FeatureSchema to another.
	 * The second geometry attribute will be omitted. 
	 * @param firstSchema
	 * @param secondSchema
	 * @return
	 */
	private FeatureSchema mergeFSchemas(FeatureSchema firstSchema, FeatureSchema secondSchema){
		FeatureSchema newSchema = this.copyFeatureSchema(firstSchema);
		for (int i = 0; i < secondSchema.getAttributeCount(); i++) {
			AttributeType at = secondSchema.getAttributeType(i);
			String aname = secondSchema.getAttributeName(i);
			if(newSchema.hasAttribute(aname)==true){
				aname = aname + "N";
			}
			//-- don't store a second geometry
			if(at != AttributeType.GEOMETRY){
				newSchema.addAttribute(aname,at);
			}
		}		
		return newSchema;
	}	
	/**
	 * copy the input feature to a new Schema whereby the new 
	 * Feature Schema musst be an extended or shortened one 
	 * @param oldSchema
	 * @return Feature
	 */
	private Feature copyFeature(Feature feature, FeatureSchema newSchema){
		FeatureSchema oldSchema = feature.getSchema();
		Feature newF = new BasicFeature(newSchema);
		int n = 0;
		if (oldSchema.getAttributeCount() > newSchema.getAttributeCount()){
			//for schema shortening
			n = newSchema.getAttributeCount();
		}
		else{
			//for schema extension
			n = oldSchema.getAttributeCount();
		}
		for (int i = 0; i < n; i++) {			
			String aname = oldSchema.getAttributeName(i);
			Object value = feature.getAttribute(aname);			
			newF.setAttribute(aname,value);						
		}		
		return newF;
	}
	
	/**
	 * copy/clone the input featureSchema and  since it is not proper implemented in Jump 
	 * @param oldSchema
	 * @return
	 */
	private FeatureSchema copyFeatureSchema(FeatureSchema oldSchema){
		FeatureSchema fs = new FeatureSchema();
		for (int i = 0; i < oldSchema.getAttributeCount(); i++) {
			AttributeType at = oldSchema.getAttributeType(i);
			String aname = oldSchema.getAttributeName(i);
			fs.addAttribute(aname,at);
			fs.setCoordinateSystem(oldSchema.getCoordinateSystem());			
		}		
		return fs;
	}
	    
  
}
