package org.openjump.sigle.plugin.geoprocessing.oneLayer.topology;

/*Cet outil a été developpé par Michael Michaud Juin 2005
 * Erwan Bocher a ajouté la récupération des attributs lors du calcul de la topologie
 */


import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.tools.AttributeMapping;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jump.feature.FeatureCollection;

import org.openjump.sigle.utilities.geom.FeatureCollectionUtil;

/**
 * PlanarGraphPlugIn compute a planar graph from a set of features.
 * The user can choose to produce the nodes, the edges and the faces, or only
 * some of those features.
 * The following relations are kept by edge features as integer attributes
 * containing node and/or faces identifiers :
 * Edges :<br>
 *     Initial node identifier<br>
 *     Final node identifier<br>
 *     Right face<br>
 *     Left face<br>
 * @author Michael Michaud and Erwan Bocher (2005-06)
 * Comments added by Michael Michaud on 2006-05-01
 */
public class PlanarGraphPlugIn extends ThreadedBasePlugIn {
    
       
    GeometryFactory gf = new GeometryFactory();
    
    private String name = "Coverage";
        
    //   Calcul des noeuds
    private static boolean nodeb = true;
    // Calcul des faces
    public static boolean faceb = true;
    // Calcul des relations arcs/noeuds et/ou arcs/faces
    private static boolean relb = true;
    
    //Options pour rappatrier les attributs de la couche d'entrée
    private static boolean attributesb = true;
    
    private static String LEFT_FACE = "LeftFace";
    private static String RIGHT_FACE = "RightFace";
    private static String INITIAL_NODE = "StartNode";
    private static String FINAL_NODE = "EndNode";
    
    private String layerName;
    
    public Collection edges;
    
    private MultiInputDialog mid;
    
        
    // Dans le run je fais les traitements 
    
    public void run(TaskMonitor monitor, PlugInContext context)
        throws Exception {
        
        // Ici je declare fcFace pour y avoir acces dans le run 
        FeatureCollection fcFace = null;        
    
        // recuperation de la couche et des options cochées
        Layer layer = mid.getLayer("Couche à transformer en couverture");
        FeatureCollection fcSource = layer.getFeatureCollectionWrapper();
        layerName = layer.getName();
        nodeb = mid.getBoolean("Calculer la couche des noeuds");
        faceb = mid.getBoolean("Calculer la couche des faces");
        relb = mid.getBoolean("Calculer les relations arcs-noeuds et/ou arcs-faces");
        attributesb = mid.getBoolean("Conserver les attributs"); 
        
        // Get linear elements from all geometries in the layer
        monitor.report("Trouver les composants linéaires...");
        List list = getLines(fcSource);
        monitor.report("Nombre de composants trouvés : " + list.size());
        
        // Union the lines (unioning is the most expensive operation)
        monitor.report("Créer la couche des arcs...");
        FeatureCollection fcEdge = createEdgeLayer(
            layer.getFeatureCollectionWrapper(), nodeb, faceb, relb, context);
        monitor.report("...couche des arcs créée");
        
        // Create the node Layer
        monitor.report("Création des noeuds...");
        if (nodeb) {
            FeatureCollection fcNode = createNodeLayer(fcEdge, context, relb);
        }
        monitor.report("...couche des noeuds créée");
        
        // Create face Layer from edges with Polygonizer
        monitor.report("Création des faces...");
        if (faceb) {
            fcFace = createFaceLayer(fcEdge, context, relb);
        }
        monitor.report("...couche des faces créée");
    
        //Erwan aout 2005
        //Ici on applique la procédure pour récuperer les attributs de la couche d'origine
        //Les attributs sont rappatriés si l'entité produite est contenue dans l'entité source
        // Si la couche d'entrée est une couche de polygones alors les attributs sont rappatriés pour la couche de faces
        // Si la couche d'entrée est une couche de linestring alors les attributs sont rappatriés pour la couche d'arcs
       
        if (faceb){
            Feature fWithin = null;
            AttributeMapping mapping = null;
            
            if (attributesb) {
                //J'exploite la methode mapping pour recuperer les attributs
                mapping = new AttributeMapping(new FeatureSchema(), new FeatureSchema());
                List aFeatures = null;
                monitor.report("...récupération des attributs");
                if (FeatureCollectionUtil.getFeatureCollectionDimension(fcSource)==2){
                    mapping = new AttributeMapping(fcSource.getFeatureSchema(), fcFace.getFeatureSchema());
                    aFeatures = fcFace.getFeatures();
                }
                else if (FeatureCollectionUtil.getFeatureCollectionDimension(fcSource)==1) {
                    mapping = new AttributeMapping(fcSource.getFeatureSchema(), fcFace.getFeatureSchema());
                    aFeatures = fcEdge.getFeatures();
                }
                        
                FeatureDataset fcRecup = new FeatureDataset(mapping.createSchema("GEOMETRY"));
                IndexedFeatureCollection indexedB = new IndexedFeatureCollection(fcSource);
            
                for (int i = 0; (i < aFeatures.size());i++) {
                    Feature aFeature = (Feature) aFeatures.get(i);
                    Feature feature = new BasicFeature(fcRecup.getFeatureSchema());
                    int nbFeatureWithin = 0;
                    for (Iterator j = indexedB.query(aFeature.getGeometry().getEnvelopeInternal()).iterator();
                        j.hasNext() && !monitor.isCancelRequested();) {
                        
                        Feature bFeature = (Feature) j.next();
                        if (aFeature.getGeometry().within(bFeature.getGeometry())) {
                            nbFeatureWithin++;
                            fWithin = bFeature;
                        }
                    }
                    // on ne transfere les attributs que lorsque la geometry resultat 
                    // n'est contenue que une seule geometry source
                    if (nbFeatureWithin == 1 && attributesb) {
                        mapping.transferAttributes(fWithin, aFeature, feature);
                    }
                    // on clone la geometry pour que les modifs sur la geometry source 
                    // ne soient pas transferees sur la geometry resultat
                    feature.setGeometry((Geometry) aFeature.getGeometry().clone()); 
                    fcRecup.add(feature);
                }                           
                context.getLayerManager().addLayer("Graph", layerName + "_Mapping", fcRecup);
            }
            else {
                // Michael Michaud : Debug : gcFace is not in this else statement
                //context.getLayerManager().addLayer("Graph", layerName + "_Face", fcFace);
            }
            context.getLayerManager().addLayer("Graph", layerName + "_Face", fcFace);
        }
    }         
    
    /**
     * @param featureCollectionWrapper
     * @param attributesb2
     * @param context
     * @return
     */

    public void initialize(PlugInContext context) throws Exception {
        context.getFeatureInstaller().addMainMenuItem(this,new String[] { MenuNames.GEOPROCESSING, MenuNames.ONELAYER,MenuNames.TOPOLOGY }, 
                "Graphe planaire", false, null, 
                new MultiEnableCheck().add(new EnableCheckFactory(context.getWorkbenchContext()).createTaskWindowMustBeActiveCheck())
                .add(new EnableCheckFactory(context.getWorkbenchContext()).createAtLeastNLayersMustExistCheck(1))
                ); 
    }

    public boolean execute(PlugInContext context) throws Exception {
        initDialog(context);
        mid.setVisible(true);
        mid.wasOKPressed();
        return mid.wasOKPressed();
    }

    public String getName() {
        return name;
    }
    

    private void initDialog(PlugInContext context) {
        
        mid = new MultiInputDialog(context.getWorkbenchFrame(), "Fonctions topologiques", true);
        mid.addLayerComboBox("Couche à transformer en couverture", context.getLayerManager().getLayer(0), context.getLayerManager());
        mid.addLabel("La couche des arcs est toujours calculée");
        mid.addCheckBox("Calculer la couche des noeuds", nodeb);
        mid.addCheckBox("Calculer la couche des faces", faceb);
        mid.addCheckBox("Calculer les relations arcs-noeuds et/ou arcs-faces", relb);
        mid.addCheckBox("Conserver les attributs", attributesb);
        mid.pack();
        //mid.show();
    }

    // ************************************************
    // extract lines from a feature collection
    // ************************************************
    public List getLines(FeatureCollection fc) {
        List linesList = new ArrayList();
        LinearComponentExtracter filter = new LinearComponentExtracter(linesList);
        int count = 0;
        for (Iterator i = fc.iterator(); i.hasNext(); ) {
            Geometry g = ((Feature)i.next()).getGeometry();
            g.apply(filter);
        }
        return linesList;
    }
    
    // ************************************************
    // Create edge layer
    // ************************************************
    public FeatureCollection createEdgeLayer(FeatureCollection fc,
        boolean nodeb, boolean faceb, boolean relations,
        PlugInContext context) {
        // Schema edge
        FeatureSchema fsEdge = new FeatureSchema();
          fsEdge.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
          fsEdge.addAttribute("ID", AttributeType.INTEGER);
          // Edge - Node relation
          if (nodeb && relations) {
              fsEdge.addAttribute(INITIAL_NODE, AttributeType.INTEGER);
              fsEdge.addAttribute(FINAL_NODE, AttributeType.INTEGER);
          }
          // Edge - Face relation
          if (faceb && relations) {
              fsEdge.addAttribute(RIGHT_FACE, AttributeType.INTEGER);
              fsEdge.addAttribute(LEFT_FACE, AttributeType.INTEGER);
          }
        FeatureDataset fcEdge = new FeatureDataset(fsEdge);
        
        // Get linear elements from all geometries in the layer
        List list = getLines(fc);
        
        // Union the lines (unioning is the most expensive operation)
        Geometry geom = gf.createMultiLineString(gf.toLineStringArray(list));
        geom = gf.createMultiLineString(null).union(geom);
        GeometryCollection gc = geom instanceof GeometryCollection ?
            (GeometryCollection)geom:
            gf.createGeometryCollection(new Geometry[]{geom});
        
        // Create the edge layer by merging lines between 3+ order nodes
        // (Merged lines are multilines)
        LineMerger lineMerger = new LineMerger();
        for (int i = 0 ; i < gc.getNumGeometries() ; i++) {
            lineMerger.add(gc.getGeometryN(i));
        }
        edges = lineMerger.getMergedLineStrings();
        int no = 0;
        for (Iterator it = edges.iterator() ; it.hasNext() ;) {
            Feature f = new BasicFeature(fsEdge);
            f.setGeometry((Geometry)it.next());
            f.setAttribute("ID", new Integer(++no));
            fcEdge.add(f);
        }
        context.getLayerManager().addLayer("Graph", layerName+"_Edge", fcEdge);
        return fcEdge;
    }
    
    // ************************************************
    // Create node layer
    // ************************************************
    public FeatureCollection createNodeLayer(FeatureCollection fcEdge,
        PlugInContext context, boolean relations) {
        FeatureSchema fsNode = new FeatureSchema();
        fsNode.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        fsNode.addAttribute("ID", AttributeType.INTEGER);
        FeatureDataset fcNode = new FeatureDataset(fsNode);
        
        // Create the node Layer
        Map nodes = new HashMap();
        //List edges = geometriesFromFeatures(fcEdge);
        for (Iterator it = edges.iterator() ; it.hasNext() ;) {
            Coordinate[] cc = ((Geometry)it.next()).getCoordinates();
            nodes.put(cc[0], gf.createPoint(cc[0]));
            nodes.put(cc[cc.length-1], gf.createPoint(cc[cc.length-1]));
        }
        int no = 0;
        for (Iterator it = nodes.values().iterator() ; it.hasNext() ; ) {
            Feature f = new BasicFeature(fsNode);
            f.setGeometry((Geometry)it.next());
            f.setAttribute("ID", new Integer(++no));
            nodes.put(f.getGeometry().getCoordinate(), f);
            fcNode.add(f);
        }
        context.getLayerManager().addLayer("Graph", layerName+"_Node", fcNode);
        
        // Compute the relation between edges and nodes
        if (relations) {
            for (Iterator it = fcEdge.iterator() ; it.hasNext() ;) {
                Feature f = (Feature)it.next();
                Coordinate[] cc = f.getGeometry().getCoordinates();
                f.setAttribute(INITIAL_NODE, ((Feature)nodes.get(cc[0])).getAttribute("ID"));
                f.setAttribute(FINAL_NODE, ((Feature)nodes.get(cc[cc.length-1])).getAttribute("ID"));
            }
        }
        return fcNode;
    }
    
    // ************************************************
    // Create face layer
    // ************************************************
    public FeatureCollection createFaceLayer(FeatureCollection fcEdge,
        PlugInContext context, boolean relations) {
        // Create the face layer
        FeatureSchema fsFace = new FeatureSchema();
        fsFace.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        fsFace.addAttribute("ID", AttributeType.INTEGER);
        FeatureDataset fcFace = new FeatureDataset(fsFace);
        
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(edges);
        int no = 0;
        for (Iterator it = polygonizer.getPolygons().iterator() ; it.hasNext() ;) {
            Feature f = new BasicFeature(fsFace);
            f.setGeometry((Geometry)it.next());
            f.setAttribute("ID", new Integer(++no));
            System.out.println("Face : " + f.getID() + " : " + f.getAttribute("ID"));
            fcFace.add(f);
        }
        //context.getLayerManager().addLayer("Graph", layerName+"_Face", fcFace);
        
        // inscrit les numéros de face dans les arcs
        // Les arcs qui sont en bords de face sont codés à -1.
        if(relations) {
            for (Iterator it = fcEdge.getFeatures().iterator() ; it.hasNext() ; ) {
                Feature edge = (Feature)it.next();
                Geometry g1 = edge.getGeometry();
                List list = fcFace.query(g1.getEnvelopeInternal());
                for (int i = 0 ; i < list.size() ; i++) {
                    Feature face = (Feature)list.get(i);
                    Geometry g2 = face.getGeometry();
                    Geometry inters = g2.intersection(g1);
                    // Michael Michaud : added on 2006-05-01
                    // Process properly the case of empty intersection
                    if (inters.isEmpty()) continue;
                    else if (inters.getLength()>0) {
                        Integer idValue = (Integer) face.getAttribute("ID");
                        if (!idValue.equals("")) {
                            if (inters.getCoordinates()[0].equals(g1.getCoordinates()[0])) {
                                edge.setAttribute(RIGHT_FACE, face.getAttribute("ID"));
                            }
                            else {edge.setAttribute(LEFT_FACE, face.getAttribute("ID"));}
                        }
                    }
                    else {
                        if (inters.getCoordinates()[0].equals(g1.getCoordinates()[0])) {
                            edge.setAttribute(RIGHT_FACE, new Integer(-1));
                        }
                        else {edge.setAttribute(LEFT_FACE, new Integer(-1));}
                    }  
                        
                }
            }
        }
        return fcFace;
    }
     
}


