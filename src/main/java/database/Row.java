package database;

import java.sql.SQLException;

import database.annotations.Stored;

/**
 * Created by will on 10/01/16.
 */
public class Row {
  @Stored public int rowid;

  public void save() throws SQLException {
    Connection con = Connection.getGlobal();
    con.save(this);
  }
}
