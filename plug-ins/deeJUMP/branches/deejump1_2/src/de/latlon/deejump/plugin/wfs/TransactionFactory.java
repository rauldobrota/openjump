/*----------------    FILE HEADER  ------------------------------------------

Copyright (C) 2001-2005 by:
lat/lon GmbH
http://www.lat-lon.de

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Contact:

Andreas Poth
lat/lon GmbH
Aennchenstraﬂe 19
53177 Bonn
Germany


 ---------------------------------------------------------------------------*/

package de.latlon.deejump.plugin.wfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.filterencoding.AbstractOperation;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.GeometryImpl;
import org.deegree.model.spatialschema.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.FeatureEventType;

/**
 * Factory class to generate WFS transaction requests. 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class TransactionFactory {
    
    
    /** the srs to be used in requests containing gml */
    //FIXME
    private static String crs = "-1";
    
    /** common transaction header */
    private static final String REQUEST_HEADER = "<?xml version='1.0' encoding='ISO-8859-1'?>" +
    		"<wfs:Transaction version='1.1.0' service='WFS' " +
            "xmlns:gml='http://www.opengis.net/gml' " +
    		"xmlns:ogc='http://www.opengis.net/ogc' xmlns:wfs='http://www.opengis.net/wfs' " +
    		"xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +    		
    		"xsi:schemaLocation='http://www.opengis.net/wfs/1.0.0/WFS-transaction.xsd' ";
    
    /**
     * Prevent instantiation
     */
    private TransactionFactory() {
    }    
    
    /**
     * Combines geometry update xml with attribute update xml into a common 
     * transaction update xml
     * 
     * @param geomFragment the update fragment containing features with changed geometries 
     * @param attrFragment the update fragment containing features with changed attributes
     * @return an xml containing a full xml update request
     */
    public static final StringBuffer createCommonUpdateTransaction( QualifiedName featureType, 
                                                                    StringBuffer geomFragment, 
                                                                    StringBuffer attrFragment ){
        
        StringBuffer sb = new StringBuffer();
        
        if ( geomFragment == null  && attrFragment == null  ){
            return sb;
        }

        sb.append( REQUEST_HEADER )
        .append( "xmlns:" ).append( featureType.getPrefix() ).append( "='")
        .append( featureType.getNamespace() ).append( "'  >");
        
        if ( geomFragment != null){
        	sb.append( geomFragment );            
        }
        if ( attrFragment != null){
        	sb.append( attrFragment );           
        }
            
        sb.append( "</wfs:Transaction>" );
        return sb;        
    }

    /**
     * Generates an update transcation request.
     * @param fet the feature event type (FeatureEventType.GEOMETRY_MODIFIED or FeatureEventType.ATTRIBUTE_MODIFIED)
     * @param featureType the name of the WFS feature type
     * @param newFeatures list containing features to be updated
     * @param oldFeatures list containing original features (those are used as filter) 
     * @return an XML fragment containing an update transaction
     */
    public static final StringBuffer createUpdateTransaction( 
            FeatureEventType fet, QualifiedName featureType, QualifiedName geoPropName,
            ArrayList newFeatures, HashMap oldFeatures ){
        StringBuffer sb = new StringBuffer();
        if ( featureType == null){
            return sb;
        }
        if ( newFeatures == null || newFeatures.size() < 1 ){
            return sb;
        }
        
        appendUpdate( fet, sb, featureType, geoPropName, newFeatures, oldFeatures );
            
        return sb;        
    }
    
    public static final StringBuffer createTransaction( 
            FeatureEventType transacType, 
            QualifiedName featureType, QualifiedName geoPropName, ArrayList newFeatures ){
        
        StringBuffer sb = new StringBuffer();
        if ( featureType == null){
            return sb;
        }
        if ( newFeatures == null || newFeatures.size() < 1 ){
            return sb;
        }
        
        sb.append( REQUEST_HEADER )
        .append( "xmlns:" ).append( featureType.getPrefix() ).append( "='")
        .append( featureType.getNamespace() ).append( "'  >");
        
        if ( transacType.equals( FeatureEventType.ADDED )){      
            appendInsert( sb, featureType, geoPropName, newFeatures );
            
        } else if ( transacType.equals( FeatureEventType.DELETED )){            
            appendDelete( sb, featureType, newFeatures );          
        }

        sb.append( "</wfs:Transaction>" );
        return sb;        
    }
    
    /** creates and append an update request to a string buffer
     * 
     * @param fet the feature event type
     * @param sb the StringBuffer to append to
     * @param featureType the feature type name
     * @param features the list of features
     * @param oldFeatures a ap containing the old features
     */
    private static final void appendUpdate( FeatureEventType fet, 
            StringBuffer sb, QualifiedName featureType, QualifiedName geoPropName,
            ArrayList features, HashMap oldFeatures){
        

        for (Iterator iter = features.iterator(); iter.hasNext();) {

            Feature feat = (Feature) iter.next();
            sb.append( "<wfs:Update typeName='" )
            .append( featureType.getPrefix() ).append( ":" ).append( featureType.getLocalName() )
            .append("'>");
            sb.append( createPropertiesFragment( geoPropName, featureType,fet, feat) );

            sb.append( "<ogc:Filter>" );
            
		    Feature oldFeat = (Feature) oldFeatures.get( feat );
		    sb.append( createOperationFragment(oldFeat, featureType ) );
		    		    
		    sb.append( "</ogc:Filter>" );
            sb.append( "</wfs:Update>" );
        }
    }

    
    /**
     * Appends an insert transaction to a string buffer
     * @param sb the strign buffer to append to
     * @param featureType the feature type name
     * @param features the list of new features
     */
    private static final void appendInsert( StringBuffer sb, QualifiedName featureType, 
                                            QualifiedName geoPropName, ArrayList features){

        sb.append( "<wfs:Insert handle='insert1' idgen='GenerateNew' >" );
        
        for (Iterator iter = features.iterator(); iter.hasNext();) {
            Feature feat = (Feature) iter.next();
            String s = new StringBuilder()
                .append( featureType.getPrefix() )
                .append( ":" )
                .append( featureType.getLocalName() )
                .toString();
            
            sb.append( "<" ).append( s ).append( ">" );
            sb.append( createInsertPropertiesFragment( geoPropName, featureType, feat ) );
            sb.append( "</" ).append( s ).append( ">" );
        }
        
        sb.append( "</wfs:Insert>" );
    }

    /**
     * Appends a delete transactio to an existing string buffer
     * @param sb the string buffer to append to
     * @param featureType the feature type name
     * @param features the list of new features to be deleted
     */
    private static final void appendDelete( StringBuffer sb, QualifiedName featureType, ArrayList features){
		for (Iterator iter = features.iterator(); iter.hasNext();) {
	        sb.append( "<wfs:Delete typeName='" )
            .append( featureType.getPrefix() ).append( ":" )
            .append( featureType.getLocalName() ).append("'>")      
        	.append( "<ogc:Filter>" ); 

	        Feature feat = (Feature) iter.next();
		    sb.append( createOperationFragment(feat, featureType) );
		    
		    sb.append( "</ogc:Filter></wfs:Delete>" );
		}
    }

    /**
     *  Creates a StringBuffer containing the gml representation of a geometry
     * @return gml representing the input geometry
     * @param featureType the feature type name (for insert requests; use null otherwise)
     * @param geometry the geometry
     * @return
     */
    public static final StringBuffer createGeometryGML( Geometry geometry ){ 
        org.deegree.model.spatialschema.Geometry gg = null;
        try {

            CoordinateSystem cs = CRSFactory.create( crs );
            gg = JTSAdapter.wrap( geometry );
            ( (GeometryImpl) gg).setCoordinateSystem( cs );
        } catch ( Exception e) {
            e.printStackTrace();
        }
        
        StringBuffer sb = null;
        try {
            sb = GMLGeometryAdapter.export( gg );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        
        return sb;
	}
    
    /**
     * @return Returns the srs.
     */
    public static String getCrs() {
        return crs;
    }
    /**
     * @param srs The srs to set.
     */
    public static void setCrs(String crs) {
        TransactionFactory.crs = crs;
    }
    
    /**
     * Creates an xml fragment defining properties
     * @param featureType 
     * @param featureType the name of the feature type
     * @param fet the feature type event
     * @param bf the feature
     * @return an xml fragment defining properties
     */
    private static final StringBuffer createPropertiesFragment( 
            QualifiedName geoPropName, 
            QualifiedName featureType, FeatureEventType fet,
            Feature bf){        
        
        StringBuffer sb = new StringBuffer(  );
        Object[] os = bf.getAttributes();
        FeatureSchema fs = bf.getSchema();
        
        for (int j = 0; j < os.length; j++) {

            String attName = fs.getAttributeName(j);
            
            if( ( !"GEOMETRY".equalsIgnoreCase( attName ) && 
                    fet == FeatureEventType.ATTRIBUTES_MODIFIED ) ){
                Object attValue = bf.getAttribute(j);
                if ( attValue != null ){
                    sb.append( "<wfs:Property><wfs:Name>" ).append( featureType.getPrefix() ).append( ":" )
                    .append( attName ).append( "</wfs:Name>" )
                    .append( "<wfs:Value>").append( attValue ).append( "</wfs:Value></wfs:Property>");
                }
            } else if (  "GEOMETRY".equalsIgnoreCase( attName ) &&
                    fet == FeatureEventType.GEOMETRY_MODIFIED ) {

                sb.append( "<wfs:Property><wfs:Name>" )
                	.append( featureType.getPrefix() ).append( ":" ).append( geoPropName.getLocalName() )
                	.append( "</wfs:Name><wfs:Value>" );
                sb.append( createGeometryGML( bf.getGeometry()) )
                	.append( "</wfs:Value></wfs:Property>");
            }
        }
        return sb;
    }

    /**
     * Creates a fragment containing properties and their values
     * @param featureType 
     * @param featureType
     * @param bf
     * @return
     */
    private static final StringBuffer createInsertPropertiesFragment( 
            QualifiedName geoAttName, QualifiedName featureType, Feature bf){
        
        StringBuffer sb = new StringBuffer(  );
        Object[] attributes = bf.getAttributes();
        FeatureSchema featSchema = bf.getSchema();
        
        for (int j = 0; j < attributes.length; j++) {
            
            String attName = featSchema.getAttributeName(j);

            if( !"GEOMETRY".equalsIgnoreCase( attName ) ){
                Object attValue = bf.getAttribute(j);
                // FIXME ist this right?
                if ( attValue != null ){
                    sb.append( "<" ).append( featureType.getPrefix() ).append( ":" )
                    .append( attName ).append( ">" );
                    sb.append( attValue );
                    sb.append( "</" ).append( featureType.getPrefix() ).append( ":" )
                    .append( attName ).append( ">" );
                }
            } else {
                
                sb.append( "<" ).append( featureType.getPrefix() ).append( ":" )
                .append( geoAttName.getLocalName() ).append( ">" );
                sb.append( createGeometryGML( bf.getGeometry()) );
                sb.append( "</" ).append( featureType.getPrefix() ).append( ":" )
                .append( geoAttName.getLocalName() ).append( ">" );
            }
        }
        return sb;
    }

    /**
     * Creates a fragment with a PropertyIsEqualTo filter operation from a feature bf. 
     * @param bf
     * @return
     */
    private static final StringBuffer createOperationFragment( Feature bf, QualifiedName featureType ){

        StringBuffer sb = new StringBuffer(  );
        Object[] os = bf.getAttributes();
        FeatureSchema fs = bf.getSchema();
        int featCount = 0;
        for (int j = 0; j < os.length; j++) {
            
            String attName = fs.getAttributeName(j);
            if( !"GEOMETRY".equalsIgnoreCase( attName )){
                Object attValue = bf.getAttribute(j);
                
                if ( attValue != null ){
                    
                    double value = 0.1;
            	
        			try {
                        value = Double.parseDouble( attValue.toString() );
                        
                    } catch (NumberFormatException e) {
                        value = 0d;
                    }
                    
                    value = value - (int)value;
                    if ( value == 0d ){                                        
                        String attNameWoPrefix = attName.substring( attName.indexOf( ":" ) + 1, attName.length() );
                        QualifiedName qn = new QualifiedName( featureType.getPrefix(), attNameWoPrefix, featureType.getNamespace() );
                        AbstractOperation oper = new PropertyIsCOMPOperation( 100, 
                                                                              new PropertyName( qn ), new Literal( attValue.toString() ) );
                        sb.append( oper.toXML() );
                        featCount++;
                    }
                }
            }
        }
        
        if( featCount > 1 ){
            sb.insert( 0, "<ogc:And>" );
	        sb.append( "</ogc:And>" );			    
	    }
        
        return sb;
    }

    
    
}
