package database.jdbcimpl;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.abstractation.Log;
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
      Log.i("Error getting column:", column);
      return defaultValue;
    }
  }

  public Object get(int index, Object defaultValue) {
    try {
      return result.getObject(index);
    } catch (SQLException sqle) {
      Log.i("Error getting index:", index);
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
      return result.getRow() == 0 || result.first();
    } catch (SQLException sqle) {
      Log.w("Error moving to first:", sqle.getMessage());
      return false;
    }
  }

  public void close() {
    try {
      result.close();
    } catch (SQLException sqle) {
      Log.w("Error closing:", sqle.getMessage());
      sqle.printStackTrace();
    }
  }
}
