/*
 * $Id: PostGISConnection.java,v 1.4 2004/07/12 17:51:13 bowens Exp $
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
// Changed: Uwe Dalluege (UD), 15.07.2005, uwe.dalluege@rzcn.haw-hamburg.de
// 2006.06.21, ud.
// 2006.11.06, UD
package net.refractions.postgis;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.task.TaskMonitor;


import java.io.Reader;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import java.math.*;


/**
 * This class represents the actual connection of a PostGIS data source. 
 */
public class PostGISConnection implements Connection {
  Map properties;
  String server;
  String port;
  String database;
  String table;
  String username;
  String password;
  String uniqueCol;
  String method;
//U.D., uwe.dalluege@rzcn.haw-hamburg.de
  int srid_tmp = 0;
  boolean errGeomEmpty = false;
  
  /**
   * Creates an unconfigured connection.
   */
  public PostGISConnection() {
  	
    this(null);
  }
  
  /**
   * Creates a configured connection. See {@link PostGISDataSource} for 
   * property keys.
   */
  public PostGISConnection(Map properties) {
    this.properties = properties;        
  }
  
  /**
   * Configures the connection. See {@link PostGISDataSource} for property
   * keys.
   */
  public void setProperties(Map properties) {
    this.properties = properties;
  }
  
  /**
   * Executes a query against a connection. The result of the query is returned as 
   * a FeatureCollection.
   * @param query SQL statement.
   */
  public FeatureCollection executeQuery(String query) {
    FeatureDataset dataSet;

    String geoColName = null;
    readProperties();
    java.sql.Connection conn = connect();
    
    try {
      Statement st = conn.createStatement();
      
      ResultSet rs = st.executeQuery(query + " LIMIT 0");
      ResultSetMetaData meta = rs.getMetaData();
      
      boolean geomCol = false;
      FeatureSchema schema = new FeatureSchema();
      StringBuffer sql = new StringBuffer( "SELECT" );
      int num_cols = meta.getColumnCount();
      for( int col_idx = 1; col_idx <= num_cols; col_idx++ ) {
        String attr_name = meta.getColumnName ( col_idx );
// UD, uwe.dalluege@rzcn.haw-hamburg.de
// This do not work? What for?
//        String table_name = meta.getTableName ( col_idx );
      
        AttributeType attr_type = AttributeType.STRING;
      
        // determine the attribute type based on the sql type
        if( meta.getColumnTypeName( col_idx ).equalsIgnoreCase( "geometry") && !geomCol ) {
          // found the first geometry column (any extra geometry columns are treated as strings)
          geomCol = true;
          attr_type = AttributeType.GEOMETRY;
// UD, uwe.dalluege@rzcn.haw-hamburg.de          
          geoColName = attr_name;
// UD end          
          sql.append( " asText(" + attr_name + ") AS " + attr_name + "," );
        } 
        else {
          int sql_type = meta.getColumnType( col_idx );
//System.out.println ( "PGC 116 sql_type: " + sql_type + " " ); 
          switch( sql_type ) {
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
              attr_type = AttributeType.INTEGER;
              sql.append( " " + attr_name + "," );
              break;
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
// UD. Need also numeric! 21.07.2005
            case Types.NUMERIC:
              attr_type = AttributeType.DOUBLE;
              sql.append( " " + attr_name + "," );
              break;
            default:
              attr_type = AttributeType.STRING;
              sql.append( " " + attr_name + "," );
          }
        }
        schema.addAttribute( attr_name, attr_type );
      }
           
      rs.close();
    
// UD, uwe.dalluege@rzcn.haw-hamburg.de 
// Read SRID
      query = "SELECT SRID ( " + geoColName + " ) FROM " + table;
      rs = st.executeQuery ( query + " LIMIT 1" );
      while ( rs.next ( ) )
      {
      	srid_tmp = rs.getInt ( 1 );
      }
      
      rs.close ( ); 
// UD end
      
      if (!geomCol) {
        st.close();
        conn.close();
        throw new IllegalStateException("The table you have selected does not contain any geometric data.");  
      }
      
      sql.deleteCharAt( sql.lastIndexOf( "," ) );
      sql.append( " FROM " + table);
      
      st.execute( "BEGIN" );
      String s = "DECLARE my_cursor CURSOR FOR " + sql.toString();
      
      st.execute( "DECLARE my_cursor CURSOR FOR " + sql.toString() );
      rs = st.executeQuery( "FETCH FORWARD ALL IN my_cursor" );
      dataSet = new FeatureDataset( schema ) ;
      
// UD, uwe.dalluege@rzcn.haw-hamburg.de  
// change 0 to srid_tmp
      GeometryFactory factory = 
      	new GeometryFactory( new PrecisionModel(), srid_tmp );
      
      WKTReader wktReader = new WKTReader( factory );
      while( rs.next() ) {
        Feature f = new BasicFeature( schema );
        
        
        for( int attr_idx = 0; attr_idx < schema.getAttributeCount(); attr_idx++ ) {
          AttributeType attr_type = schema.getAttributeType( attr_idx );
          if( attr_type.equals( AttributeType.GEOMETRY ) ) {
            Reader wkt = rs.getCharacterStream( schema.getAttributeName( attr_idx ) ); 
            Geometry geom;
            if( wkt == null ) {
              geom = new GeometryCollection( null, factory);
            } 
            else {
              geom = wktReader.read( wkt );
            } 
            f.setAttribute( attr_idx, geom );
          } 
          else if( attr_type.equals( AttributeType.INTEGER ) ) {
// u.d. to get the null values
          	Object ob = rs.getObject( schema.getAttributeName( attr_idx ) );
//System.out.println ( "PGC 197 int Ob ob: " + ob.toString ( ) ); 
//System.out.println ( "PGC 198 int Ob ob: " + ob.getClass ( ).getName ( ) );
						

          	if ( ob == null )
          	{ f.setAttribute( attr_idx,  null  ) ; }
          	
          	else 
          	{ 
          		String iType = ob.getClass ( ).getName ( );
          		if ( iType.indexOf ( "Integer" ) >= 0 )
          		{
          			Integer iObj = ( Integer ) ob;
          			f.setAttribute( attr_idx, new Integer ( iObj.intValue( ) ) );
          		}
          		else
          		{ // Long
           			Long iObj = ( Long ) ob;
//System.out.println ( "PGC 215 Long: " + iObj.intValue( ) );
          			f.setAttribute( attr_idx, new Long ( iObj.intValue( ) ) );
          		}
          	}
//System.out.println ( " PGCon 194 lesen? " + rs.getObject( schema.getAttributeName( attr_idx ) ) );
// getInt
//            f.setAttribute( attr_idx, new Integer( rs.getInt( schema.getAttributeName( attr_idx ) ) ) );
          } 
          else if( attr_type.equals( AttributeType.DOUBLE ) ) {
 // u.d. to get the null values
          	Object ob = rs.getObject( schema.getAttributeName( attr_idx ) );
          	if ( ob == null )
          	{ f.setAttribute( attr_idx,  null  ) ; }
          	else 
          	{ 
          		String dType = ob.getClass ( ).getName ( );
          		if ( dType.indexOf ( "Double" ) >= 0 )
          		{
          			Double dObj = ( Double ) ob;
          			f.setAttribute( attr_idx, new Double( dObj.doubleValue( ) ) );
          		}
// UD, 06.11.06, also need Float Datatype
          		else if ( dType.indexOf ( "Float" ) >= 0 )
          		{
          			Float dObj = ( Float ) ob;
          			f.setAttribute( attr_idx, new Double( dObj.doubleValue( ) ) );
          		}
          		else
          		{ // BigDecimal
          			
          			BigDecimal dObj = ( BigDecimal ) ob;
//System.out.println ( "PGC 240 Long: " + dObj.doubleValue( ) );
          			f.setAttribute
          				( attr_idx, new BigDecimal ( dObj.doubleValue( ) ) );
          		}
          	} 
//            f.setAttribute( attr_idx, new Double( rs.getDouble( schema.getAttributeName( attr_idx ) ) ) );
          } 
          else if( attr_type.equals( AttributeType.STRING ) ) {
            f.setAttribute( attr_idx, rs.getString( schema.getAttributeName( attr_idx ) ) );
          } 
        }
// UD, uwe.dalluege@rzcn.haw-hamburg.de  
        f.getGeometry().setSRID ( srid_tmp );
        dataSet.add( f );
      }
      
      st.execute( "CLOSE my_cursor" );
      st.execute( "END" );
      st.close();
      conn.close();
    }
    catch(Exception e) {
      if (PostGISPlugIn.DEBUG) e.printStackTrace();
      throw new IllegalStateException(e.getMessage());
    }
    
    return(dataSet);
  }
  
  /**
   * Since any exceptions will cause the query to fail, this function 
   * simply calls executeQuery(String query) and does not ignore any 
   * exceptions.
   */
  public FeatureCollection executeQuery(String query, List exceptions) {
    return(executeQuery(query));
  }
  
  /**
   * Executes an update against the connection.
   * @param query This parameter is ignored, the connection determines everything 
   * it needs to know from setProperties(HashMap properties) and collection.
   * @param collection The updated features.
   */
  public void executeUpdate(String query, FeatureCollection collection) {
  
// U.D. for exist unique column
    boolean unColExists = false;
    
  	if (collection.isEmpty()) 
      throw new IllegalStateException("No data to write, empty Feature Collection");
    
    int SRID = ((Feature)collection.iterator().next()).getGeometry().getSRID();
    
    
    // get the feature schema
    FeatureSchema schema = collection.getFeatureSchema();
    HashSet schemaCols = new HashSet( schema.getAttributeCount() );
    for( int i = 0; i < schema.getAttributeCount(); i++ ) {
      String name = schema.getAttributeName( i );
      if( schemaCols.contains( name ) ) {
        throw new UnsupportedOperationException
        ( "The FeatureSchema contains duplicate attribute names; you must remove duplicate attribute names before saving to a PostGIS table." );
      }
// toLowerCase, ud, 2006.07.29
      schemaCols.add( name.toLowerCase ( ) );
    }
    
    java.sql.Connection conn = null;
    Statement stmt = null;
    
    HashSet cols = null;
    StringBuffer sqlBuf = null;
    String sql = null;
    
    //connect
    readProperties();  
    conn = connect();
    
    // determine if the table already exists or not
    boolean table_exists = false;
    

    
    try {
      stmt = conn.createStatement();

      ResultSet rs = stmt.executeQuery
      	( "SELECT * FROM " + table + " LIMIT 0" );
      ResultSetMetaData meta = rs.getMetaData();
      int num_cols = meta.getColumnCount();
      
      cols = new HashSet( num_cols );
      for( int col_idx = 1; col_idx <= num_cols; col_idx++ ) 
      { // toLowerCase, ud, 2006.07.29
        cols.add( meta.getColumnName( col_idx ).toLowerCase( ));
      }
      rs.close();
      table_exists = true;
    } 
    catch( SQLException sqle ) {
      table_exists = false;
      //if( method == PostGISDataSource.SAVE_METHOD_UPDATE ) {
      //  throw new IllegalStateException( "Save method is set to UPDATE and table does not exist; cannot update a non-existant table" );
      //}       
    }
    
    // begin the transaction
    try {
      conn.setAutoCommit( false ); // don't know why transactions don't work...
      String geomCol = null;
      
//System.out.println 
//( "PostGIS Connection method: " + method + " table_exists: " + table_exists ); 


// U.D. if DELETE_METHODE then delete
			if ( method == PostGISDataSource.SAVE_METHOD_DELETE )
			{
				if ( table_exists )
				{
//System.out.println 
//( "PostGIS Connection tabelle: " + table + " löschen!"); 
					int opt = JOptionPane.showConfirmDialog 
						( PostGISCommonDriverPanel.driverPanel, 
								"OverWrite existing table  " + table + " ?", 
								"OverWrite " + table + " ?", 
								JOptionPane.YES_NO_OPTION );
					
//System.out.println ( "PGC opt = " + opt + " " + JOptionPane.NO_OPTION ); 					
					if ( opt == JOptionPane.NO_OPTION ) return;
					
					String sqlDrop = "SELECT DropGeometryTable ( '" + table + "' )";
					try 
					{
						stmt.execute( sqlDrop );
						table_exists = false;
					} 
					catch( SQLException sqle ) 
					{
/*
System.out.println 
( "PostGIS Connection sql: " + sqle ); 
System.out.println 
( "PostGIS Connection sqlDrop: " + sqlDrop ); 
*/	
System.out.println 
( "PostGIS Connection tabelle: " + table + " nicht gelöscht!"); 							
					}
				}
			}
// U.D. delete end
// --------------------------------------------------------------------------
// Overwrite
// UD, if OVERWRITE_METHODE then delete only values, not the table
			if ( method == PostGISDataSource.SAVE_METHOD_OVERWRITE )
			{
				if ( table_exists )
				{
//System.out.println 
//( "PostGIS Connection tabelle: " + table + " löschen!"); 
					int opt = JOptionPane.showConfirmDialog 
						( PostGISCommonDriverPanel.driverPanel, 
								"OverWrite existing table  " + table + " ?", 
								"OverWrite " + table + " ?", 
								JOptionPane.YES_NO_OPTION );
					
//System.out.println ( "PGC opt = " + opt + " " + JOptionPane.NO_OPTION ); 					
					if ( opt == JOptionPane.NO_OPTION ) return;
					
					String sqlDrop = "DELETE FROM " + table ;
//System.out.println( "PostGISConnection: sqlDrop: " + sqlDrop );
					try 
					{
						stmt.execute( sqlDrop );
						table_exists = false; // need for other code; normaly true
					} 
					catch( SQLException sqle ) 
					{
/*
System.out.println 
( "PostGIS Connection sql: " + sqle ); 
System.out.println 
( "PostGIS Connection sqlDrop: " + sqlDrop ); 
*/	
System.out.println 
( "PostGISConnection: Daten aus Tabelle: " + table + " nicht gelöscht!"); 							
					}
				}
				else
				{ // UD, If not exist, it is like SAVE_METHOD_DELETE					
					 method = PostGISDataSource.SAVE_METHOD_DELETE;
				}
			}
// U.D. overwrite end			
// --------------------------------------------------------------------------
     
      if( !table_exists ) { 
        // build the create table statement
        sqlBuf = new StringBuffer( "CREATE TABLE " + table + " (" );
        int num_attrs = schema.getAttributeCount();
//UD, 2006.07.28, uwe.dalluege@rzcn.haw-hamburg.de 
//System.out.println ("PostGISConnection: method = " + method );
				if ( num_attrs <= 1 && 
						(
						 method != PostGISDataSource.SAVE_METHOD_DELETE 
						 && method != PostGISDataSource.SAVE_METHOD_OVERWRITE ) 
						 )
				{
System.out.println( " Error: Please insert FeatureColumn!" );
//					return;
					throw new Exception ( "Error: Please insert FeatureColumn!");
//					return;
				}
// UD End
        cols = new HashSet( num_attrs );
//        unColExists = false;
        
        for( int i = 0; i < num_attrs; i++ ) {
          String name = schema.getAttributeName( i );
     
        
// U.D.
          
//System.out.println ( "PGConn: " + name + "  " + uniqueCol + " " + unColExists );
          if ( name.trim ( ).equalsIgnoreCase( uniqueCol.trim( )  ) )
          {
          	unColExists = true;
//System.out.println ( "PGConn: " + name + "  " + uniqueCol + " " + unColExists );          	
          }
          
//System.out.println ( "PGC 422 addCol name: " + name ); 
// toLowerCase, ud, 2006.07.29
          cols.add( name.trim( ).toLowerCase ( ) );
          AttributeType type = schema.getAttributeType( i );
          if( type.equals( AttributeType.INTEGER ) ) {
            sqlBuf.append( " " + name + " INT," );
          } 
          else if( type.equals( AttributeType.DOUBLE ) ) {
            sqlBuf.append( " " + name + " FLOAT8," );
          } 
          else if( type.equals( AttributeType.GEOMETRY ) ) {
            // do not add to the query string

            if( geomCol == null ) {
              // save the name of the geometry column for later
              geomCol = name;
// UD, uwe.dalluege@rzcn.haw-hamburg.de  
// not used!
// 						sqlBuf.append( " " + name.toLowerCase ( ) + " geometry," );
            } 
          } 
          else { // U.D. from varchar (255) to text-obj
            sqlBuf.append( " " + name + " TEXT," );
          }
        }
// u.d.
        int lastIx = sqlBuf.lastIndexOf( "," );
        if ( lastIx >= 0 )
//        	sqlBuf.deleteCharAt( sqlBuf.lastIndexOf( "," ) );
           	sqlBuf.deleteCharAt( lastIx );
// ud. 21.06.2006 PostgreSQL 8.1 has no default OIDS
        sqlBuf.append( " ) WITH OIDS" );
        sql = sqlBuf.toString();  
//System.out.println ( "PGConnection sql: " + sql + " uc: " + unColExists ); 
        // create the table
				if ( !unColExists && 
						(
						method != PostGISDataSource.SAVE_METHOD_DELETE  
						&& method != PostGISDataSource.SAVE_METHOD_OVERWRITE)
						)
				{
					JOptionPane.showMessageDialog 
						( PostGISCommonDriverPanel.driverPanel, 
							"Unique Column  " + uniqueCol + "  does not existx!",
							"Table will not saved!", JOptionPane.ERROR_MESSAGE );					
					return;
					
				}


        try {
// UD, crazy code! 2006.07.28
          if ( method != PostGISDataSource.SAVE_METHOD_OVERWRITE )
          	stmt.executeUpdate( sql );
        } 
        catch( SQLException sqle ) {
          if (PostGISPlugIn.DEBUG) { 
            System.out.println(sql);
            sqle.printStackTrace();
          }
          throw new Exception( "Create table statement failed: " + 
          		sqle.toString() + "\n" + sql );
        }
      
        // add the geometry column
// UD, 
// geomCol.toLowerCase ( ) because PostGIS does not support GEOMETRY (uppercase)
        sql = "SELECT AddGeometryColumn( '" + database + "', '" + table + 
        "', '" + geomCol.toLowerCase ( ) + "', " + SRID + ", 'GEOMETRY', 2 )";
        
        try {
//System.out.println ( " PGC 443 sql: " + sql ); 
// UD, a little crazy! 2006.07.28
          if ( method != PostGISDataSource.SAVE_METHOD_OVERWRITE )
          	stmt.execute( sql );
        } 
        catch( SQLException sqle ) {
          if (PostGISPlugIn.DEBUG) { 
            System.out.println(sql);
            sqle.printStackTrace();
          } 
          throw new Exception( "AddGeometryColumn statement failed: " + sqle.toString() + "\n" + sql );
        }
      }
    
      /** table creation complete, now move onto the update */
      
      String val = null;
      String uniqueVal = null;
      
      Iterator it = collection.iterator();
      while (it.hasNext()) 
      {
          uniqueVal = null;
          Feature f = (Feature) it.next();
          
// u.d. exist column-names  
          boolean errCol = false;
          boolean errUni = true;
          String errColName = null;
          for (int ii = 0; ii < schema.getAttributeCount(); ii++) 
          {
              String attrName = schema.getAttributeName(ii);
//System.out.println ( "PGC 468: attrName: "  + 
//		attrName + " colsCont: " + cols.contains(attrName)); 

						Object [ ] ocols = cols.toArray ( );
						for ( int t = 0; t < ocols.length; t++ )
						{
//System.out.println ( "PGC 584: ocols " + ocols [ t ] ); 							
						}



// toLowerCase, ud, 2006.07.29
						if ( !cols.contains(attrName.toLowerCase ( ) ) )
							{
								errCol = true;
								errColName = attrName;
							}
						if ( attrName.equalsIgnoreCase(uniqueCol) ) errUni = false;
          }
// U.D. DB schema and feature schema not the same          
          if ( errCol && 
          		( method != PostGISDataSource.SAVE_METHOD_DELETE &&
          			method != PostGISDataSource.SAVE_METHOD_OVERWRITE
          				) )
          {
						JOptionPane.showMessageDialog 
							( PostGISCommonDriverPanel.driverPanel, 
								"Feature and TableSchema are not equal ( " + 
								errColName + " )!\n" +
								"Please use only lowercase colum names!",
								"Table will not saved!", JOptionPane.ERROR_MESSAGE );
						stmt.close();
						conn.close();    
          	return;
          }
// u.d. unique column not present
          if ( errUni && 
          		( method != PostGISDataSource.SAVE_METHOD_DELETE &&
          			method != PostGISDataSource.SAVE_METHOD_OVERWRITE 	
          		) )
          {
						JOptionPane.showMessageDialog 
							( PostGISCommonDriverPanel.driverPanel, 
								"Unique Column  " + uniqueCol + "  does not existY!",
								"Table will not saved!", JOptionPane.ERROR_MESSAGE );
						stmt.close();
						conn.close();        
          	return;
          }          
          

          ////////////////////////////////////////////////////////////////
          //If the value of uniqueCol exists we make that the uniqueVal in
          //an UPDATE or INSERT, if does not exists uniqueCol must be
          //AutoIncrement and the value is set by the DBMS in an INSERT.
          //Otherwise is an ERROR.
//System.out.println ( "PGC 614 count: " + schema.getAttributeCount()); 
          for (int i = 0; i < schema.getAttributeCount(); i++) 
          {
              String attrName = schema.getAttributeName(i);
//System.out.println ( "PGC 618 attrName " + attrName );
// toLowerCase, ud, 2006.07.29
              if (cols.contains(attrName.toLowerCase( ))) 
              {
//                  if (attrName.equalsIgnoreCase(uniqueCol)) 
//System.out.println ( "PGC 622 count: " ); 
              	if ( method == PostGISDataSource.SAVE_METHOD_DELETE ||
              			 method == PostGISDataSource.SAVE_METHOD_OVERWRITE )
                {
//System.out.println ( "PGC 488 stmt: " + stmt ); 
									insertFeatureNew (f, stmt, cols, schema, SRID );
									break;
                }
                else if (attrName.equalsIgnoreCase(uniqueCol))
                {
                      //Is the value in feature's uniqueCol null?
                      if (f.getAttribute(attrName) == null) {
                          ResultSet rs =
                              stmt.executeQuery(
                                  "SELECT * FROM " + table + " LIMIT 0");
                          ResultSetMetaData meta = rs.getMetaData();
                          //if (meta
                          //    .isAutoIncrement(rs.findColumn(name))) {
                          //The uniqueCol column has no value in the
                          //feature, but is an autoincremental column
                          //so the DBMS assigns this value.
// UD, put SRID in the variable-list
//                          insertFeature(f, false, stmt, cols, schema );
                        insertFeature(f, false, stmt, cols, schema, SRID );
                          break;
                      }
                      else	//There a uniqueVal in feature's uniqueCol?
                      //if (f.getString(i).length() > 0) 
                      {
                          AttributeType attrType = schema.getAttributeType(i);
                          if (attrType.equals(AttributeType.INTEGER)) {
                              val = "" + f.getInteger(i);
                          } else if (attrType.equals(AttributeType.DOUBLE)) {
                              val = "" + f.getDouble(i);
                          } else if (
                              attrType.equals(AttributeType.GEOMETRY)) {
                              val =
                                  "GeometryFromText( '"
                                      + f.getGeometry().toText()
                                      + "', "
// UD, uwe.dalluege@rzcn.haw-hamburg.de
// this does not work!
//                                      + f.getGeometry().getSRID()
// this works!
                                      + SRID
                                      + ")";
                          } else {
                          	
// old     ud.              val = "'" + f.getString(i) + "'";
                            val = "'" + 
                              f.getString(i).replaceAll ( "'", "''" ) + "'";
                          }
                          
// U.D. if delete methode no need to check val
                          ResultSet rs;
                          if ( method == PostGISDataSource.SAVE_METHOD_DELETE ||
                          		method == PostGISDataSource.SAVE_METHOD_OVERWRITE)
                          { // u.d. no resultset if delete methode
                          	rs = null;
                          }
                          else
                          {
                          	rs =
                              stmt.executeQuery(
                                  "SELECT * FROM "
                                      + table
                                      + " WHERE "
                                      + attrName
                                      + "="
                                      + val);
                      		}
                     
                          //Is there a row with that uniqueVal in the DBMS?
                          // U.D. only when not delete methode!
                          if ( rs != null )
                          {
                          	if ( rs.next( ) )
                          	{
                              	//Yes. That feature exists in DBMS.
                              	//We update the data of that feature
// UD, extend SRID
//                          		updateFeature(f, stmt, cols, schema );
                          		updateFeature(f, stmt, cols, schema, SRID );
                          		break;
                          	} 
                          	else 
                          	{
                              	//No. That feature does not exist in DBMS.
                              	//We must create it.
// UD, extend SRID
//                              insertFeature(f, true, stmt, cols, schema );
                        	  insertFeature(f, true, stmt, cols, schema, SRID );                        
                              	break;
                          	} // end else
                          } // if != null
                          else
                          { // u.d. if delete methode only insert
                          	
                          	insertFeature(f, true, stmt, cols, schema, SRID );                        
                            break;
                          }
                          
                          
                          
                          
                      }
                  } 
                  
              }
          }
      }
      
      // end the transaction
      conn.commit(); // don't know why transactions don't work...
      
      stmt.close();
      conn.close();        
    } 
    catch( Exception e ) {
      try {
        conn.rollback();
        stmt.close();
        conn.close();  
      }
      catch(SQLException sqle) {
        if (PostGISPlugIn.DEBUG) sqle.printStackTrace();     
      }
      
      if (PostGISPlugIn.DEBUG) e.printStackTrace();
      throw new IllegalStateException(e.getMessage());
    }
// ud. emty geometry not saved  
    if ( errGeomEmpty )
    {
 			JOptionPane.showMessageDialog 
						( PostGISCommonDriverPanel.driverPanel, 
							"The Layer had empty Geometries!",
							"Empty Geometries are not saved!", 
							JOptionPane.ERROR_MESSAGE );	
 			errGeomEmpty = false;
    }
    
  }
  
  /**
   *Update a feature existent in DBMS identified by uniqueCol/uniqueVal.
   */
  private void updateFeature
  	(Feature f, Statement stmt, HashSet cols, FeatureSchema schema, int SRID ) 
  		throws Exception {
      StringBuffer sqlBuf = new StringBuffer("UPDATE " + table + " SET ");
      String val = null;
      String uniqueVal = null;
//System.out.println ( "PGC 631 updateFeature" ); 
      
      for (int i = 0; i < schema.getAttributeCount(); i++) 
      {
          String attrName = schema.getAttributeName(i);
// toLowerCase, 2006.07.29
          if (cols.contains(attrName.toLowerCase( ))) 
          {
              AttributeType type = schema.getAttributeType(i);
              if (type.equals(AttributeType.INTEGER)) {
// u.d. find null
              	if ( f.getAttribute ( i ) == null )
              	{ val = null; }
              	else 
              	{
// old u.d.       val = "" + f.getInteger(i);
              		
									Object ob = f.getAttribute ( i );
									String iType = ob.getClass ( ).getName( );
										
									if ( iType.indexOf ( "Integer" ) >= 0  )
									{
										val = "" + f.getInteger(i);
									}
									else
									{ // Long
										Long iObj = ( Long ) ob;
//System.out.println ( "PGC 790 Long: " + iObj.longValue( ) );
											val = "" +  iObj.longValue( );
									}
              	}
              	
              } else if (type.equals(AttributeType.DOUBLE)) {
// u.d. find null
              	if ( f.getAttribute ( i ) == null )
              	{ val = null; }              	
              	else 
              	{ 
// old u.d.       val = "" + f.getDouble(i);
									Object ob = f.getAttribute ( i );
									String dType = ob.getClass ( ).getName( );
										
									if ( dType.indexOf ( "Double" ) >= 0  )
									{ 										
										val = "" + f.getDouble(i); 
									}
									else
									{ // Big Decimal
										BigDecimal dObj = ( BigDecimal ) ob;
										val = "" + dObj.doubleValue ( ); 
									} 										              		
              	}
                  
              } else if (type.equals(AttributeType.GEOMETRY)) {
                  val =
                      "GeometryFromText( '"
                          + f.getGeometry().toText()
                          + "', "
// UD, uwe.dalluege@rzcn.haw-hamburg.de  
// .getSRID ( ) does not work! Therefor SRID!        
//                        + f.getGeometry().getSRID()
                          + SRID
                          + ")";
              } else {
//                  val = "'" + f.getString(i) + "'";
              	val = "'" + f.getString(i).replaceAll ( "'", "''" ) + "'";
              }
              if (attrName.equals(uniqueCol)) {
                  uniqueVal = val;
              } else {
                  sqlBuf.append(" " + attrName + "=" + val + ",");
              }
          }
      }
      sqlBuf.deleteCharAt(sqlBuf.lastIndexOf(","));
      sqlBuf.append(" WHERE " + uniqueCol + " = " + uniqueVal);
      String sql = sqlBuf.toString();

      try {
          stmt.executeUpdate(sql);
      } catch (SQLException sqle) {
          throw new Exception(
              "Update statement failed: " + sqle.toString() + "\n" + sql);
      }
  }
// --------------------------------------------------------- insertFeature ( ) 
  /**
   * Insert feature in table. If there is an uniqueVal this value is used
   * to make the new row's identifier. If there is not an uniqueVal in feature
   * this value is assigned by the DBMS through autoincrement.
   */
  private void insertFeature
  	(Feature f, boolean existsUnique, Statement stmt, HashSet cols, 
  			FeatureSchema schema, int SRID )
      throws Exception {
      StringBuffer sqlBuf = new StringBuffer("INSERT INTO " + table + " (");
      String sql = null;
//System.out.println ( "PGC 631 insertFeature" );       
//System.out.println ( "PGConn existsUnique: " + existsUnique ); 
      
      for (int i = 0; i < schema.getAttributeCount(); i++) {
          String name = schema.getAttributeName(i);
// toLowerCase, ud, 2006.07.29
          if (cols.contains(name.toLowerCase( )) && 
          		(!name.equals(uniqueCol) || existsUnique)) {
              sqlBuf.append(" " + name.toLowerCase ( ) + ",");
          }
      }
      sqlBuf.deleteCharAt(sqlBuf.lastIndexOf(","));
      sqlBuf.append(" ) VALUES (");
      String insertQueryHead = sqlBuf.toString();
      sqlBuf = new StringBuffer(insertQueryHead);
      for (int j = 0; j < schema.getAttributeCount(); j++) {
          String name = schema.getAttributeName(j);
// toLowerCase, ud, 2006.07.29
          if (cols.contains(name.toLowerCase( )) && 
          		(!name.equals(uniqueCol) || existsUnique)) {
              AttributeType type = schema.getAttributeType(j);
              
              if (type.equals(AttributeType.INTEGER)) {
//System.out.println ( "PGCon: f.a: " + f.getAttribute ( j ) );
// u.d.  if the field has no value         
									if ( f.getAttribute ( j ) == null )
									{ sqlBuf.append(" " + null + ","); }
									else 
									{
										Object ob = f.getAttribute ( j );
										String iType = ob.getClass ( ).getName( );
										
//	old u.d.						sqlBuf.append(" " + f.getInteger(j) + ",");
										if ( iType.indexOf ( "Integer" ) >= 0  )
										{
//											Integer iObj = ( Integer ) ob;

//System.out.println ( "PGC 782 iObj.class: " + iObj.getClass().getName()); 
//System.out.println ( "PGC 783 Integer: " + iObj.intValue( ) );
//System.out.println ( "PGC 784 f.getInt: " +  f.getInteger(j) );
											sqlBuf.append(" " + f.getInteger(j) + ",");
										}
										else
										{ // Long
											Long iObj = ( Long ) ob;
//System.out.println ( "PGC 790 Long: " + iObj.longValue( ) );
											sqlBuf.append(" " + iObj.longValue( ) + ",");
										}
										
									}
									
              } else if (type.equals(AttributeType.DOUBLE)) {
 									if ( f.getAttribute ( j ) == null )
									{ sqlBuf.append(" " + null + ","); }
 									else 
 									{ 
// old u.d.					sqlBuf.append(" " + f.getDouble(j) + ","); 
										Object ob = f.getAttribute ( j );
										String dType = ob.getClass ( ).getName( );
										
										if ( dType.indexOf ( "Double" ) >= 0  )
										{ 										
											sqlBuf.append(" " + f.getDouble(j) + ","); 
										}
										else
										{ // Big Decimal
											BigDecimal dObj = ( BigDecimal ) ob;
											sqlBuf.append(" " + dObj.doubleValue ( ) + ","); 
										} 										
 									}
 									
              } else if (type.equals(AttributeType.GEOMETRY)) {
                  sqlBuf.append(
                      " GeometryFromText( '"
                          + f.getGeometry().toText()
                          + "', "
// UD, uwe.dalluege@rzcn.haw-hamburg.de  
// .getSRID ( ) does not work! Therefor SRID!        
//                          + f.getGeometry().getSRID()
                          + SRID
                          + "),");
              } else {
//old             sqlBuf.append(" '" + f.getString(j) + "',");
// ud. for apostrophe in text
                  String fString = f.getString(j);
//            	  sqlBuf.append(" '" + f.getString(j) + "',");
            	  sqlBuf.append(" '" + fString.replaceAll( "'", "''" ) + "',");
              }
          }
      }
      sqlBuf.deleteCharAt(sqlBuf.lastIndexOf(","));
      sqlBuf.append(" ) ");
      sql = sqlBuf.toString();

      try {
          stmt.executeUpdate(sql);
//System.out.println ( "PGConn sql: " + sql ); 
      } catch (SQLException sqle) {
          throw new Exception(
              "Insert statement failed: " + sqle.toString() + "\n" + sql);
      }
  } // End insertFeature
// --------------------------------------------------------- insertFeature ( )  
  
// --------------------------------------------------------- insertFeature ( ) 
  /**
   * Insert feature in table. No unique column must exist! U.D.
   */
  private void insertFeatureNew
  	(Feature f, Statement stmt, HashSet cols, 
  			FeatureSchema schema, int SRID )
      throws Exception {
      StringBuffer sqlBuf = new StringBuffer("INSERT INTO " + table + " (");
      String sql = null;
//System.out.println ( "PGC 766 insertFeatureNew" );       
// System.out.println ( "PGConn existsUnique: " + existsUnique ); 
      
      for (int i = 0; i < schema.getAttributeCount(); i++) {
          String name = schema.getAttributeName(i);
// toLowerCase, ud, 2006.07.29
          if (cols.contains(name.toLowerCase( ))) {
              sqlBuf.append(" " + name.toLowerCase ( ) + ",");
          }
      }
      sqlBuf.deleteCharAt(sqlBuf.lastIndexOf(","));
      sqlBuf.append(" ) VALUES (");
      String insertQueryHead = sqlBuf.toString();
      sqlBuf = new StringBuffer(insertQueryHead);
      for (int j = 0; j < schema.getAttributeCount(); j++) {
          String name = schema.getAttributeName(j);
// toLowerCase, ud, 2006.07.29
          if (cols.contains(name.toLowerCase ( ))) {
              AttributeType type = schema.getAttributeType(j);
              
              if (type.equals(AttributeType.INTEGER)) {
//System.out.println ( "PGCon: f.a: " + f.getAttribute ( j ) );
// u.d.  if the field has no value         
									if ( f.getAttribute ( j ) == null )
									{ sqlBuf.append(" " + null + ","); }
									else 
									{
										Object ob = f.getAttribute ( j );
										String iType = ob.getClass ( ).getName( );
										
										if ( iType.indexOf ( "Integer" ) >= 0  )
										{
											Integer iObj = ( Integer ) ob;

//System.out.println ( "PGC 842 iObj.class: " + iObj.getClass().getName()); 
//System.out.println ( "PGC 843 Integer: " + iObj.intValue( ) );
//System.out.println ( "PGC 843 f.getInt: " +  f.getInteger(j) );
											sqlBuf.append(" " + f.getInteger(j) + ",");
										}
										else
										{ // Long
											Long iObj = ( Long ) ob;
//System.out.println ( "PGC 856 Long: " + iObj.longValue( ) );
											sqlBuf.append(" " + iObj.longValue( ) + ",");
										}
									}
									
              } else if (type.equals(AttributeType.DOUBLE)) {
 									if ( f.getAttribute ( j ) == null )
									{ sqlBuf.append(" " + null + ","); }
 									else 
 									{ 
										Object ob = f.getAttribute ( j );
										String dType = ob.getClass ( ).getName( );
										
										if ( dType.indexOf ( "Double" ) >= 0  )
										{ 										
											sqlBuf.append(" " + f.getDouble(j) + ","); 
										}
										else
										{ // Big Decimal
											BigDecimal dObj = ( BigDecimal ) ob;
											sqlBuf.append(" " + dObj.doubleValue ( ) + ","); 
										}
 									}
 									
              } else if (type.equals(AttributeType.GEOMETRY)) {
// empty geometries are not saves
              	String sGeom = f.getGeometry( ).toText( );
              	if ( sGeom.indexOf( "EMPTY" ) >= 0 ) 
              	{
//System.out.println ( "PGC 978 geom: " +     f.getGeometry().toText() ); 
									errGeomEmpty = true;
              		return;
              	}
//System.out.println ( "PGC 981 geom: " +     f.getGeometry().toText() );
                  sqlBuf.append(
                      " GeometryFromText( '"
                          + f.getGeometry().toText()
                          + "', "
// UD, uwe.dalluege@rzcn.haw-hamburg.de  
// .getSRID ( ) does not work! Therefor SRID!        
//                          + f.getGeometry().getSRID()
                          + SRID
                          + "),");
              } else {
// ud. for apostrophe in text
                  String fString = f.getString(j);
//            	  sqlBuf.append(" '" + f.getString(j) + "',");
            	  sqlBuf.append(" '" + fString.replaceAll( "'", "''" ) + "',");
              }
          }
      }
      sqlBuf.deleteCharAt(sqlBuf.lastIndexOf(","));
      sqlBuf.append(" ) ");
      sql = sqlBuf.toString();

      try {
          stmt.executeUpdate(sql);
//System.out.println ( "PGConn 1083 sql: " + sql ); 
      } catch (SQLException sqle) {
          throw new Exception(
              "Insert statement failed: " + sqle.toString() + "\n" + sql);
      }
  } // End insertFeature
// --------------------------------------------------------- insertFeature ( )  

  
  
  

  /*
   * Reads the query + connection properties into global variables.
   */
  private void readProperties() {
    server = (String)properties.get( PostGISDataSource.SERVER_KEY );
    port = (String)properties.get( PostGISDataSource.PORT_KEY );
    database = (String)properties.get( PostGISDataSource.DATABASE_KEY );
    table = (String)properties.get( PostGISDataSource.TABLE_KEY );
    username = (String)properties.get( PostGISDataSource.USERNAME_KEY );
    password = (String)properties.get( PostGISDataSource.PASSWORD_KEY );
    uniqueCol = (String)properties.get( PostGISDataSource.UNIQUE_COLUMN_KEY );
    method = (String)properties.get( PostGISDataSource.SAVE_METHOD_KEY );
  }
  
  /*
   * Opens a connection to a PostgresSQL database.
   */
  private java.sql.Connection connect() {
    try {
      String jdbcUrl = "jdbc:postgresql://" + server + ":" + port + "/" + database;
      Class.forName( "org.postgresql.Driver" );
      return(DriverManager.getConnection( jdbcUrl, username, password ));
    }
    catch(ClassNotFoundException cnfe) {
      if (PostGISPlugIn.DEBUG) cnfe.printStackTrace();
      throw new IllegalStateException("Could not load PostgresSQL driver: " + cnfe.getMessage());  
    }
    
    catch(SQLException sqle) {
      if (PostGISPlugIn.DEBUG) sqle.printStackTrace();
      throw new IllegalStateException("Could not connection to database: " + sqle.getMessage());
    } 
  }
  
  /**
   * @see Connection#close()
   */
  public void close() {}

/* (non-Javadoc)
 * @see com.vividsolutions.jump.io.datasource.Connection#executeQuery(java.lang.String, java.util.Collection, com.vividsolutions.jump.task.TaskMonitor)
 */
public FeatureCollection executeQuery(String query, Collection exceptions, TaskMonitor monitor) {
	// TODO Auto-generated method stub (temporary implementation [brent owens])
	return executeQuery(query);
}

/* (non-Javadoc)
 * @see com.vividsolutions.jump.io.datasource.Connection#executeQuery(java.lang.String, com.vividsolutions.jump.task.TaskMonitor)
 */
public FeatureCollection executeQuery(String query, TaskMonitor monitor) throws Exception {
	// TODO Auto-generated method stub (temporary implementation [brent owens])
	return executeQuery(query);
}

/* (non-Javadoc)
 * @see com.vividsolutions.jump.io.datasource.Connection#executeUpdate(java.lang.String, com.vividsolutions.jump.feature.FeatureCollection, com.vividsolutions.jump.task.TaskMonitor)
 */
public void executeUpdate(String query, FeatureCollection featureCollection, TaskMonitor monitor) throws Exception {
	// TODO Auto-generated method stub (temporary implementation [brent owens])
	executeUpdate(query, featureCollection);
}
}
 