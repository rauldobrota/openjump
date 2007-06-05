/*
 * Created on 09.11.2005 for PIROL
 *
 * SVN header information:
 *  $Author$
 *  $Rev: 2434 $
 *  $Date$
 *  $Id$
 */
package de.fho.jump.pirol.utilities.FeatureCollection;

/**
 * Role for FeatureCollections that contain point geometries, only
 *
 * @author Ole Rahn
 * <br>
 * <br>FH Osnabr&uuml;ck - University of Applied Sciences Osnabr&uuml;ck,
 * <br>Project: PIROL (2005),
 * <br>Subproject: Daten- und Wissensmanagement
 * 
 * @version $Rev: 2434 $
 * 
 */
public class RolePointFeatures extends PirolFeatureCollectionRole {

    /**
     *
     */
    public RolePointFeatures() {
        super(PirolFeatureCollectionRoleTypes.POINTLAYER);
    }

}
