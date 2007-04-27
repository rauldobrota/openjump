/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.latlon.deejump.plugin.wfs;

public class WFSOptions {

    private int maxFeatures;
    
    private String[] outputFormats; 

    private String selectedOutputFormat; 

    private String[] protocols; 

    private String selectedProtocol; 
    
    public WFSOptions( int maxFeatures, String[] outputFormats, String[] protocols ) {
        setMaxFeatures(maxFeatures);
        setOutputFormats(outputFormats);
        setProtocols(protocols);
    }

    public WFSOptions(){
        this( 1000,
              new String[] {"GML2", "text/xml; subtype=gml/3.1.1"},
              new String[] {"GET", "POST"});
    }
    
    public int getMaxFeatures() {
        return maxFeatures;
    }

    
    public void setMaxFeatures(int maxFeatures) {
        if( maxFeatures < 1 ) {
            throw new IllegalArgumentException("maxFeatures must be a number greater than 1.");
        }
        this.maxFeatures = maxFeatures;
    }

    public String[] getOutputFormats() {
        
        return outputFormats;
    }

    public void setOutputFormats(String[] outputFormats) {
        if( outputFormats == null || outputFormats.length == 0 ) {
            throw new IllegalArgumentException("outputFormats cannot be null or have zero length");
        }
        this.outputFormats = outputFormats;
        //the selected is the first one in the list
        setSelectedOutputFormat( this.outputFormats[0] );
    }

    public String[] getProtocols() {
        return protocols;
    }

    public void setProtocols(String[] protocols) {
        if( protocols == null || protocols.length == 0 ) {
            throw new IllegalArgumentException("outputFormats cannot be null or have zero length");
        }
        this.protocols = protocols;
        //the selected is the first one in the list
        setSelectedProtocol( this.protocols[0] );

    }

    public String getSelectedOutputFormat() {
        return selectedOutputFormat;
    }

    public void setSelectedOutputFormat(String selectedOutputFormat) {
        if( selectedOutputFormat == null ) {
            throw new IllegalArgumentException("selectedOutputFormat cannot be null or have zero length");
        }
        this.selectedOutputFormat = selectedOutputFormat;
    }

    public String getSelectedProtocol() {
        return selectedProtocol;
    }

    public void setSelectedProtocol(String selectedProtocol) {
        if( selectedProtocol == null ) {
            throw new IllegalArgumentException("selectedProtocol cannot be null or have zero length");
        }
        this.selectedProtocol = selectedProtocol;
    }
    
}
