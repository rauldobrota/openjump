//$Header$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2005 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/exse/
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
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 
 ---------------------------------------------------------------------------*/

package de.latlon.deejump;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * ... 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author: ut $
 * 
 * @version 2.0, $Revision: 83 $, $Date: 2006-03-06 10:37:56 +0100 (Mo, 06 M√§r 2006) $
 * 
 * @since 2.0
 */
public class DeeJUMPProperties {
    private static final String BUNDLE_NAME = "de.latlon.deejump.deejump";//$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

    private DeeJUMPProperties() {
    }

    public static String getString( String key ) {
        // TODO Auto-generated method stub
        try {
            return RESOURCE_BUNDLE.getString( key );
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}

/* ********************************************************************
 Changes to this class. What the people have been up to:
 $Log$
 Revision 1.1  2006/03/06 09:37:56  ut
 properties for startup


 ********************************************************************** */