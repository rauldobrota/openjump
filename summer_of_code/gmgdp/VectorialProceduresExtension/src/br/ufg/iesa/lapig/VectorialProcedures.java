package br.ufg.iesa.lapig;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.task.TaskMonitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Processing the vectorial procedures
 * @author Leandro Leal
 */
public class VectorialProcedures {

    List GeometryList;
    
    /**
     * Empty constructor
     */
    public VectorialProcedures() {
        GeometryList = new ArrayList();
    }
        
    /**
     * Processing the union
     * @param monitor   Current monitor of process
     * @param fc1   FeatureCollection of layer 1
     * @param fc2   FeatureColletcion of layer 2
     * @return  FeatureColleciont of resultant layer
     * @throws java.lang.NullPointerException
     */
    public FeatureCollection Union(TaskMonitor monitor, FeatureCollectionWrapper fc1,
            FeatureCollectionWrapper fc2) throws NullPointerException{
        monitor.allowCancellationRequests();
        this.GeometryList.clear();
         monitor.report("Processing union...");
        
        Geometry resultOper = this.layerFeatureUnion(monitor,fc1).union(
                    this.layerFeatureUnion(monitor,fc2));
        
        GeometryList.add(resultOper); // result of operation
        return FeatureDatasetFactory.createFromGeometry(GeometryList);
    }
    
    /**
     * Processing the intersection
     * @param monitor   Current monitor of process
     * @param fc1   FeatureCollection of layer 1
     * @param fc2   FeatureColletcion of layer 2
     * @return  FeatureColleciont of resultant layer
     * @throws java.lang.NullPointerException
     */
    public FeatureCollection Intersection(TaskMonitor monitor, FeatureCollectionWrapper fc1,
            FeatureCollectionWrapper fc2) throws NullPointerException {
        monitor.allowCancellationRequests();
        this.GeometryList.clear();
        monitor.report("Processing intersection...");
        
        Geometry resultOper = this.layerFeatureUnion(monitor,fc1).intersection(
                    this.layerFeatureUnion(monitor,fc2));
        
        GeometryList.add(resultOper); // result of operation
        return FeatureDatasetFactory.createFromGeometry(GeometryList);
    }
    
        /**
     * Processing the difference
     * @param monitor   Current monitor of process
     * @param fc1   FeatureCollection of layer 1
     * @param fc2   FeatureColletcion of layer 2
     * @return  FeatureColleciont of resultant layer
     * @throws java.lang.NullPointerException
     */
    public FeatureCollection difference(TaskMonitor monitor, FeatureCollectionWrapper fc1,
            FeatureCollectionWrapper fc2) throws NullPointerException{
        monitor.allowCancellationRequests();
        this.GeometryList.clear();
         monitor.report("Processing difference...");
        
        Geometry resultOper = this.layerFeatureUnion(monitor,fc1).difference(
                    this.layerFeatureUnion(monitor,fc2));
        
        GeometryList.add(resultOper); // result of operation
        return FeatureDatasetFactory.createFromGeometry(GeometryList);
    }
    
   /**
     * Processing the Sym Difference
     * @param monitor
     * @param fc1
     * @param fc2
     * @return 
     * @throws java.lang.NullPointerException
     */
    public FeatureCollection symDifference(TaskMonitor monitor, FeatureCollectionWrapper fc1,
            FeatureCollectionWrapper fc2) throws NullPointerException{
        monitor.allowCancellationRequests();
        this.GeometryList.clear();
         monitor.report("Processing sym difference...");
        
        Geometry resultOper = this.layerFeatureUnion(monitor,fc1).symDifference(
                    this.layerFeatureUnion(monitor,fc2));
        
        GeometryList.add(resultOper); // result of operation
        return FeatureDatasetFactory.createFromGeometry(GeometryList);
    }
    
    /**
     * Processing the Buffer
     * @param monitor   Current monitor of process
     * @param fc    FeatureCollection of resultant layer
     * @param b double value for buffer procedures
     * @return  FeatureCollecion of buffer layer
     * @throws java.lang.NullPointerException
     */
    public FeatureCollection Buffer(TaskMonitor monitor, FeatureCollection fc,
            double b) 
            throws NullPointerException{    
        monitor.allowCancellationRequests();
        
        monitor.report("Processing buffer...");
        
        Collection features = fc.getFeatures();
        Feature firstF = (Feature)fc.getFeatures().get(0);
        FeatureCollection result = new FeatureDataset(firstF.getSchema());

        int size = features.size();
        int count=0;
        
        for (Iterator i = features.iterator(); i.hasNext();) {
            Feature f = (Feature) i.next();
            Feature newF = f.clone(false);
            Geometry buffer = f.getGeometry().buffer(b);
            
            newF.setGeometry(buffer);
            result.add(newF);
            count++;

            monitor.report(count++, size, "features");
        }
        return result;
    }
    
    /* Get create a single Geometry with all features of layer */
    private Geometry layerFeatureUnion(TaskMonitor monitor, FeatureCollection fc) {
        monitor.allowCancellationRequests();

        int size = fc.size();
        int count = 1;
        
        Geometry currUnion = null;

        for (Iterator i = fc.iterator(); i.hasNext();) {
            Feature f = (Feature) i.next();
            Geometry geom = f.getGeometry();

            if (currUnion == null) {
                currUnion = geom;
            } else {
                currUnion = currUnion.union(geom);
            }

            monitor.report(count++, size, "features");
        }
        return currUnion;
    }
}
