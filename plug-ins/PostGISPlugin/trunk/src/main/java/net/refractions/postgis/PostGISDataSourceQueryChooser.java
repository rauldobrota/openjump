/*
 * $Id: PostGISDataSourceQueryChooser.java,v 1.1.1.1 2004/01/06 00:13:16 pramsey Exp $
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

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser;

/**
 * A DataSourceQueryChooser for a PostGIS data source.
 */
public abstract class PostGISDataSourceQueryChooser implements DataSourceQueryChooser {
  private static PostGISCommonDriverPanel panel;
  private PostGISDataSource dataSource;
  private HashMap properties;
  
  JTextField serverField;
  JTextField portField;
  JTextField databaseField;
  JTextField tableField;
  JTextField usernameField;
  JPasswordField passwordField;
  
  /**
   * Creates a new query chooser.
   * @param dataSource DataSource object to be queried against.
   */
  public PostGISDataSourceQueryChooser(PostGISDataSource dataSource) {
    this.dataSource = dataSource;    
    if (panel == null) panel = new PostGISCommonDriverPanel();
  }
  
  /**
   * @see DataSourceQueryChooser#getComponent()
   */
  public Component getComponent() { 
  	return(panel); 
  }
  
  /**
   * @see DataSourceQueryChooser#getDataSourceQueries()
   */
  public abstract Collection getDataSourceQueries();
     
  /**
   * Checks wether the user input was valid or not.
   * @see DataSourceQueryChooser#isInputValid()
   */
  public boolean isInputValid() {
    if (panel.getServer().equals("")) return(false);
    if (panel.getPort().matches("")) return(false);
    if (panel.getDatabase().equals("")) return(false);
    if (panel.getTable().equals("")) return(false);
    if (panel.getUsername().equals("")) return(false);
    if (panel.getPassword().equals("")) return(false);
    return(true);
  }
  
  protected HashMap getProperties() {
    if (properties == null) properties = new HashMap();
    properties.put(PostGISDataSource.SERVER_KEY, panel.getServer());
    properties.put(PostGISDataSource.PORT_KEY, panel.getPort());
    properties.put(PostGISDataSource.DATABASE_KEY, panel.getDatabase());
    properties.put(PostGISDataSource.TABLE_KEY, panel.getTable());
    properties.put(PostGISDataSource.USERNAME_KEY, panel.getUsername());
    properties.put(PostGISDataSource.PASSWORD_KEY, panel.getPassword());
    return(properties);
  }
  
  protected PostGISDataSource getDataSource() {
    return(dataSource);   
  }
  
  public String toString() {
    return("PostGIS Table" );
  }
  
}