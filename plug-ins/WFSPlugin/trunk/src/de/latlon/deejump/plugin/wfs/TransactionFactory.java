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
 Aennchenstra�e 19
 53177 Bonn
 Germany


 ---------------------------------------------------------------------------*/

package de.latlon.deejump.plugin.wfs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
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
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.FeatureEventType;

import de.latlon.deejump.plugin.wfs.auth.UserData;

/**
 * Factory class to generate WFS transaction requests.
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class TransactionFactory {

    private static Logger LOG = Logger.getLogger( TransactionFactory.class );

    /** the srs to be used in requests containing gml */
    // FIXME
    private static String crs = "-1";

    /** common transaction header */
    private static final String REQUEST_HEADER = "<?xml version='1.0' encoding='ISO-8859-1'?>"
                                                 + "<wfs:Transaction version='1.1.0' service='WFS' "
                                                 + "xmlns:gml='http://www.opengis.net/gml' "
                                                 + "xmlns:ogc='http://www.opengis.net/ogc' xmlns:wfs='http://www.opengis.net/wfs' "
                                                 + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                                                 + "xsi:schemaLocation='http://www.opengis.net/wfs/1.1.0/WFS-transaction.xsd' ";

    // private static final DateFormat formatter = DateFormat.getDateInstance( DateFormat.SHORT,
    // Locale.US );

    private static final SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ss" );

    /**
     * Combines geometry update xml with attribute update xml into a common transaction update xml
     * 
     * @param context
     * 
     * @param featureType
     * 
     * @param geomFragment
     *            the update fragment containing features with changed geometries
     * @param attrFragment
     *            the update fragment containing features with changed attributes
     * @return an xml containing a full xml update request
     */
    public static final StringBuffer createCommonUpdateTransaction( WorkbenchContext context,
                                                                    QualifiedName featureType,
                                                                    StringBuffer geomFragment, StringBuffer attrFragment ) {

        StringBuffer sb = new StringBuffer();

        if ( geomFragment == null && attrFragment == null ) {
            return sb;
        }

        sb.append( REQUEST_HEADER );

        UserData logins = (UserData) context.getBlackboard().get( "LOGINS" );
        if ( logins != null && logins.getUsername() != null && logins.getPassword() != null ) {
            sb.append( "\nuser=\"" ).append( logins.getUsername() );
            sb.append( "\"\npassword=\"" ).append( logins.getPassword() ).append( "\"\n" );
        }

        sb.append( "xmlns:" ).append( featureType.getPrefix() ).append( "='" ).append( featureType.getNamespace() ).append(
                                                                                                                            "'  >" );

        if ( geomFragment != null ) {
            sb.append( geomFragment );
        }
        if ( attrFragment != null ) {
            sb.append( attrFragment );
        }

        sb.append( "</wfs:Transaction>" );
        return sb;
    }

    /**
     * Generates an update transcation request.
     * 
     * @param fet
     *            the feature event type (FeatureEventType.GEOMETRY_MODIFIED or
     *            FeatureEventType.ATTRIBUTE_MODIFIED)
     * @param featureType
     *            the name of the WFS feature type
     * @param geoPropName
     * @param newFeatures
     *            list containing features to be updated
     * @param oldFeatures
     *            list containing original features (those are used as filter)
     * @return an XML fragment containing an update transaction
     */
    public static final StringBuffer createUpdateTransaction( FeatureEventType fet, QualifiedName featureType,
                                                              QualifiedName geoPropName,
                                                              ArrayList<Feature> newFeatures,
                                                              HashMap<Feature, Feature> oldFeatures ) {
        StringBuffer sb = new StringBuffer();
        if ( featureType == null ) {
            return sb;
        }
        if ( newFeatures == null || newFeatures.size() < 1 ) {
            return sb;
        }

        appendUpdate( fet, sb, featureType, geoPropName, newFeatures, oldFeatures );

        return sb;
    }

    public static final StringBuffer createTransaction( WorkbenchContext context, FeatureEventType transacType,
                                                        QualifiedName featureType, QualifiedName geoPropName,
                                                        ArrayList<Feature> newFeatures ) {

        StringBuffer sb = new StringBuffer();
        if ( featureType == null ) {
            return sb;
        }
        if ( newFeatures == null || newFeatures.size() < 1 ) {
            return sb;
        }

        sb.append( REQUEST_HEADER );

        UserData logins = (UserData) context.getBlackboard().get( "LOGINS" );
        if ( logins != null && logins.getUsername() != null && logins.getPassword() != null ) {
            sb.append( "\nuser=\"" ).append( logins.getUsername() );
            sb.append( "\"\npassword=\"" ).append( logins.getPassword() ).append( "\"\n" );
        }

        sb.append( "xmlns:" ).append( featureType.getPrefix() ).append( "='" ).append( featureType.getNamespace() ).append(
                                                                                                                            "'  >" );

        if ( transacType.equals( FeatureEventType.ADDED ) ) {
            appendInsert( sb, featureType, geoPropName, newFeatures );

        } else if ( transacType.equals( FeatureEventType.DELETED ) ) {
            appendDelete( sb, featureType, newFeatures );
        }

        sb.append( "</wfs:Transaction>" );
        return sb;
    }

    /**
     * creates and append an update request to a string buffer
     * 
     * @param fet
     *            the feature event type
     * @param sb
     *            the StringBuffer to append to
     * @param featureType
     *            the feature type name
     * @param features
     *            the list of features
     * @param oldFeatures
     *            a ap containing the old features
     */
    private static final void appendUpdate( FeatureEventType fet, StringBuffer sb, QualifiedName featureType,
                                            QualifiedName geoPropName, ArrayList<Feature> features,
                                            HashMap<Feature, Feature> oldFeatures ) {

        for ( Iterator<Feature> iter = features.iterator(); iter.hasNext(); ) {

            Feature feat = iter.next();
            sb.append( "<wfs:Update typeName='" ).append( featureType.getPrefix() ).append( ":" ).append(
                                                                                                          featureType.getLocalName() ).append(
                                                                                                                                               "'>" );
            sb.append( createPropertiesFragment( geoPropName, featureType, fet, feat ) );

            Feature oldFeat = oldFeatures.get( feat );
            StringBuffer s = createOperationFragment( oldFeat, featureType );
            if ( s.length() > 0 ) {
                sb.append( "<ogc:Filter>" );
                sb.append( s );
                sb.append( "</ogc:Filter>" );
            }

            sb.append( "</wfs:Update>" );
        }
    }

    /**
     * Appends an insert transaction to a string buffer
     * 
     * @param sb
     *            the strign buffer to append to
     * @param featureType
     *            the feature type name
     * @param features
     *            the list of new features
     */
    private static final void appendInsert( StringBuffer sb, QualifiedName featureType, QualifiedName geoPropName,
                                            ArrayList<Feature> features ) {

        sb.append( "<wfs:Insert handle='insert1' idgen='GenerateNew' >" );

        for ( Iterator<Feature> iter = features.iterator(); iter.hasNext(); ) {
            Feature feat = iter.next();
            String s = new StringBuilder().append( featureType.getPrefix() ).append( ":" ).append(
                                                                                                   featureType.getLocalName() ).toString();

            sb.append( "<" ).append( s ).append( ">" );
            sb.append( createInsertPropertiesFragment( geoPropName, featureType, feat ) );
            sb.append( "</" ).append( s ).append( ">" );
        }

        sb.append( "</wfs:Insert>" );
    }

    /**
     * Appends a delete transactio to an existing string buffer
     * 
     * @param sb
     *            the string buffer to append to
     * @param featureType
     *            the feature type name
     * @param features
     *            the list of new features to be deleted
     */
    private static final void appendDelete( StringBuffer sb, QualifiedName featureType, ArrayList<Feature> features ) {
        for ( Iterator<Feature> iter = features.iterator(); iter.hasNext(); ) {
            sb.append( "<wfs:Delete typeName='" ).append( featureType.getPrefix() ).append( ":" ).append(
                                                                                                          featureType.getLocalName() ).append(
                                                                                                                                               "'>" );

            Feature feat = iter.next();
            StringBuffer s = createOperationFragment( feat, featureType );
            if ( s.length() > 0 ) {
                sb.append( "<ogc:Filter>" );
                sb.append( s );
                sb.append( "</ogc:Filter>" );
            }

            sb.append( "</wfs:Delete>" );
        }
    }

    /**
     * Creates a StringBuffer containing the gml representation of a geometry
     * 
     * @return gml representing the input geometry
     * @param geometry
     *            the geometry
     */
    public static final StringBuffer createGeometryGML( Geometry geometry ) {
        org.deegree.model.spatialschema.Geometry gg = null;
        try {
            LOG.debug( "Using crs " + crs );
            CoordinateSystem cs = CRSFactory.create( crs );
            gg = JTSAdapter.wrap( geometry );
            ( (GeometryImpl) gg ).setCoordinateSystem( cs );
        } catch ( Exception e ) {
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
     * @param crs
     *            The srs to set.
     */
    public static void setCrs( String crs ) {
        TransactionFactory.crs = crs;
    }

    /**
     * Creates an xml fragment defining properties
     * 
     * @param featureType
     * @param featureType
     *            the name of the feature type
     * @param fet
     *            the feature type event
     * @param bf
     *            the feature
     * @return an xml fragment defining properties
     */
    private static final StringBuffer createPropertiesFragment( QualifiedName geoPropName, QualifiedName featureType,
                                                                FeatureEventType fet, Feature bf ) {

        StringBuffer sb = new StringBuffer();
        Object[] os = bf.getAttributes();
        FeatureSchema fs = bf.getSchema();

        for ( int j = 0; j < os.length; j++ ) {

            String attName = fs.getAttributeName( j );

            LOG.debug( "Shall we insert attribute " + attName + "?" );

            if ( ( ( !( fs.getAttributeType( j ) == AttributeType.GEOMETRY ) ) && fet == FeatureEventType.ATTRIBUTES_MODIFIED ) ) {
                LOG.debug( "Inserting modified attribute." );

                if ( fs.getAttributeType( j ) == AttributeType.DATE ) {
                    Date attValue = (Date) bf.getAttribute( j );
                    String val = formatter.format( attValue );
                    if ( attValue != null ) {
                        LOG.debug( "Inserting date value of " + val );
                        sb.append( "<wfs:Property><wfs:Name>" ).append( featureType.getPrefix() ).append( ":" );
                        sb.append( attName ).append( "</wfs:Name>" ).append( "<wfs:Value>" ).append( val );
                        sb.append( "</wfs:Value></wfs:Property>" );
                    }
                } else {
                    Object attValue = bf.getAttribute( j );
                    if ( attValue != null ) {
                        sb.append( "<wfs:Property><wfs:Name>" ).append( featureType.getPrefix() ).append( ":" );
                        sb.append( attName ).append( "</wfs:Name>" ).append( "<wfs:Value>" ).append( attValue );
                        sb.append( "</wfs:Value></wfs:Property>" );
                    }
                }
            } else if ( ( fs.getAttributeType( j ) == AttributeType.GEOMETRY )
                        && fet == FeatureEventType.GEOMETRY_MODIFIED ) {
                LOG.debug( "Inserting modified geometry." );
                if ( fs.getAttributeName( j ).equals( "FAKE_GEOMETRY" ) ) {
                    LOG.debug( "Skipping fake geometry." );
                    continue;
                }
                sb.append( "<wfs:Property><wfs:Name>" );
                sb.append( featureType.getPrefix() );
                sb.append( ":" ).append( geoPropName.getLocalName() );
                sb.append( "</wfs:Name><wfs:Value>" );
                sb.append( createGeometryGML( bf.getGeometry() ) );
                sb.append( "</wfs:Value></wfs:Property>" );
            }
        }
        return sb;
    }

    /**
     * Creates a fragment containing properties and their values
     * 
     * @param featureType
     * @param featureType
     * @param bf
     * @return the fragment
     */
    private static final StringBuffer createInsertPropertiesFragment( QualifiedName geoAttName,
                                                                      QualifiedName featureType, Feature bf ) {

        LOG.debug( "Ok, creating insert properties." );

        StringBuffer sb = new StringBuffer();
        Object[] attributes = bf.getAttributes();
        FeatureSchema featSchema = bf.getSchema();

        for ( int j = 0; j < attributes.length; j++ ) {

            String attName = featSchema.getAttributeName( j );

            LOG.debug( "Pondering about property with name " + attName );

            if ( !( featSchema.getAttributeType( j ) == AttributeType.GEOMETRY ) ) {
                LOG.debug( "Not a geometry." );

                if ( featSchema.getAttributeType( j ) == AttributeType.DATE ) {
                    Date attValue = (Date) bf.getAttribute( j );
                    String val = formatter.format( attValue );
                    if ( attValue != null ) {
                        sb.append( "<" ).append( featureType.getPrefix() ).append( ":" ).append( attName ).append( ">" );
                        sb.append( val );
                        sb.append( "</" ).append( featureType.getPrefix() ).append( ":" ).append( attName ).append( ">" );
                    }
                } else {
                    Object attValue = bf.getAttribute( j );
                    if ( attValue != null ) {
                        sb.append( "<" ).append( featureType.getPrefix() ).append( ":" ).append( attName ).append( ">" );
                        sb.append( attValue );
                        sb.append( "</" ).append( featureType.getPrefix() ).append( ":" ).append( attName ).append( ">" );
                    }
                }
            } else {
                LOG.debug( "It's a geometry." );
                LOG.debug( attName.equals( "GEOMETRY" ) ? "Schema not loaded? Using strange mechanisms here!"
                                                       : "Ok, using schema." );
                if ( featSchema.getAttributeName( j ).equals( "FAKE_GEOMETRY" ) ) {
                    LOG.debug( "Skipping fake geometry." );
                    continue;
                }
                sb.append( "<" ).append( featureType.getPrefix() ).append( ":" );
                sb.append( attName.equals( "GEOMETRY" ) ? geoAttName.getLocalName() : attName ).append( ">" );
                sb.append( createGeometryGML( bf.getGeometry() ) );
                sb.append( "</" ).append( featureType.getPrefix() ).append( ":" );
                sb.append( attName.equals( "GEOMETRY" ) ? geoAttName.getLocalName() : attName ).append( ">" );
            }
        }
        return sb;
    }

    /**
     * Creates a fragment with a PropertyIsEqualTo filter operation from a feature bf.
     * 
     * @param bf
     * @return the fragment
     */
    private static final StringBuffer createOperationFragment( Feature bf, QualifiedName featureType ) {

        StringBuffer sb = new StringBuffer();
        Object[] os = bf.getAttributes();
        FeatureSchema fs = bf.getSchema();
        int featCount = 0;
        for ( int j = 0; j < os.length; j++ ) {

            String attName = fs.getAttributeName( j );
            if ( !( fs.getAttributeType( j ) == AttributeType.GEOMETRY ) ) {
                Object attValue = bf.getAttribute( j );

                if ( attValue != null ) {

                    double value = 0.1;

                    try {
                        value = Double.parseDouble( attValue.toString() );

                    } catch ( NumberFormatException e ) {
                        value = 0d;
                    }

                    value = value - (int) value;
                    if ( value == 0d ) {
                        String attNameWoPrefix = attName.substring( attName.indexOf( ":" ) + 1, attName.length() );
                        QualifiedName qn = new QualifiedName( featureType.getPrefix(), attNameWoPrefix,
                                                              featureType.getNamespace() );
                        AbstractOperation oper = new PropertyIsCOMPOperation( 100, new PropertyName( qn ),
                                                                              new Literal( attValue.toString() ) );
                        sb.append( oper.toXML() );
                        featCount++;
                    }
                }
            }
        }

        if ( featCount > 1 ) {
            sb.insert( 0, "<ogc:And>" );
            sb.append( "</ogc:And>" );
        }

        return sb;
    }

}
