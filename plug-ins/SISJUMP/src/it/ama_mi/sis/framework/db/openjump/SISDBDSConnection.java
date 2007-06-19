package it.ama_mi.sis.framework.db.openjump;

import it.ama_mi.sis.framework.db.DBColMorpher;
import it.ama_mi.sis.framework.db.DBColMorpherFactory;
import it.ama_mi.sis.framework.db.DBRowMorpher;
import it.ama_mi.sis.framework.db.col.DBCol;
import it.ama_mi.sis.framework.db.col.DBColType;
import it.ama_mi.sis.framework.db.jdbc.JdbcDBDataImpl;
import it.ama_mi.sis.framework.db.jdbc.cm.GeometryOracleToJtsCM;
import it.ama_mi.sis.framework.db.jdbc.cm.GeometryPostgisToJtsCM;
import it.ama_mi.sis.framework.db.rm.DBColTypeDBRowMorpher;

import java.sql.SQLException;
import java.sql.Types;

import com.vividsolutions.jump.datastore.AdhocQuery;
import com.vividsolutions.jump.datastore.DataStoreConnection;
import com.vividsolutions.jump.datastore.DataStoreException;
import com.vividsolutions.jump.datastore.DataStoreMetadata;
import com.vividsolutions.jump.datastore.FilterQuery;
import com.vividsolutions.jump.datastore.Query;
import com.vividsolutions.jump.io.FeatureInputStream;

/**
 */
public class SISDBDSConnection
    implements DataStoreConnection
{
	private JdbcDBDataImpl data;
	private String schemaName;
	
  private SISDBDSMetadata dbMetadata;

  public SISDBDSConnection(JdbcDBDataImpl data,String schemaName) {
    this.data = data;
    setSchemaName(schemaName);
    dbMetadata = new SISDBDSMetadata(this);
  }

  /**
	 * @return Returns the data.
	 */
	public synchronized JdbcDBDataImpl getData() {
			//always connect before returning
		if( data != null )
			if( !data.isConnected() )
				data.connect();
		
		return data;
	}

	/**
	 * @return Returns the schemaName.
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schemaName The schemaName to set.
	 */
	public void setSchemaName(String schemaName) {
			//remove prefix and suffix blanks 
		schemaName = schemaName != null ? schemaName.trim() : null;
		
			//if a single "-" was specified, use null instead
		if( "-".equals(schemaName != null ? schemaName : "") )
			schemaName = null;

			//if an empty string was specified, use null instead
		if( "".equals(schemaName != null ? schemaName : "") )
			schemaName = null;
				
		this.schemaName = schemaName;
	}

	public DBRowMorpher newMorpher() {
		DBColTypeDBRowMorpher morpher = new DBColTypeDBRowMorpher();
		
		DBColMorpherFactory factory = new DBColMorpherFactory() {
			public DBColMorpher createColMorpher(int index, DBCol src, DBCol dst) {
				return new GeometryPostgisToJtsCM(src,dst);
			}
		};		
		morpher.add(
				new DBColType(Types.OTHER,"geometry"),
				new DBColType(Types.OTHER,"geometry"),
				factory);
		
		factory = new DBColMorpherFactory() {
			public DBColMorpher createColMorpher(int index, DBCol src, DBCol dst) {
				return new GeometryOracleToJtsCM(src,dst);
			}
		};		
		morpher.add(
				new DBColType(Types.OTHER,"SDO_GEOMETRY"),
				new DBColType(Types.OTHER,"SDO_GEOMETRY"),
				factory);
		morpher.add(
				new DBColType(Types.STRUCT,"MDSYS.SDO_GEOMETRY"),
				new DBColType(Types.STRUCT,"MDSYS.SDO_GEOMETRY"),
				factory);
		
		return morpher;
	}
	
	public DataStoreMetadata getMetadata()
  {
    return dbMetadata;
  }

    public FeatureInputStream execute(Query query) {
        if (query instanceof FilterQuery) {
            try {
                return executeFilterQuery((FilterQuery) query);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (query instanceof AdhocQuery) {
            return executeAdhocQuery((AdhocQuery) query);
        }
        throw new IllegalArgumentException("Unsupported Query type");
    }

  /**
   * Executes a filter query.
   *
   * The SRID is optional for queries - it will be determined automatically
   * from the table metadata if not supplied.
   *
   * @param query the query to execute
   * @return the results of the query
 * @throws SQLException
   */
  public FeatureInputStream executeFilterQuery(FilterQuery query) throws SQLException
  {
  	/*
    SpatialReferenceSystemID srid = dbMetadata.getSRID(query.getDatasetName(), query.getGeometryAttributeName());
    String[] colNames = dbMetadata.getColumnNames(query.getDatasetName());

    SISDBSQLBuilder builder = new SISDBSQLBuilder(srid, colNames);
    String queryString = builder.getSQL(query); */

    SISDBFeatureInputStream ifs = new SISDBFeatureInputStream(this, query);
    
    return ifs;
  }

  public FeatureInputStream executeAdhocQuery(AdhocQuery query)
  {
    SISDBFeatureInputStream ifs = new SISDBFeatureInputStream(this, query);
    
    return ifs;
  }


  public void close()
      throws DataStoreException
  {
    try {
    	if( !data.isConnected() )
    		data.disconnect();
    }
    catch (Exception ex) { throw new DataStoreException(ex); }
  }

  public boolean isClosed() throws DataStoreException {
    try {
        return !data.isConnected();
    } catch (Exception e) {
        throw new DataStoreException(e);
    }
  }

}