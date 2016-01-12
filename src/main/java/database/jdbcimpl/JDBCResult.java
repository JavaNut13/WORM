package database.jdbcimpl;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.abstractation.SQLResult;

public final class JDBCResult implements SQLResult {
  private final ResultSet result;

  public JDBCResult(ResultSet rs) {
    result = rs;
  }

  public Object get(String column, Object defaultValue) {
    try {
      return result.getObject(column);
    } catch (SQLException sqle) {
      return defaultValue;
    }
  }

  public Object get(int index, Object defaultValue) {
    try {
      return result.getObject(index);
    } catch (SQLException sqle) {
      return defaultValue;
    }
  }


  public boolean moveToNext() {
    try {
      return result.next();
    } catch (SQLException sqle) {
      return false;
    }
  }

  public boolean moveToFirst() {
    try {
      return result.first();
    } catch (SQLException sqle) {
      return false;
    }
  }

  public void close() {
    try {
      result.close();
    } catch (SQLException sqle) {
      sqle.printStackTrace();
    }
  }
}
