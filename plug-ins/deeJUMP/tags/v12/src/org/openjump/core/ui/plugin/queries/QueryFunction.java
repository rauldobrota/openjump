package org.openjump.core.ui.plugin.queries;

import com.vividsolutions.jump.I18N;

/**
 * Function
 * Definition of functions used in the QueryDialog 
 * @author Michaël MICHAUD
 * @version 0.1.0 (4 Dec 2004)
 */

public class QueryFunction {
    private String key;
    public char type;  // B=boolean, N=numeric, S=string, E=enumeration, G=geometric
    public int[] args; // arguments for the substring function
    public double arg; // argument for the buffer function
    
    // NUMERIC FUNCTION
    public final static QueryFunction BNOF = new QueryFunction("bnof", 'B');
    
    // NUMERIC FUNCTION
    public final static QueryFunction NNOF = new QueryFunction("nnof", 'N');
    
    // STRING FUNCTION
    public final static QueryFunction SNOF = new QueryFunction("snof", 'S');
    public final static QueryFunction TRIM = new QueryFunction("trim", 'S');
    public final static QueryFunction SUBS = new QueryFunction("subs", 'S', new int[]{0,2});
    
    public final static QueryFunction LENG = new QueryFunction("leng", 'N');
    
    // GEOMETRIC FUNCTION
    public final static QueryFunction GNOF = new QueryFunction("gnof", 'G');
    //public final static Function LENG = new Function("leng", 'N');
    public final static QueryFunction AREA = new QueryFunction("area", 'N');
    public final static QueryFunction NBPT = new QueryFunction("nbpt", 'N');
    public final static QueryFunction NBPA = new QueryFunction("nbpa", 'N');
    public final static QueryFunction BUFF = new QueryFunction("buff", 'G', 1000);
    public final static QueryFunction CENT = new QueryFunction("cent", 'G');
    public final static QueryFunction EMPT = new QueryFunction("empt", 'B');
    public final static QueryFunction SIMP = new QueryFunction("simp", 'B');
    public final static QueryFunction VALI = new QueryFunction("vali", 'B');
    
    public static QueryFunction[] BOOLEAN_FUNCTIONS = new QueryFunction[] {BNOF};
    
    public static QueryFunction[] NUMERIC_FUNCTIONS = new QueryFunction[] {NNOF};
    
    public static QueryFunction[] STRING_FUNCTIONS = new QueryFunction[] {
            SNOF, TRIM, SUBS, LENG
    };
            
    public static QueryFunction[] GEOMETRIC_FUNCTIONS = new QueryFunction[] {
            GNOF, LENG, AREA, NBPT, NBPA, BUFF, CENT, EMPT, SIMP, VALI
    };
    
    public QueryFunction(String key, char type) {
        this.key = key;
        this.type = type;
    }
    
    public QueryFunction(String key, char type, int[] args) {
        this.key = key;
        this.type = type;
        this.args = args;
    }
    
    public QueryFunction(String key, char type, double arg) {
        this.key = key;
        this.type = type;
        this.arg = arg;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(I18N.get("org.openjump.core.ui.plugin.queries.QueryFunction."+key));
        if(this==BUFF) {return sb.toString() + " ("+arg+")";}
        else if (this==SUBS && args.length==1) {return sb.toString() + " ("+args[0] + ")";}
        else if (this==SUBS && args.length==2) {return sb.toString() + " ("+args[0]+","+args[1]+")";}
        else {return sb.toString();}
    }

}
