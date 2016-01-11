package database.abstractation;

import java.sql.SQLException;

import database.annotations.Stored;

public class Row {
  @Stored public int rowid;

  public void save() throws SQLException {
    Connection con = Connection.getGlobal();
    con.save(this);
  }
}
