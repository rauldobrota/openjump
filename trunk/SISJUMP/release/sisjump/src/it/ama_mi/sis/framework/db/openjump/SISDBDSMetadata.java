package it.ama_mi.sis.framework.db.openjump;

import it.ama_mi.sis.framework.db.DBRowMorpher;
import it.ama_mi.sis.framework.db.col.DBColContext;
import it.ama_mi.sis.framework.db.col.DBRow;
import it.ama_mi.sis.framework.db.rh.SingleFieldRH;
import it.ama_mi.sis.framework.utils.Props;
import it.ama_mi.sis.framework.utils.db.CommandBuilder;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.datastore.DataStoreMetadata;



public class SISDBDSMetadata implements DataStoreMetadata {

  private SISDBDSConnection conn;
	private DBColContext[] contexts;
  //private Map sridMap = new HashMap();

  public SISDBDSMetadata( SISDBDSConnection conn ) {
    this.conn = conn;
  }

  private void readContexts() {
  	if( contexts != null )
  		return;
  	
			//if a schema name was specified use only tables from that schema
  	String schemaName = conn.getSchemaName();
  	if( "".equals(schemaName != null ? schemaName : "") )
  			schemaName = null;				//use null instead of an empty string
  	
  	contexts = conn.getData().getRowContexts(
  			new DBColContext(schemaName,"%"),true);			//want views
  }
  
  public String[] getDatasetNames() {
  	readContexts();				//force contexts creation

  		//as dataset names, use the complete combination of schemaName.tableName
		ArrayList arr = new ArrayList();
		for(int i=0;i<contexts.length;i++)
			//arr.add(contexts[i].getTableName());			
			arr.add(contexts[i].toString());			
		
		return (String[])arr.toArray(new String[arr.size()]);
  }


  public Envelope getExtents( String datasetName, String attributeName )
  {
			//use geometry(extent()) to make sure a geometry is returned, not a BOX
		CommandBuilder builder = new CommandBuilder(conn.getData());
		//builder.appendLine("SELECT geometry(extent(${specifiedgeom}))");
		
		//to support all different adapters, let them specify the table to act upon,
		//because some of them will want to use something like "SELECT xxx FROM DUAL"
		//instead of "SELECT xxx FROM some_table"
		//builder.appendLine("SELECT ${EXTENT} FROM ${st} t");
		builder.appendLine("SELECT ${EXTENT} FROM ${EXTENT_TABLE} t");
		
			//The specified dataset name be in the form of schemaName.tableName
			//so separate them and use them accordingly
		DBColContext context = DBColContext.valueOf(datasetName);
		Props props = builder.prepareProps(new CommandBuilder.FromItem(context,"t"));
		props.add("EXTENT_GEOM","${geom[" + attributeName + "]}");
		props.add("EXTENT_GEOM_nq","${geom_nq[" + attributeName + "]}");
  	
  	String readCommand = props.subs(builder.toString());
  	
		final DBRowMorpher morpher = conn.newMorpher(); 		
		Geometry geom = 
			(Geometry)conn.getData().read(readCommand,new SingleFieldRH(0) {
			public boolean handleFirstRow(DBRow dbrow) {
				//use source as destination too, no need to clone here
				morpher.setup(dbrow,dbrow);
    		
					//let the superclass handle this
				return super.handleFirstRow(morpher.getMorphedRow());		//use the morphed row
			}
		
			public boolean handleRow(DBRow dbrow) {				
				morpher.morph();				//produce the morphed row
				
					//let the superclass handle this
				return super.handleRow(morpher.getMorphedRow());		//use the morphed row
			}			
		});

  	return geom.getEnvelopeInternal();
  }

/*  
  public SpatialReferenceSystemID getSRID(String tableName, String colName)
          throws SQLException {
      String key = tableName + "#" + colName;
      if (!sridMap.containsKey(key)) {
          // not in cache, so query it
          String srid = querySRID(tableName, colName);
          sridMap.put(key, new SpatialReferenceSystemID(srid));
      }
      SpatialReferenceSystemID srid = (SpatialReferenceSystemID) sridMap
              .get(key);
      return srid;
  }

  private String querySRID(String tableName, String colName)
  {
    String sql = "SELECT getsrid(\"" + colName + "\") FROM \"" + tableName + "\" LIMIT 1";
  	Object obj = conn.getData().read(sql,new SingleFieldRH(0));

  	return String.valueOf(obj);
  } */

  public String[] getGeometryAttributeNames( String datasetName ) {
			//The specified dataset name be in the form of schemaName.tableName
			//so separate them and use them accordingly
  		//don't quote column names
		DBColContext context = DBColContext.valueOf(datasetName);
  	return new CommandBuilder(conn.getData()).getGeoms(
  			context.getSchemaName(),context.getTableName(),false);
  }

  public String[] getColumnNames( String datasetName ) {
			//The specified dataset name be in the form of schemaName.tableName
			//so separate them and use them accordingly
			//don't quote column names
		DBColContext context = DBColContext.valueOf(datasetName);
  	return new CommandBuilder(conn.getData()).getFields(
  			context.getSchemaName(),context.getTableName(),false);
  }
}