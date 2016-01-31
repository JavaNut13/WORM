package worm.abstractation;

import java.sql.SQLException;

import worm.annotations.Stored;

/**
 * Little superclass to store the rowid and a save() method. For record objects to extend
 */
public class Row {
  @Stored
  public int rowid;

  public void save() throws SQLException {
    Connection con = Connection.getGlobal();
    con.save(this);
  }
}
