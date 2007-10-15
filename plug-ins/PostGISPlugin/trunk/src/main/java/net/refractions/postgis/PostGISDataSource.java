/*
 * $Id: PostGISDataSource.java,v 1.1.1.1 2004/01/06 00:13:16 pramsey Exp $
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package net.refractions.postgis;

import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;

/**
 * This class implements a data source for a PostGIS database.
 */
// Last update U.D. uwe dalluege, HCU Hamburg, 2006.07.28
public class PostGISDataSource extends DataSource {
  // DriverProperties and DriverPanelCache keys
  public final static String SERVER_KEY = "SERVER";
  public final static String PORT_KEY = "PORT";
  public final static String DATABASE_KEY = "DATABASE";
  public final static String TABLE_KEY = "TABLE";
  public final static String WHERE_KEY = "WHERE";
  public final static String USERNAME_KEY = "USERNAME";
  public final static String PASSWORD_KEY = "PASSWORD";
  public final static String SAVE_METHOD_KEY = "SAVE_METHOD";
  public final static String UNIQUE_COLUMN_KEY = "UNIQUE_COLUMN";
  
  public final static String SAVE_METHOD_INSERT = "INSERT";
  public final static String SAVE_METHOD_UPDATE = "UPDATE";
  
// UD, 2006.07.28
  public final static String SAVE_METHOD_DELETE = "DELETE";
  public final static String SAVE_METHOD_OVERWRITE = "OVERWRITE";
  
  PostGISConnection conn = null;
  
  /**
   * Configures and returns a data source connection.
   */
  public Connection getConnection() {
    if (conn == null) conn = new PostGISConnection();
    conn.setProperties(getProperties());
    return(conn);
  }
}