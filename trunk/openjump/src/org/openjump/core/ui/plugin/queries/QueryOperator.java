package org.openjump.core.ui.plugin.queries;

import com.vividsolutions.jump.I18N;

/**
 * IOperator
 * @author Michaël MICHAUD
 * @version 0.1.0 (4 Dec 2004)
 */
 
/**
 * Interface IOperator containing the definition of common operators
 */
public class QueryOperator {
    private String key;
    public char type;  // B=boolean, N=numeric, S=string, E=enumeration, G=geometric
    public double arg = -1d; // arguments for the within distance function
    
    // OPERATOR FOR NUMERIC VALUES AND DATES
    public final static QueryOperator BEQ = new QueryOperator("beq", 'B');
    public final static QueryOperator BNE = new QueryOperator("bne", 'B');
    
    // OPERATOR FOR NUMERIC VALUES AND DATES
    public final static QueryOperator EQ = new QueryOperator("eq", 'N');
    public final static QueryOperator NE = new QueryOperator("ne", 'N');
    public final static QueryOperator LT = new QueryOperator("lt", 'N');
    public final static QueryOperator GT = new QueryOperator("gt", 'N');
    public final static QueryOperator LE = new QueryOperator("le", 'N');
    public final static QueryOperator GE = new QueryOperator("ge", 'N');

    // OPERATOR FOR STRING VALUES
    public final static QueryOperator EQUA= new QueryOperator("equa", 'S');
    public final static QueryOperator DIFF = new QueryOperator("diff", 'S');
    public final static QueryOperator STAR = new QueryOperator("star", 'S');
    public final static QueryOperator ENDS = new QueryOperator("ends", 'S');
    public final static QueryOperator MATC = new QueryOperator("matc", 'S');
    public final static QueryOperator FIND = new QueryOperator("find", 'S');
    public final static QueryOperator BEFO = new QueryOperator("befo", 'S');
    public final static QueryOperator AFTE = new QueryOperator("afte", 'S');
        
    // OPERATOR FOR GEOMETRIC ATTRIBUTE
    public final static QueryOperator INTER = new QueryOperator("inter", 'G');
    public final static QueryOperator CONTA = new QueryOperator("conta", 'G');
    public final static QueryOperator WITHI = new QueryOperator("withi", 'G');
    public final static QueryOperator WDIST = new QueryOperator("wdist", 'G', 1000.0);
    public final static QueryOperator TOUCH = new QueryOperator("touch", 'G');
    public final static QueryOperator CROSS = new QueryOperator("cross", 'G');
    public final static QueryOperator OVERL = new QueryOperator("overl", 'G');
    public final static QueryOperator DISJO = new QueryOperator("disjo", 'G');
    
    public static QueryOperator[] BOOLEAN_OP = new QueryOperator[] {BEQ, BNE};
    
    public static QueryOperator[] NUMERIC_OP = new QueryOperator[] {
        EQ, NE, LT, GT, LE, GE
    };
        
    public static QueryOperator[] STRING_OP = new QueryOperator[] {
        EQUA, DIFF, STAR, ENDS, MATC, FIND, BEFO, AFTE
    };
        
    public static QueryOperator[] GEOMETRIC_OP = new QueryOperator[] {
        INTER, CONTA, WITHI, WDIST, TOUCH, CROSS, OVERL, DISJO
    };   
    
    public QueryOperator(String key, char type) {
        this.key = key;
        this.type = type;
    }
    
    public QueryOperator(String key, char type, double arg) {
        this.key = key;
        this.type = type;
        this.arg = arg;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(I18N.get("org.openjump.core.ui.plugin.queries.QueryOperator."+key));
        if(arg<0.0) {return sb.toString();}
        else {return sb.toString() + " ("+arg+")";}
    }

}
