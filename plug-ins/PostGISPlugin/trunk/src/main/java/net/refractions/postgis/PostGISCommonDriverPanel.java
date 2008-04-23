/*
 * $Id: PostGISCommonDriverPanel.java,v 1.1.1.1 2004/01/06 00:13:15 pramsey Exp $
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

import java.awt.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.text.PlainDocument;

//import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.*;

//u.d.
import com.vividsolutions.jump.workbench.model.Layer;

/**
 * This class is the common ui between the PostGIS Load and Save
 * plugins.
 * @author chodgson
 */
//JBuilder displays this component as a "Red Bean". There's a trick to
//displaying it -- see jcstest.AbstractDriverPanelProxy and
//http://www.visi.com/~gyles19/fom-serve/cache/97.html. [Jon Aquino]
public class PostGISCommonDriverPanel extends JPanel {
  public static final String KEY = PostGISCommonDriverPanel.class.getName();
  
  public static String PG_DEFAULT_PORT = "5432";
  
  private static final String PKG_KEY = "net.refractions.postgis";
  
  private static final String SERVER = I18N.getString(KEY + ".server");
  private static final String PORT = I18N.getString(KEY + ".port");
  private static final String DATABASE = I18N.getString(KEY + ".database");
  private static final String TABLE = I18N.getString(KEY + ".table");
  private static final String USERNAME = I18N.getString(KEY + ".username");
  private static final String PASSWORD = I18N.getString(KEY + ".password");
  private static final String WHERE = I18N.getString(KEY + ".where");
  
  
  //These allow differnt objects to retain the same field values
  private static PlainDocument serverDoc = null;
  private static PlainDocument portDoc = null;
  private static PlainDocument databaseDoc = null;
  private static PlainDocument tableDoc = null;
  private static PlainDocument whereDoc = null;
  private static PlainDocument usernameDoc = null;
  private static PlainDocument passwordDoc = null;
  
// U.D. for error component
  public static JPanel driverPanel;
  
  JTextField serverField;
	JTextField portField;
	JTextField databaseField;
	JTextField tableField;
	JTextArea whereField;
	JTextField usernameField;
	JPasswordField passwordField;
	
  public PostGISCommonDriverPanel() {
		super();
    if (serverDoc == null) {
      serverDoc = new PlainDocument();
      portDoc = new PlainDocument();
      databaseDoc = new PlainDocument();
      tableDoc = new PlainDocument();
      whereDoc = new PlainDocument();
      usernameDoc = new PlainDocument();
      passwordDoc = new PlainDocument();
    }
    
		JLabel theLabel;
		GridBagLayout gbLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout( gbLayout );
		Insets labelInsets = new Insets( 2, 6, 2, 2 );
		Insets fieldsInsets = new Insets( 2, 2, 2, 6 );
		
		// first row of fields
		c.gridy = 0;
		c.gridx = 0;
		c.insets = labelInsets;
		c.anchor = GridBagConstraints.EAST;
		theLabel = new JLabel( SERVER+":" );
		gbLayout.setConstraints( theLabel, c );
		this.add( theLabel );
		c.gridx = 1;
		c.insets = fieldsInsets;
		c.anchor = GridBagConstraints.WEST;
		serverField = new JTextField( serverDoc, null, 15 );

		gbLayout.setConstraints( serverField, c );
		this.add( serverField );
		c.gridx = 2;
		c.insets = labelInsets;
		c.anchor = GridBagConstraints.EAST;
		theLabel = new JLabel( PORT+":" );
		gbLayout.setConstraints( theLabel, c );
		this.add( theLabel );
		c.gridx = 3;
		c.insets = fieldsInsets;
		c.anchor = GridBagConstraints.WEST;
		portField = new JTextField( portDoc, null, 5 );
		portField.setText(PG_DEFAULT_PORT);
		gbLayout.setConstraints( portField, c );
		this.add( portField );
		
		// second row of fields
		c.gridy = 1;
		c.gridx = 0;
		c.insets = labelInsets;
		c.anchor = GridBagConstraints.EAST;
		theLabel = new JLabel( DATABASE+":" );
		gbLayout.setConstraints( theLabel, c );
		this.add( theLabel );
		c.gridx = 1;
		c.insets = fieldsInsets;
		c.anchor = GridBagConstraints.WEST;
		databaseField = new JTextField( databaseDoc, null, 15 );

		gbLayout.setConstraints( databaseField, c );
		this.add( databaseField );
		c.gridx = 2;
		c.insets = labelInsets;
		c.anchor = GridBagConstraints.EAST;
		theLabel = new JLabel( TABLE+":" );
		gbLayout.setConstraints( theLabel, c );
		this.add( theLabel );
		c.gridx = 3;
		c.insets = fieldsInsets;
		c.anchor = GridBagConstraints.WEST;
		
		tableField = new JTextField( tableDoc, null, 15 );

		gbLayout.setConstraints( tableField, c );
		this.add( tableField );

		// third row of fields
		c.gridy = 2;
		c.gridx = 0;
		c.insets = labelInsets;
		c.anchor = GridBagConstraints.EAST;
		theLabel = new JLabel( USERNAME+":" );
		gbLayout.setConstraints( theLabel, c );
		this.add( theLabel );
		c.gridx = 1;
		c.insets = fieldsInsets;
		c.anchor = GridBagConstraints.WEST;
		usernameField = new JTextField( usernameDoc, null, 15 );

		gbLayout.setConstraints( usernameField, c );
		this.add( usernameField );
		c.gridx = 2;
		c.insets = labelInsets;
		c.anchor = GridBagConstraints.EAST;
		theLabel = new JLabel( PASSWORD+":" );
		gbLayout.setConstraints( theLabel, c );
		this.add( theLabel );
		c.gridx = 3;
		c.insets = fieldsInsets;
		c.anchor = GridBagConstraints.WEST;
		passwordField = new JPasswordField( passwordDoc, null, 15 );

		gbLayout.setConstraints( passwordField, c );
		this.add( passwordField );

		// forth row of fields
		c.gridy = 3;
		c.gridx = 0;
		c.insets = labelInsets;
		c.anchor = GridBagConstraints.EAST;
		theLabel = new JLabel( WHERE+":" );
		gbLayout.setConstraints( theLabel, c );
		this.add( theLabel );
		c.gridx = 1;
		c.gridwidth = 3;
		c.insets = fieldsInsets;
		c.anchor = GridBagConstraints.WEST;
		whereField = new JTextArea( whereDoc,"", 4, 40 );
		whereField.setText("");
		gbLayout.setConstraints( whereField, c );
		this.add( whereField );

		this.driverPanel = this;
		
	}

	public String getValidationError() {
		//if( input is not valid ) {
		//	return fileNamePanel.getValidationError();
		//}

		return null;
	}

	public void setCache( DriverPanelCache cache ) {

		if( cache.get( PostGISDataSource.SERVER_KEY ) != null ) {
			serverField.setText( (String)cache.get( PostGISDataSource.SERVER_KEY ) );
		}
		if( cache.get( PostGISDataSource.PORT_KEY ) != null ) {
			portField.setText( (String)cache.get( PostGISDataSource.PORT_KEY ) );
		}
		if( cache.get( PostGISDataSource.DATABASE_KEY ) != null ) {
			databaseField.setText( (String)cache.get( PostGISDataSource.DATABASE_KEY ) );
		}
		if( cache.get( PostGISDataSource.TABLE_KEY ) != null ) {
			tableField.setText( (String)cache.get( PostGISDataSource.TABLE_KEY ) );
		}
		if( cache.get( PostGISDataSource.WHERE_KEY ) != null ) {
			whereField.setText( (String)cache.get( PostGISDataSource.WHERE_KEY ) );
		}
		if( cache.get( PostGISDataSource.USERNAME_KEY ) != null ) {
			usernameField.setText( (String)cache.get( PostGISDataSource.USERNAME_KEY ) );
		}
		if( cache.get( PostGISDataSource.PASSWORD_KEY ) != null ) {
			passwordField.setText( (String)cache.get( PostGISDataSource.PASSWORD_KEY ) );
		}
	}
	
 	public void putCache( DriverPanelCache cache ) {
		cache.put( PostGISDataSource.SERVER_KEY, serverField.getText() );
		cache.put( PostGISDataSource.PORT_KEY, portField.getText() );
		cache.put( PostGISDataSource.DATABASE_KEY, databaseField.getText() );
		cache.put( PostGISDataSource.TABLE_KEY, tableField.getText() );
		cache.put( PostGISDataSource.WHERE_KEY, whereField.getText() );
		cache.put( PostGISDataSource.USERNAME_KEY, usernameField.getText() );
		cache.put( PostGISDataSource.PASSWORD_KEY, passwordField.getText() );
	}

	public String getServer() {
		return serverField.getText();
	}

	public String getPort() {
		return portField.getText();
	}

	public String getDatabase() {
		return databaseField.getText();
	}

	public String getTable() {
// UD. uwe.dalluege@rzcn.haw-hamburg.de
// toLowerCase ( ) because AddGeometryColumn does not work with uppercase?
		return tableField.getText().toLowerCase ( ) ;
	}

	public String getWhere() {
		return whereField.getText();
	}

	public String getUsername() {
		return usernameField.getText();
	}

	public String getPassword() {
		return passwordField.getText();
	}

}
