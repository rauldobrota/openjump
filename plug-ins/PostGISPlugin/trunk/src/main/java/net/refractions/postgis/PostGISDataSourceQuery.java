/*
 * $Id: PostGISDataSourceQuery.java,v 1.1.1.1 2004/01/06 00:13:16 pramsey Exp $
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

import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import java.util.HashMap;

/**
 * This class represents a query against a PostGIS data source. 
 */
public class PostGISDataSourceQuery extends DataSourceQuery {
  private HashMap properties = null;
  
  /**
   * Creates a new query.
   * @param dataSource The data source go be query against.
   * @param query The "sql" of the query.
   * @param name Name of the query.
   */
  public PostGISDataSourceQuery(DataSource dataSource, String query, String name) {
    super(dataSource, query, name);
  }
  
  /**
   * Returns the DataSource for the query.
   */
  public DataSource getDataSource() 
  {
    DataSource ds = super.getDataSource();
    ds.setProperties(properties);
    return(ds);
  }
  
  /**
   * Property map for the query object. For defined keys see: {@link PostGISDataSource}
   */
  public void setProperties(HashMap properties) {
    this.properties = (HashMap)properties.clone();  
  }
}  