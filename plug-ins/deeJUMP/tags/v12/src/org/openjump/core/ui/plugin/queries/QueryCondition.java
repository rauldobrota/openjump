package org.openjump.core.ui.plugin.queries;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Iterator;
import java.math.BigDecimal;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * QueryDialog
 * @author Michaël MICHAUD
 * @version 0.1.0 (4 Dec 2004)
 */ 
public class QueryCondition  {
    SimpleQueryDialog query;
    QueryFunction ft;
    QueryOperator op;
    Pattern pattern;   // only used for match & find functions
    PlugInContext context;
    
     public QueryCondition(SimpleQueryDialog query, PlugInContext context) {
        this.query = query;
        this.ft=query.function;
        this.op=query.operator;
        if (op==QueryOperator.MATC || op==QueryOperator.FIND) {
            if (query.caseSensitive.getState())
                pattern = Pattern.compile((String)query.valueCB.getSelectedValue());
            else 
                pattern = Pattern.compile((String)query.valueCB.getSelectedValue(), Pattern.CASE_INSENSITIVE);
        }
        this.context = context;
    }
    
    public String toString() {
        String att = query.attribute.trim().equals("")?"GEOMETRY":query.attribute;
        String func = ft.toString().trim().equals("")?"":"."+ft;
        return "" + att + func + " " + op + " \"" +
               query.valueCB.getSelectedValue() + "\"";
    }
    
    public boolean test(Feature feature) throws Exception {
        Object o = null;
        //System.out.print("Nature de l'attribut : ");
        if(query.attributeType=='G') {
            //System.out.println(" géométrique");
            //System.out.println("Operator = " + op);
            o = feature.getGeometry();
            if(ft.type=='G') return test(gfunction((Geometry)o));
            else if(ft.type=='N') return test(nfunction((Geometry)o));
            else if(ft.type=='B') return test(bfunction((Geometry)o));
            else return false;
        }
        else {
            // System.out.println(" sémantique");
            // attributes which does not exist for this feature must have
            // been eliminated before the test procedure
            // (see QueryDialog#executeQuery())
            o = feature.getAttribute(query.attribute);
            if(o instanceof Boolean) return test(((Boolean)o).booleanValue());
            else if(o instanceof Integer) return test(((Integer)o).doubleValue());
            else if(o instanceof Long) return test(((Long)o).doubleValue());
            else if(o instanceof Double) return test(((Double)o).doubleValue());
            else if(o instanceof BigDecimal) return test(((BigDecimal)o).doubleValue());
            else if(o instanceof Date) return test((double)((Date)o).getTime());
            else if(o instanceof String && ft.type=='S') return test(sfunction((String)o));
            else if(o instanceof String && ft.type=='N') return test(nfunction((String)o));
            else return false;
        }
    }
    
    private boolean test(boolean b) throws Exception {
        boolean value=query.valueCB.getSelectedIndex()==0?true:false;
        if (b==value && op==QueryOperator.BEQ) return true;
        else return false;
    }
    
    private boolean test(double d) throws Exception {
        double value = Double.parseDouble((String)query.valueCB.getSelectedValue());
        if (op==QueryOperator.EQ && d==value) return true;
        else if (op==QueryOperator.NE && d!=value) return true;
        else if (op==QueryOperator.LT && d<value) return true;
        else if (op==QueryOperator.GT && d>value) return true;
        else if (op==QueryOperator.LE && d<=value) return true;
        else if (op==QueryOperator.GE && d>=value) return true;
        else return false;
    }
    
    private boolean test(String s) throws Exception {
        String value = (String)query.valueCB.getSelectedValue();
        if (query.caseSensitive.getState()) {
            if (op==QueryOperator.EQUA) return s.equals(value);
            else if (op==QueryOperator.DIFF) return !s.equals(value);
            else if (op==QueryOperator.STAR) return s.startsWith(value);
            else if (op==QueryOperator.ENDS) return s.endsWith(value);
            else if (op==QueryOperator.MATC) return pattern.matcher(s).matches();
            else if (op==QueryOperator.FIND) return pattern.matcher(s).find();
            else if (op==QueryOperator.BEFO) return s.compareTo(value)<=0;
            else if (op==QueryOperator.AFTE) return s.compareTo(value)>=0;
            else return false;
        }
        else {
            if (op==QueryOperator.EQUA) return s.equalsIgnoreCase(value);
            else if (op==QueryOperator.DIFF) return !s.equalsIgnoreCase(value);
            else if (op==QueryOperator.STAR) return s.toUpperCase().startsWith(value.toUpperCase());
            else if (op==QueryOperator.ENDS) return s.toUpperCase().endsWith(value.toUpperCase());
            else if (op==QueryOperator.MATC) return pattern.matcher(s).matches();
            else if (op==QueryOperator.FIND) return pattern.matcher(s).find();
            else if (op==QueryOperator.BEFO) return s.compareToIgnoreCase(value)<=0;
            else if (op==QueryOperator.AFTE) return s.compareToIgnoreCase(value)>=0;
            else return false;
        }
    }
    
    private boolean test(Geometry g) throws Exception {
        int pos = query.valueCB.getSelectedIndex();
        // Target Geometry is the selection
        // System.out.println("position de la valeur sélectionnée : " + pos);
        if (pos==0) {
            for (Iterator it = query.selection.iterator() ; it.hasNext() ;) {
                Geometry p = (Geometry)it.next();
                if (op==QueryOperator.INTER && g.intersects(p)) return true;
                else if (op==QueryOperator.CONTA && g.contains(p)) return true;
                else if (op==QueryOperator.WITHI && g.within(p)) return true;
                else if (op==QueryOperator.WDIST && g.distance(p)<op.arg) return true;
                else if (op==QueryOperator.TOUCH && g.touches(p)) return true;
                else if (op==QueryOperator.CROSS && g.crosses(p)) return true;
                else if (op==QueryOperator.OVERL && g.overlaps(p)) return true;
                else if (op==QueryOperator.DISJO && g.disjoint(p)) return true;
                else;
            }
            return false;
        }
        else if (pos==1) {
            Layer[] ll = context.getLayerNamePanel().getSelectedLayers();
            for (int i = 0 ; i < ll.length ; i++) {
                FeatureCollection fc = ll[i].getFeatureCollectionWrapper();
                for (Iterator it = fc.iterator() ; it.hasNext() ;) {
                    Geometry p = ((Feature)it.next()).getGeometry();
                    if (op==QueryOperator.INTER && g.intersects(p)) return true;
                    else if (op==QueryOperator.CONTA && g.contains(p)) return true;
                    else if (op==QueryOperator.WITHI && g.within(p)) return true;
                    else if (op==QueryOperator.WDIST && g.distance(p)<op.arg) return true;
                    else if (op==QueryOperator.TOUCH && g.touches(p)) return true;
                    else if (op==QueryOperator.CROSS && g.crosses(p)) return true;
                    else if (op==QueryOperator.OVERL && g.overlaps(p)) return true;
                    else if (op==QueryOperator.DISJO && g.disjoint(p)) return true;
                    else;
                }
                return false;
            }
        }
        else if (pos==2) {
            List ll = context.getLayerManager().getLayers();
            for (int i = 0 ; i < ll.size() ; i++) {
                FeatureCollection fc = ((Layer)ll.get(i)).getFeatureCollectionWrapper();
                for (Iterator it = fc.iterator() ; it.hasNext() ;) {
                    Geometry p = ((Feature)it.next()).getGeometry();
                    if (op==QueryOperator.INTER && g.intersects(p)) return true;
                    else if (op==QueryOperator.CONTA && g.contains(p)) return true;
                    else if (op==QueryOperator.WITHI && g.within(p)) return true;
                    else if (op==QueryOperator.WDIST && g.distance(p)<op.arg) return true;
                    else if (op==QueryOperator.TOUCH && g.touches(p)) return true;
                    else if (op==QueryOperator.CROSS && g.crosses(p)) return true;
                    else if (op==QueryOperator.OVERL && g.overlaps(p)) return true;
                    else if (op==QueryOperator.DISJO && g.disjoint(p)) return true;
                    else;
                }
                return false;
            }
        }
        else {
            Layer layer = context.getLayerManager().getLayer((String)query.valueCB.getSelectedValue());
            FeatureCollection fc = layer.getFeatureCollectionWrapper();
            for (Iterator it = fc.iterator() ; it.hasNext() ;) {
                Geometry p = ((Feature)it.next()).getGeometry();
                if (op==QueryOperator.INTER && g.intersects(p)) return true;
                else if (op==QueryOperator.CONTA && g.contains(p)) return true;
                else if (op==QueryOperator.WITHI && g.within(p)) return true;
                else if (op==QueryOperator.WDIST && g.distance(p)<op.arg) return true;
                else if (op==QueryOperator.TOUCH && g.touches(p)) return true;
                else if (op==QueryOperator.CROSS && g.crosses(p)) return true;
                else if (op==QueryOperator.OVERL && g.overlaps(p)) return true;
                else if (op==QueryOperator.DISJO && g.disjoint(p)) return true;
                else;
            }
            return false;
        }
        return false;
    }
    
    //**************************************************************************
    // apply functions
    //**************************************************************************
    
    private String sfunction(String s) {
        if (ft==QueryFunction.SNOF) return s;
        else if (ft==QueryFunction.TRIM) return s.trim();
        else if (ft==QueryFunction.SUBS && ft.args.length==1) {
            return s.substring(ft.args[0]);
        }
        else if (ft==QueryFunction.SUBS && ft.args.length==2) {
            return s.substring(ft.args[0], ft.args[1]);
        }
        else return s;
    }
    
    private double nfunction(String s) {
        if (ft==QueryFunction.LENG) return (double)s.length();
        else return 0.0;
    }
    
    private Geometry gfunction(Geometry g) {
        //System.out.println("geometric function");
        if (ft==QueryFunction.GNOF) return g;
        else if (ft==QueryFunction.CENT) return g.getInteriorPoint();
        else if (ft==QueryFunction.BUFF) return g.buffer(ft.args[0]);
        else return g;
    }
    
    private double nfunction(Geometry g) {
        //System.out.println("numeric function");
        if (ft==QueryFunction.LENG) return g.getLength();
        else if (ft==QueryFunction.AREA) return g.getArea();
        else if (ft==QueryFunction.NBPT) return (double)g.getNumPoints();
        else if (ft==QueryFunction.NBPA) {
            if (g.isEmpty()) return 0;
            else if (g instanceof GeometryCollection)
                return ((GeometryCollection)g).getNumGeometries();
            else return 1;
        }
        else return 0.0;
    }
    
    private boolean bfunction(Geometry g) {
        //System.out.println("boolean function");
        if (ft==QueryFunction.EMPT) return g.isEmpty();
        else if (ft==QueryFunction.SIMP) return g.isSimple();
        else if (ft==QueryFunction.VALI) return g.isValid();
        else return false;
    }

}
