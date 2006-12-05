package it.ama_mi.sis.framework.db.openjump;

import it.ama_mi.sis.framework.db.BaseDBData;
import it.ama_mi.sis.framework.db.DBRowMorpher;
import it.ama_mi.sis.framework.db.col.DBCol;
import it.ama_mi.sis.framework.db.col.DBColContext;
import it.ama_mi.sis.framework.db.col.DBColType;
import it.ama_mi.sis.framework.db.col.DBRow;
import it.ama_mi.sis.framework.db.jdbc.adapters.OracleDBDataAdapter;
import it.ama_mi.sis.framework.db.jdbc.adapters.PostgresqlDBDataAdapter;
import it.ama_mi.sis.framework.db.rh.QueuedRH;
import it.ama_mi.sis.framework.utils.Props;
import it.ama_mi.sis.framework.utils.db.CommandBuilder;
import it.ama_mi.sis.framework.utils.geom.oracle.OraWriter;

import java.sql.SQLException;
import java.sql.Types;

import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;

import org.postgis.jts.JtsBinaryWriter;
import org.postgresql.util.PGobject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.datastore.AdhocQuery;
import com.vividsolutions.jump.datastore.FilterQuery;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.FeatureInputStream;

/**
 * Reads features from an Oracle database.
 */
public class SISDBFeatureInputStream implements FeatureInputStream
{
  private SISDBDSConnection conn;
	private QueuedRH queued;
	private FeatureSchema featureSchema;  
  private FilterQuery query;
  private AdhocQuery adhocQuery;
  private int[] attrIndex;

  public SISDBFeatureInputStream(SISDBDSConnection conn, FilterQuery query) {
    this.conn = conn;
    this.query = query;
  }

  public SISDBFeatureInputStream(SISDBDSConnection conn, AdhocQuery adhocQuery) {
    this.conn = conn;
    this.adhocQuery = adhocQuery;
  }

  private void startQueued(final String readCommand,final DBRow parameterRow) {
		new Thread(new Runnable() {
			public void run() {
		    BaseDBData.read(conn.getData(),readCommand,parameterRow,queued);
			};
		}).start();			
  }
  
  private synchronized void init()
  {
    if( queued != null )						//if already created
  		return;													//nothing to do

		conn.getData().setDebugStream(System.out);
		conn.getData().setDebugLevel(1);									//1=>execute and print

		String readCommand = "";
		DBRow parameterRow = null;
		
			//if a filter query was specified
		if( query != null ) {
				//create the read command query around the specified dataset name
			readCommand = makeFilterReadCommand();
			//create the necessary spatial query parameter row for the filter geometry
			parameterRow = makeParameterRow(query.getFilterGeometry());
		}
		
			//if an adhoc query was specified
		if( adhocQuery != null ) {
				//create the read command for the specified adhoc query
			readCommand = makeAdhocReadCommand();
		}
		
			//use a very small queue. Capacity is not important, it's basically used
			//only to separate the thread reading the DB from the one painting the screen
			//Use a timeout of 60 seconds while putting, so that we stop it if the
			//painting thread stops reading.
			//Use a timeout of 60 seconds while getting, so that the painting thread
			//stops reading if we cannot provide rows faster enough
			//Don't throw exceptions on timeouts. TODO review!!! Is it a good idea???
			//TODO review!!! Make these parameters???
		final DBRowMorpher morpher = conn.newMorpher(); 		
		queued = new QueuedRH(5,60000,60000,false) {
			public boolean handleFirstRow(DBRow dbrow) {
				//use source as destination too, no need to clone here
				morpher.setup(dbrow,dbrow);
				
    		createFeatureSchema(morpher.getMorphedRow());				//use the morphed row
    		
					//let the superclass handle this
				return super.handleFirstRow(morpher.getMorphedRow());		//use the morphed row
			}
		
			public boolean handleRow(DBRow dbrow) {				
				morpher.morph();				//produce the morphed row
				
					//let the superclass handle this
				return super.handleRow(morpher.getMorphedRow());		//use the morphed row
			}			
		};
		
			//if no read command was created, log it
		if( readCommand == null )
    	System.out.println("No read command created");
		else	
			startQueued(readCommand,parameterRow);
  }
  
	private String makeFilterReadCommand() {
			//sorround the provided datadef with the box filter
			//on the first geometric field (${geom} stands for ${geoms[0]}
		CommandBuilder builder = new CommandBuilder(conn.getData());
		builder.appendLine("SELECT * FROM ${st} t");
		builder.appendLine("WHERE ${BOXFILTER}");

			//The dataset name inside the query may be in the form of schemaName.tableName
			//so separate them and use them accordingly
		DBColContext context = DBColContext.valueOf(query.getDatasetName());
		Props props = builder.prepareProps(new CommandBuilder.FromItem(context,"t"));
		props.add("BOXFILTER_GEOM",
				"${t.geom[" + query.getGeometryAttributeName() + "]}");
		
		String readCommand = props.subs(builder.toString());
		
	  String whereCond = query.getCondition();
	  if (whereCond != null)
	  	readCommand += " AND " + whereCond;
		
	/*		
		if( sql.indexOf("${geom}") > -1 )
			return null;				//don't work if no geometric column found */

	
		return readCommand;
	}

	private String makeAdhocReadCommand() {
			//let the provided adhoc query pass through Props substitution
			//so that the user can use the ${} syntax
		 	//NO!!! It's impossibile, because we don't have any from item to use!!!
			//TODO review!!! Add a specific UI to OpenJUMP to support this and other
			//functionalities???
		return adhocQuery.getQuery();
	}

/*	
	private Object[] makeBoundsArgs(Geometry geom) {
    Envelope env = geom.getEnvelopeInternal();
		
    System.out.println("thread: " + Thread.currentThread() + " env: " + env);
    
		GeometryFactory factory = new GeometryFactory();
		LineString jtsline = factory.createLineString(
				new Coordinate[] { 
						new Coordinate(env.getMinX(),env.getMinY()),
						new Coordinate(env.getMaxX(),env.getMaxY()) });

		try {
			//HACK!!!
			if( conn.getData().getAdapter() instanceof OracleDBDataAdapter ) {
				jtsline.setSRID(0);					//special case for VIEWPORT_TRANSFORM()
				OraWriter writer = new OraWriter(
						(OracleConnection)conn.getData().getNativeConnection());
				//set to use the instances SRID first, and then the factory's one
				writer.setWhichSRID(OraWriter.WHICHSRID_INSTANCEFACTORY);
				STRUCT line = writer.write(jtsline);
				
				return new Object[] { line };
			} 

			//HACK!!!
			if( conn.getData().getAdapter() instanceof PostgresqlDBDataAdapter) {
				JtsBinaryWriter writer = new JtsBinaryWriter();		
				PGobject line = new PGobject();
				line.setType("geometry");			//TODO review!! copied from PGgeometry!!!
				line.setValue(writer.writeHexed(jtsline));
				
				return new Object[] { line };
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		throw new IllegalStateException("Adapter not supported");
	}
	
	private int[] makeArgsTypes(Object[] args) {
		int[] res = new int[args.length];

		//HACK!!!
		if( conn.getData().getAdapter() instanceof OracleDBDataAdapter )
			Arrays.fill(res,Types.STRUCT);
		else if( conn.getData().getAdapter() instanceof PostgresqlDBDataAdapter)
			Arrays.fill(res,Types.OTHER);
		else
			throw new IllegalStateException("Adapter not supported");
		
		return res;
	} */

	private DBRow makeParameterRow(Geometry geom) {
    Envelope env = geom.getEnvelopeInternal();
		
    System.out.println("thread: " + Thread.currentThread() + " env: " + env);
    
		GeometryFactory factory = new GeometryFactory();
		LineString jtsline = factory.createLineString(
				new Coordinate[] { 
						new Coordinate(env.getMinX(),env.getMinY()),
						new Coordinate(env.getMaxX(),env.getMaxY()) });

		DBCol column = null;
		try {
			//HACK!!!
			if( conn.getData().getAdapter() instanceof OracleDBDataAdapter ) {
				jtsline.setSRID(0);					//special case for VIEWPORT_TRANSFORM()
				OraWriter writer = new OraWriter(
						(OracleConnection)conn.getData().getNativeConnection());
				//set to use the instances SRID first, and then the factory's one
				writer.setWhichSRID(OraWriter.WHICHSRID_INSTANCEFACTORY);
				STRUCT line = writer.write(jtsline);
				
				column = new DBCol();
				column.setValue(line);
				column.getType().setDataType(Types.STRUCT);
			} 

			//HACK!!!
			if( conn.getData().getAdapter() instanceof PostgresqlDBDataAdapter) {
				JtsBinaryWriter writer = new JtsBinaryWriter();		
				PGobject line = new PGobject();
				line.setType("geometry");			//TODO review!! copied from PGgeometry!!!
				line.setValue(writer.writeHexed(jtsline));
				
				column = new DBCol();
				column.setValue(line);
				column.getType().setDataType(Types.OTHER);
			}			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		if( column == null )
			throw new IllegalStateException("Adapter not supported");

		DBRow res = new DBRow();
		res.addColumn(column);
		
		return res;
	}
		
	private void createFeatureSchema(DBRow dbrow) {
		featureSchema = new FeatureSchema();
		DBCol[] columns = dbrow.getColumns();
			//OpenJUMP can't work with more than one geometric column,
			//so add only the one the user requested while adding the layer
			//If an adhoc query was specified, use only the first geometric column
		attrIndex = new int[columns.length];
		int index = 0;
		boolean geomAdded = false;
		for(int i=0;i<columns.length;i++) {
			attrIndex[i] = -1;				//by default column not added
			
			if( columns[i].getType().isGeometric() ) {
				if( query != null )
					if(	!columns[i].getName().equals(query.getGeometryAttributeName()) )
						continue;
				
				if( geomAdded )
					continue;
				
				geomAdded = true;				//add this column and remember it
			}
		
			featureSchema.addAttribute(columns[i].getName(),mapTypeName(columns[i]));
			attrIndex[i] = index++;				//store the attribute index for this column
		}
	}

	/**
	 * Partially from ValueConverterFactory.getConverter()
	 * 
	 * @param col
	 * @return
	 */
	private AttributeType mapTypeName(DBCol col) {
		DBColType type = col.getType();		
		
/*    String classname = rsm.getColumnClassName(columnIndex);
    String dbTypeName = rsm.getColumnTypeName(columnIndex);
    int precision = rsm.getPrecision(columnIndex);
    int scale = rsm.getScale(columnIndex);
    int sqlType = rsm.getColumnType(columnIndex); */

    if( type.getDataType() == Types.INTEGER ||
    		type.getDataType() == Types.BIGINT ||
    		type.getDataType() == Types.SMALLINT ||
    		type.getDataType() == Types.TINYINT )
        return AttributeType.INTEGER;

    if( type.getDataType() == Types.DATE ||
    		type.getDataType() == Types.TIME ||
    		type.getDataType() == Types.TIMESTAMP )
    	return AttributeType.DATE;

    if( type.getDataType() == Types.DECIMAL ||
    		type.getDataType() == Types.DOUBLE ||
    		type.getDataType() == Types.FLOAT ||
    		type.getDataType() == Types.NUMERIC ||
    		type.getDataType() == Types.REAL )
    	return AttributeType.DOUBLE;

    if( type.isGeometric() )
    	return AttributeType.GEOMETRY;
    
    // default is string
    return AttributeType.STRING;
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.io.FeatureInputStream#hasNext()
	 */
	public synchronized boolean hasNext() throws Exception {
		init();
		
		return queued.hasNext();
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jump.io.FeatureInputStream#next()
	 */
	public synchronized Feature next() throws Exception {
		init();
		
		Object[] values = queued.next();
		if( values == null )
			return null;						//TODO review!!! What else should I do???
		
		Feature res = new BasicFeature(getFeatureSchema());		
			//some columns may have not been added, so use the attribute index
		for(int i=0;i<values.length;i++)
			if( attrIndex[i] > -1 )
				res.setAttribute(attrIndex[i],values[i]);
		
		return res;
	}

  public synchronized void close() throws SQLException {
  	if( queued != null )
  		queued.close();

  	queued = null;
  }

  public synchronized FeatureSchema getFeatureSchema()
  {
  	init();
  	  
  		//must call hasNext() to wait for at least the first row
  	queued.hasNext();
  	
	  	//if no schema created, log it and create an empty one
	  if( featureSchema == null ) {
	  	System.out.println("Empty FeatureSchema created");
	  	featureSchema = new FeatureSchema();
	  }
  	
  	return featureSchema;
  }
}