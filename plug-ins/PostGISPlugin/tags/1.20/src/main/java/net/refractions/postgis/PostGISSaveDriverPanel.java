/*
 * $Id: PostGISSaveDriverPanel.java,v 1.2 2004/07/12 16:19:43 bowens Exp $
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.vividsolutions.jump.workbench.ui.*;

/**
 * This class contains the ui for the save PostGIS plugin.
 * @author chodgson
 */
//JBuilder displays this component as a "Red Bean". There's a trick to
//displaying it -- see jcstest.AbstractDriverPanelProxy and
//http://www.visi.com/~gyles19/fom-serve/cache/97.html. [Jon Aquino]
// Last change UD, uwe dalluege, HCU Hamburg, 2006.03.24
public class PostGISSaveDriverPanel extends AbstractDriverPanel implements ActionListener {
	static final String INSERT_HELP_STRING = "Create the table if it doesn't already exist, then insert new geometries into it.";
	static final String UPDATE_HELP_STRING = "Insert new rows or update the existing rows in the table, based on the specified unique column (primary key). You must specify a unique column name which exists in both the feature schema and the database table.";
// UD, 2006.07.28
	static final String DELETE_HELP_STRING = 
		"Creates a new PostGIS-Table without CONSTRAINTSs and saves the layer."; 
		static final String OVERWRITE_HELP_STRING = 
		"\n\nOverwrites an existing PostGIS-Table and keeps the table CONSTRAINTs."; 
	
	ButtonGroup methodButtons;
	//JRadioButton insertButton;

	JRadioButton updateButton;
	
// UD, 2006.03.24 
	JRadioButton deleteButton;
	JLabel theLabel;
// UD, 2006.07.28	
	JRadioButton overwriteButton;

	
	JTextArea help;
	JTextField uniqueField;
	PostGISCommonDriverPanel commonPanel;
  OKCancelPanel okCancelPanel;
	
	public PostGISSaveDriverPanel() {
		try {
      jbInit();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		this.setLayout( new BorderLayout() );
		
//		JLabel theLabel;
		GridBagLayout gbLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JPanel topPanel = new JPanel();
		topPanel.setLayout( gbLayout );
		Insets insets = new Insets( 2, 2, 2, 2 );
		c.insets = insets;
		
		TitledBorder border = BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), "Select Save Method:" );
		border.setTitleJustification( TitledBorder.LEFT );
		topPanel.setBorder( border );
		
//		c.gridx = 0;
//		c.gridy = 0;
//		c.anchor = GridBagConstraints.WEST;
//		insertButton = new JRadioButton( "Insert" );
//		insertButton.setActionCommand( "insert" );
//		insertButton.addActionListener( this );
//		gbLayout.setConstraints( insertButton, c );
//		topPanel.add( insertButton );

//		c.gridy = 1;
		
// U.D. 2006.03.25	
		c.anchor = GridBagConstraints.WEST;
		
// U.D. 2006.03.24
		c.gridx = 0;
		c.gridy = 0;
		deleteButton = new JRadioButton ( "New Table" );
		deleteButton.setActionCommand( "delete" );
//		deleteButton.setSelected(true);
		deleteButton.addActionListener( this );
		gbLayout.setConstraints( deleteButton, c );
		topPanel.add( deleteButton );
// U.D. end
		
// UD, 2006.07.28
		c.gridx = 0;
		c.gridy = 1;
		overwriteButton = new JRadioButton ( "Overwrite" );
		overwriteButton.setActionCommand( "overwrite" );
		overwriteButton.setSelected(true);
		overwriteButton.addActionListener( this );
		gbLayout.setConstraints( overwriteButton, c );
		topPanel.add( overwriteButton );
// UD end		
		
		c.gridx = 0;
		c.gridy = 2;
		updateButton = new JRadioButton( "Insert" );
		updateButton.setActionCommand( "update" );
		updateButton.addActionListener( this );
		updateButton.setSelected(false);
		gbLayout.setConstraints( updateButton, c );
		topPanel.add( updateButton );
		

		
		methodButtons = new ButtonGroup();
		//methodButtons.add( insertButton );
		methodButtons.add( updateButton );
		
// U.D. 2006.03.24
		methodButtons.add( deleteButton );
// UD, 2006.07.28		
		methodButtons.add( overwriteButton );
		
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		theLabel = new JLabel( "Unique Column:" );
		gbLayout.setConstraints( theLabel, c );
		theLabel.setEnabled( false );
		topPanel.add( theLabel );
		
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		uniqueField = new JTextField( 15 );
		gbLayout.setConstraints( uniqueField, c );
		topPanel.add( uniqueField );
		uniqueField.setEnabled( false );
		
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 2;
		help = new JTextArea( 4, 40 );
		help.setEditable( false );
		help.setLineWrap( true );
		help.setWrapStyleWord( true );
		help.setBackground( topPanel.getBackground() );
		help.setFont( new Font( "Sans-Serif", Font.PLAIN, 12 ) );
		gbLayout.setConstraints( help, c );
		topPanel.add( help );
		
// U.D. Default overwrite-info		
		help.setText( OVERWRITE_HELP_STRING );
		
		this.add( topPanel, BorderLayout.NORTH );
    commonPanel = new PostGISCommonDriverPanel();
		this.add( commonPanel, BorderLayout.CENTER );

		okCancelPanel = new OKCancelPanel();
    //the following line is commented out because JUMP has already added 
    // an okay cancel control for us
		//this.add( okCancelPanel, BorderLayout.SOUTH );

// U.D.
//		setSaveMethod( PostGISDataSource.SAVE_METHOD_INSERT );
		
//		setSaveMethod( PostGISDataSource.SAVE_METHOD_DELETE );
			setSaveMethod( PostGISDataSource.SAVE_METHOD_OVERWRITE );
		
	}

	public String getValidationError() {
		//if( input is not valid ) {
		//	return fileNamePanel.getValidationError();
		//}

		return null;
	}

	public void addActionListener( ActionListener l ) {
		okCancelPanel.addActionListener( l );
	}

	public void removeActionListener( ActionListener l ) {
		okCancelPanel.removeActionListener( l );
	}

	public boolean wasOKPressed() {
		return okCancelPanel.wasOKPressed();
	}

	public void setCache( DriverPanelCache cache ) {
		super.setCache( cache );
		commonPanel.setCache( cache );
		if( cache.get( PostGISDataSource.SAVE_METHOD_KEY ) != null ) {
			setSaveMethod( (String)cache.get( PostGISDataSource.SAVE_METHOD_KEY ) );
		}
		if( cache.get( PostGISDataSource.UNIQUE_COLUMN_KEY ) != null ) {
			uniqueField.setText( (String)cache.get( PostGISDataSource.UNIQUE_COLUMN_KEY ) );
		} 
	}

	public DriverPanelCache getCache() {
		DriverPanelCache cache = super.getCache();
		commonPanel.putCache( cache );
		cache.put( PostGISDataSource.SAVE_METHOD_KEY, getSaveMethod() );
		cache.put( PostGISDataSource.UNIQUE_COLUMN_KEY, uniqueField.getText() );
		return cache;
	}

  private void setSaveMethod( String method ) {
		if( method.equals( PostGISDataSource.SAVE_METHOD_UPDATE ) ) {
			updateButton.doClick();
		}// else {
			//insertButton.doClick();
		//}
	}

	public String getSaveMethod() {
		if( methodButtons.isSelected( updateButton.getModel( ) ) ) {
			return PostGISDataSource.SAVE_METHOD_UPDATE;
		}
		else if ( methodButtons.isSelected( deleteButton.getModel( ) ) )
		{ // U.D. 
			return PostGISDataSource.SAVE_METHOD_DELETE;
		}
		else if ( methodButtons.isSelected( overwriteButton.getModel( ) ) )
		{ // Overwrite; UD, 2006.07.28 
			return PostGISDataSource.SAVE_METHOD_OVERWRITE;
		}
		return PostGISDataSource.SAVE_METHOD_INSERT;
	}
	
	public String getUniqueColumn() {
		return uniqueField.getText();
	}

	public String getServer() {
		return commonPanel.getServer();
	}

	public String getPort() {
		return commonPanel.getPort();
	}

	public String getDatabase() {
		return commonPanel.getDatabase();
	}

	public String getTable() {
		return commonPanel.getTable();
	}

	public String getUsername() {
		return commonPanel.getUsername();
	}

	public String getPassword() {
		return commonPanel.getPassword();
	}

	public String getWhere() {
		return commonPanel.getWhere();
	}

	public void actionPerformed( ActionEvent ae ) {
		String action = ae.getActionCommand();
		if( action.equals( "insert" ) ) {
			 theLabel.setEnabled( false );
			 uniqueField.setEnabled( false );
			 help.setText( INSERT_HELP_STRING );
		} else if( action.equals( "update" ) ) {
			theLabel.setEnabled( true );
			uniqueField.setEnabled( true );
			help.setText( UPDATE_HELP_STRING );
		}
		else if ( action.equals ( "delete" ) )
		{ // Create new Table; UD
			theLabel.setEnabled( false );
			uniqueField.setEnabled( false );
			help.setText( DELETE_HELP_STRING );
		}
		else if ( action.equals ( "overwrite" ) )
		{ // Overwrite; UD, 2006.07.28
			theLabel.setEnabled( false );
			uniqueField.setEnabled( false );
			help.setText( OVERWRITE_HELP_STRING );
		}
	}

}
