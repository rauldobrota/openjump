/*
 * $Id: PostGISLoadDataSourceQueryChooser.java,v 1.1.1.1 2004/01/06 00:13:16 pramsey Exp $
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

import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser;
import java.util.*;

/**
 * A DataSourceQueryChooser for writing to a PostGIS data source.
 */
public class PostGISLoadDataSourceQueryChooser extends PostGISDataSourceQueryChooser implements DataSourceQueryChooser {
  
  /**
   * Creates a new query chooser.
   * @param dataSource DataSource object to be queried against.
   */
  public PostGISLoadDataSourceQueryChooser(PostGISDataSource dataSource) {
    super(dataSource);  
  }
  
  /**
   * Since the ui does not allow for loading of multiple tables, 
   * the returned collection only contains a single element.
   * @see DataSourceQueryChooser#getDataSourceQueries()
   */
  public Collection getDataSourceQueries() {
    StringBuffer sql = new StringBuffer();
    Map properties = super.getProperties();
    
    sql.append(
      "SELECT * " + 
        "FROM " + (String)properties.get(PostGISDataSource.TABLE_KEY)
    );
    PostGISDataSourceQuery query = new PostGISDataSourceQuery(
      getDataSource(), sql.toString(), (String)properties.get(PostGISDataSource.TABLE_KEY)
    );    
    query.setProperties(getProperties());
    
    List queries = new ArrayList();
    queries.add(query);
    
    return(queries);
  }
}