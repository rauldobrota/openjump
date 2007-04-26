/*
 * (c) 2007 by lat/lon GmbH
 *
 * @author Ugo Taddei (taddei@latlon.de)
 *
 * This program is free software under the GPL (v2.0)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package de.latlon.deejump.ui;

/**
 * ... 
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
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
Revision 1.1  2007/04/26 09:19:26  taddei
Added initial working version of classes and complementary files.

Revision 1.1.2.1  2006/05/31 10:04:35  ut
deejump pre deegree day


********************************************************************** */