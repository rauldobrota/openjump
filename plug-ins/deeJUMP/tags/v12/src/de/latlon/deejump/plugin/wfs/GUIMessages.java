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

import java.lang.reflect.Field;
import java.util.MissingResourceException;
import java.util.ResourceBundle;



/**
 * ...
 * Idea copied from Robert Simmons, Hardcore Java, O'Reilly
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * 
 */
public class GUIMessages {
    public static final String MAIN_DIALOG_TITLE;
    public static final String SEARCHING;

    public static final String WFS_URL;
    // Main dialog
    public static final String OK;
    public static final String CANCEL;
    public static final String GEN_REQUEST;

    public static final String WFS_SERVICE;
    public static final String WFS_SERVICE_TOOL_TIP;
    public static final String CONNECT;

    // Atribute panel
    public static final String ATTR_SEARCH;
    public static final String FEATURE_TYPE;
    public static final String ATTR_BASED;
    public static final String ATTRIBUTE;
    public static final String OPERATOR;
    public static final String COMPARISON_VALUE;
    public static final String ADD_CRITERIA;
    public static final String DEL_CRITERIA;
    public static final String LOGICAL_LINK;
    public static final String LOGICAL_AND;
    public static final String LOGICAL_OR;
    public static final String SPATIAL_CRITERIA;
    public static final String NONE;
    public static final String BBOX;
    public static final String SELECTED_GEOM;

    // spatial panel
    public static final String SPATIAL_SEARCH;
    public static final String SPATIAL_OP;
    public static final String GEO_PROP_NAME;
    public static final String SRS_NAME;
    public static final String INVALID_GEOM_TYPE;
    
    // request text area
    public static final String REQUEST;


    static {
        final String BUNDLE_NAME = GUIMessages.class.getName().replace( '.', '/');
        final ResourceBundle BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME ); 
        
        MAIN_DIALOG_TITLE = getString( BUNDLE, "MAIN_DIALOG_TITLE" );
        SEARCHING = getString( BUNDLE, "SEARCHING" );
        WFS_URL  = getString( BUNDLE, "WFS_URL" );
        CONNECT = getString( BUNDLE, "CONNECT" );
        OK = getString( BUNDLE, "OK" );
        CANCEL = getString( BUNDLE, "CANCEL" );
        ATTR_SEARCH = getString( BUNDLE, "ATTR_SEARCH" );
        SPATIAL_SEARCH = getString( BUNDLE, "SPATIAL_SEARCH" );
        REQUEST = getString( BUNDLE, "REQUEST" );
        WFS_SERVICE = getString( BUNDLE, "WFS_SERVICE" );
        WFS_SERVICE_TOOL_TIP = getString( BUNDLE, "WFS_SERVICE_TOOL_TIP" );
        GEN_REQUEST = getString( BUNDLE, "GEN_REQUEST" );
        ATTR_BASED = getString( BUNDLE, "ATTR_BASED" );
        ATTRIBUTE = getString( BUNDLE, "ATTRIBUTE" );
        OPERATOR = getString( BUNDLE, "OPERATOR" );
        COMPARISON_VALUE = getString( BUNDLE, "COMPARISON_VALUE" );
        ADD_CRITERIA = getString( BUNDLE, "ADD_CRITERIA" );
        DEL_CRITERIA = getString( BUNDLE, "DEL_CRITERIA" );
        LOGICAL_LINK = getString( BUNDLE, "LOGICAL_LINK" );
        LOGICAL_AND = getString( BUNDLE, "LOGICAL_AND" );
        LOGICAL_OR = getString( BUNDLE, "LOGICAL_OR" );
        SPATIAL_CRITERIA = getString( BUNDLE, "SPATIAL_CRITERIA" );
        NONE = getString( BUNDLE, "NONE" );
        BBOX = getString( BUNDLE, "BBOX" );
        SELECTED_GEOM = getString( BUNDLE, "SELECTED_GEOM" );
        SPATIAL_OP = getString( BUNDLE, "SPATIAL_OP" );
        GEO_PROP_NAME = getString( BUNDLE, "GEO_PROP_NAME" );
        FEATURE_TYPE = getString( BUNDLE, "FEATURE_TYPE" );
        SRS_NAME = getString( BUNDLE, "SRS_NAME" );
        INVALID_GEOM_TYPE = getString( BUNDLE, "INVALID_GEOM_TYPE" );
        
    }
    
    
    private GUIMessages(){}
    
    private static String getString(final ResourceBundle bundle, final String key){
        try {
            return bundle.getString( key );
        } catch (final MissingResourceException e) {
//            assert ( false ) : e.getMessage();
            return "!_" + key + "_!";
        }
    }
    
    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
        
        Field[] fields = GUIMessages.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            System.out.println(fields[i].getName() + ": " + fields[i].get(GUIMessages.class));
        }
        
    }
}
