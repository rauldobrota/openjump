package com.vividsolutions.jump.datastore.postgis;

import java.sql.*;
import java.util.*;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jump.datastore.*;
import com.vividsolutions.jump.datastore.jdbc.*;

public class PostgisDSMetadata implements DataStoreMetadata {

  private PostgisDSConnection conn;

  private Map sridMap = new HashMap();

  public PostgisDSMetadata(PostgisDSConnection conn) {
    this.conn = conn;
  }

  public String[] getDatasetNames() {
    final List datasetNames = new ArrayList();
    // Spatial tables only.
    JDBCUtil.execute(
        conn.getConnection(),
        "SELECT DISTINCT f_table_schema, f_table_name FROM geometry_columns",
        new ResultSetBlock() {
      public void yield(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
          String schema = resultSet.getString(1);
          String table = resultSet.getString(2);
          if (! schema.equalsIgnoreCase("public"))
            table = schema + "." + table;
          datasetNames.add(table);
        }
      }
    } );
    return (String[]) datasetNames.toArray(new String[] {});
  }

  private final WKBReader reader = new WKBReader();

  public Envelope getExtents(String datasetName, String attributeName) {
    final Envelope[] e = new Envelope[]{null};
    //String sql = "SELECT AsBinary(EXTENT("+attributeName+")) FROM "+datasetName;
    String sql = "SELECT AsBinary(estimated_extent( '" + datasetName + "', '" + attributeName + "' ))";
    JDBCUtil.execute(
        conn.getConnection(), sql,
            new ResultSetBlock() {
      public void yield(ResultSet resultSet) throws Exception {
        if (resultSet.next()) {
          Geometry geom = reader.read((byte[]) resultSet.getObject(1));
          if(geom != null)
            e[0] = geom.getEnvelopeInternal();
        }
      }
    });
    return e[0];
  }

  public SpatialReferenceSystemID getSRID(String tableName, String colName)
      throws SQLException {
    String key = tableName + "#" + colName;
    if (!sridMap.containsKey(key)) {
      // not in cache, so query it
      //HACK
      //String srid = querySRID(tableName, colName);
      String srid = "-1";
      sridMap.put(key, new SpatialReferenceSystemID(srid));
    }
    SpatialReferenceSystemID srid = (SpatialReferenceSystemID) sridMap
                                  .get(key);
    return srid;
  }

  private String geomColumnMetadataWhereClause(String schemaCol, String tableCol, String tableName)
  {
    int dotPos = tableName.indexOf(".");
    return dotPos == -1
                  ? "WHERE lower(" + tableCol + ") = '" + tableName.toLowerCase() + "'"

    : "WHERE lower(" + schemaCol + ") = '"
                  + tableName.substring(0, dotPos).toLowerCase()
                  + "' "
                  + " AND lower(" + tableCol + ") = '"
                  + tableName.substring(dotPos + 1).toLowerCase() + "'";
  }

  public String[] getGeometryAttributeNames(String datasetName) {
    final List geometryAttributeNames = new ArrayList();
    String sql = "SELECT f_geometry_column FROM geometry_columns "
               + geomColumnMetadataWhereClause("f_table_schema", "f_table_name", datasetName);
    JDBCUtil.execute(
        conn.getConnection(), sql,
        new ResultSetBlock() {
      public void yield(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
          geometryAttributeNames.add(resultSet.getString(1));
        }
      }
    });
    return (String[]) geometryAttributeNames.toArray(new String[] {});
  }

  private static class ColumnNameBlock implements ResultSetBlock
  {
    List colList = new ArrayList();
    String[] colName;

    public void yield(ResultSet resultSet) throws SQLException {
      while (resultSet.next()) {
        colList.add(resultSet.getString(1));
      }
      colName = (String[]) colList.toArray(new String[0]);
    }
  }

  public String[] getColumnNames(String datasetName)
  {
    // MD - testing
//    if (datasetName.equalsIgnoreCase("trim_hydro"))
//      return new String[] { "the_geom", "gid" };

    String sql = "SELECT column_name FROM information_schema.columns "
                 + geomColumnMetadataWhereClause("table_schema", "table_name", datasetName);
    ColumnNameBlock block = new ColumnNameBlock();
    JDBCUtil.execute(conn.getConnection(), sql, block);
    return block.colName;
  }
}