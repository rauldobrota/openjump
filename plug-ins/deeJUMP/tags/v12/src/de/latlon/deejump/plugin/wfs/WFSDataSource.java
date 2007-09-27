package de.latlon.deejump.plugin.wfs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.task.TaskMonitor;

import de.latlon.deejump.util.data.JUMPFeatureFactory;

public class WFSDataSource extends DataSource {
    
    private WFSReader reader; 
    
    public WFSDataSource() {     
        reader = new WFSReader();
    }
    
    public Connection getConnection() {
        
        return new Connection() {
        
            public FeatureCollection executeQuery( String query, Collection exceptions,
                                                   TaskMonitor monitor) {
            
                try {
                    return reader.read( getProperties() );
                } catch (Exception e) {
                    exceptions.add(e);
                    return null;
                }
            }

            public void executeUpdate(String update, FeatureCollection featureCollection, TaskMonitor monitor)
                throws Exception {
                throw new UnsupportedOperationException( "Cannot update yet." );
            }

            public void close() {}

            public FeatureCollection executeQuery(String query, TaskMonitor monitor) throws Exception {
                
                ArrayList exceptions = new ArrayList();
                FeatureCollection featureCollection = executeQuery(query, exceptions, monitor);
                
                if (!exceptions.isEmpty()) {
                    throw (Exception) exceptions.iterator().next();
                }
                
                return featureCollection;
                
            }
        };
    }

    private static class WFSReader  {
        
        WFSReader(){   }
                
        FeatureCollection read( Map props ) throws Exception{
            
            //TODO move to somewhere different e.g. setter?
            
            StringBuffer req = new StringBuffer( (String)props.get( "REQUEST" ) );
            
            // must remove request from  <![CDATA["+ request +"]]>
            String request = req.substring(9, req.length()-3);
            
            org.deegree.model.feature.FeatureCollection dfc = 
                JUMPFeatureFactory.createDeegreeFCfromWFS( (String)props.get( "SERVER_URL" ), 
                                                            request );
            
            FeatureCollection dataset = JUMPFeatureFactory.createFromDeegreeFC( dfc );
            
            return dataset;
        }
        
    }
    
}
