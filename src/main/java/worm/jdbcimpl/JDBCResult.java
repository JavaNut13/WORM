package worm.jdbcimpl;

import java.sql.ResultSet;
import java.sql.SQLException;

import worm.abstractation.SQLResult;

/**
 * Wraps a JDBC ResultSet to conform to the SQLResult interface
 */
public final class JDBCResult implements SQLResult {
  private final ResultSet result;

  public JDBCResult(ResultSet rs) {
    result = rs;
  }

  public Object get(String column, Object defaultValue) {
    try {
      Object res = result.getObject(column);
      return res;
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
      if (result.getRow() == 0) {
        return result.next();
      } else {
        return result.first();
      }
      //      return result.getRow() == 0 || result.first();
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }

  public void close() {
    try {
      result.close();
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      sqle.printStackTrace();
    }
  }
}
