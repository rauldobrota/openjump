/*
 * $Id: PostGISSaveDataSourceQueryChooser.java,v 1.1.1.1 2004/01/06 00:13:16 pramsey Exp $
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
// UD, uwe.dalluege@rzcn.haw-hamburg.de changed 15.07.2005
package net.refractions.postgis;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * A DataSourceQueryChooser for writing to a PostGIS data source.
 */
public class PostGISSaveDataSourceQueryChooser extends PostGISDataSourceQueryChooser {
  private PostGISSaveDriverPanel panel;
  private HashMap properties;
  
  
  ButtonGroup methodButtons;
  JRadioButton insertButton;
  JRadioButton updateButton;
  JTextArea help;
  JTextField uniqueField;
  
  /**
   * Creates a new query chooser.
   * @param dataSource DataSource object to be queried against.
   */
  public PostGISSaveDataSourceQueryChooser(PostGISDataSource dataSource) {
    super(dataSource); 
    panel = new PostGISSaveDriverPanel();
  }
  
  /**
   * @see com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser#getComponent()
   */
  public Component getComponent() { 
  	return(panel); 
  }
  
  /**
   * Since the ui does not allow for loading of multiple tables, 
   * the returned collection only contains a single element.
   * @see com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser#getDataSourceQueries()
   */
  public Collection getDataSourceQueries() {
    StringBuffer sql = new StringBuffer();
    Map properties = super.getProperties();
    
    //we dont need to specify the update query since the connection 
    // will do that for us. We only need to specify the table
// UD, change from null to updateQuery
		String updateQuery = 
      "SELECT * FROM " +
      (String)properties.get(PostGISDataSource.TABLE_KEY);
// UD, new updateQuery      
    PostGISDataSourceQuery query = new PostGISDataSourceQuery(
      getDataSource(), updateQuery, 
      (String)properties.get(PostGISDataSource.TABLE_KEY)
    );    
    query.setProperties(getProperties());
    
    List queries = new ArrayList();
    queries.add(query);
    
    return(queries);
  }
  
  /**
   * Checks that user input is valid.
   * @see com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser#isInputValid()
   */
  public boolean isInputValid() {
    if (!super.isInputValid()) return(false);
//    if (panel.getSaveMethod().equals(PostGISDataSource.SAVE_METHOD_UPDATE) 
//     && panel.getUniqueColumn().equals(""))
    
    if ( panel.getUniqueColumn().equals("") && 
    		panel.getSaveMethod().equals(PostGISDataSource.SAVE_METHOD_UPDATE ) )    
    { // U.D. 
					JOptionPane.showMessageDialog 
						( panel, 
							"Unique Column does not exist!",
							"Error!", JOptionPane.ERROR_MESSAGE );			    	
    	return(false);
    }
    return(true);
  }
  
  /**
   * Reads all the connection + query properties from the ui.
   */
  protected HashMap getProperties() {
    if (properties == null) properties = new HashMap();
    properties.put(PostGISDataSource.SERVER_KEY, panel.getServer());
    properties.put(PostGISDataSource.PORT_KEY, panel.getPort());
    properties.put(PostGISDataSource.DATABASE_KEY, panel.getDatabase());
    properties.put(PostGISDataSource.TABLE_KEY, panel.getTable());
    properties.put(PostGISDataSource.WHERE_KEY, panel.getWhere());
    properties.put(PostGISDataSource.USERNAME_KEY, panel.getUsername());
    properties.put(PostGISDataSource.PASSWORD_KEY, panel.getPassword());
    properties.put(PostGISDataSource.SAVE_METHOD_KEY, panel.getSaveMethod());
    properties.put(PostGISDataSource.UNIQUE_COLUMN_KEY, panel.getUniqueColumn());
    return(properties);
  }
}