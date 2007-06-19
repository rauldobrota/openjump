package it.ama_mi.sis.framework.db.openjump;

import it.ama_mi.sis.framework.db.jdbc.JdbcDBDataFactory;
import it.ama_mi.sis.framework.db.jdbc.JdbcDBDataImpl;

import com.vividsolutions.jump.datastore.DataStoreConnection;
import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.parameter.ParameterList;
import com.vividsolutions.jump.parameter.ParameterListSchema;

/**
 * A driver for supplying {@link OracleDSConnection}s
 */
public class SISDBDataStoreDriver
    implements DataStoreDriver
{
	
  private ParameterListSchema schema;

  public SISDBDataStoreDriver() {
  }

  public String getName()
  {
    return "SISDB";
  }
  public ParameterListSchema getParameterListSchema()
  {
  	if( schema == null ) {
  	  String[] paramNames = new String[] {
  	  		"Adapter",
  	  		"Address",
  	  		"Port",
  	  		"Db",
  	  		"Username",
  	  		"Password",
  	  		"Schema"
  	    };
  	  Class[] paramClasses = new Class[]
  	  {
  	    String.class,
  	    String.class,
  	    String.class,
  	    String.class,
  	    String.class,
  	    String.class,
  	    String.class
  	    };
  	  
      schema = new ParameterListSchema(paramNames, paramClasses);
  	}
  	
  	return schema;
 }
  public DataStoreConnection createConnection(ParameterList params)
      throws Exception
  {
  	JdbcDBDataFactory factory = JdbcDBDataFactory.create(
  			"",
  			params.getParameterString("Adapter"),
  			params.getParameterString("Address"),
  			params.getParameterString("Port"),
  			params.getParameterString("Db"),
  			params.getParameterString("Username"),
  			params.getParameterString("Password")
  			);
  	
  	JdbcDBDataImpl data = (JdbcDBDataImpl)factory.newData();
  	
  	return new SISDBDSConnection(data,
  			params.getParameterString("Schema"));
  }
  public boolean isAdHocQuerySupported() {
      return true;
  }

}