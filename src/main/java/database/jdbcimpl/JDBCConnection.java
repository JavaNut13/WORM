package database.jdbcimpl;

import java.io.File;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.abstractation.Connection;
import database.abstractation.SQLResult;


public class JDBCConnection extends Connection {
  private java.sql.Connection database;
  private final String path;

  public JDBCConnection() {
    path = ":memory:";
  }


  public JDBCConnection(File location) {
    path = location.getAbsolutePath();
  }

  public JDBCConnection(String location) {
    path = location;
  }

  public Connection open() throws SQLException {
    close();
    database = DriverManager.getConnection("jdbc:sqlite:" + path);

    return this;
  }

  public boolean close() {
    if(database == null) {
      return false;
    }
    try {
      database.close();
      return true;
    } catch(SQLException sqle) {
      return false;
    }
  }

  public boolean isClosed() {
    return database == null;
  }

  public void sqlWithoutResult(String sql, Object[] args) throws SQLException {
    PreparedStatement stmt = database.prepareStatement(sql);
    addArgsToStatement(stmt, args);
    stmt.execute();
    stmt.close();
  }

  public SQLResult sqlWithResult(String sql, Object[] args) throws SQLException {
    PreparedStatement stmt = database.prepareStatement(sql);
    addArgsToStatement(stmt, args);
    ResultSet rs = stmt.executeQuery();
    return new JDBCResult(rs);
  }

  public int sqlReturningRowid(String sql, Object[] args) throws SQLException {
    PreparedStatement stmt = database.prepareStatement(sql);
    addArgsToStatement(stmt, args);
    stmt.executeUpdate();
    ResultSet rs = stmt.getGeneratedKeys();
    int id = rs.getInt("last_insert_rowid()");
    rs.close();
    return id;
  }

  private static void addArgsToStatement(PreparedStatement stmt, Object[] args) throws SQLException {
    int start = 1;
    for(Object arg : args) {
      if(arg instanceof Integer) {
        stmt.setInt(start, (Integer) arg);
      } else if(arg instanceof String) {
        stmt.setString(start, (String) arg);
      } else if(arg instanceof Float) {
        stmt.setFloat(start, (Float) arg);
      } else if(arg instanceof Long) {
        stmt.setLong(start, (Long) arg);
      } else if(arg instanceof Boolean) {
        stmt.setBoolean(start, (Boolean) arg);
      } else if(arg instanceof Double) {
        stmt.setDouble(start, (Double) arg);
      }
      start += 1;
    }
  }
}
