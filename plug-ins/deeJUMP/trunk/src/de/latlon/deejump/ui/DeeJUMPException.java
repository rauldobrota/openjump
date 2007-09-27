//$Header$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2006 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
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

package de.latlon.deejump.ui;

/**
 * ... 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author: taddei $
 * 
 * @version 2.0, $Revision: 147 $, $Date: 2007-05-08 11:28:04 +0200 (Di, 08 Mai 2007) $
 * 
 * @since 2.0
 */

public class DeeJUMPException extends Exception {


    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DeeJUMPException( String message ) {
        super( message );
    }

    public DeeJUMPException( String message, Throwable cause ) {
        super( message, cause );
    }

    public DeeJUMPException( Throwable cause ) {
        super( cause );
    }

}


/* ********************************************************************
Changes to this class. What the people have been up to:
$Log$
Revision 1.2  2007/05/08 09:27:54  taddei
deejump final (UT)

Revision 1.1.2.1  2006/05/31 10:04:35  ut
deejump pre deegree day


********************************************************************** */