package abstractation.migrations;

import java.sql.SQLException;
import java.util.ArrayList;

import abstractation.Connection;

/**
 * Created by will on 24/01/16.
 */
public abstract class Migrator {
  private ArrayList<Migration> migrations;

  public Migrator() {
    migrations = new ArrayList<>();
  }

  protected abstract void upgrade();

  protected void migrate(Migration m) {
    migrations.add(m);
  }

  public void perform(Connection con) {
    upgrade();
    for(Migration m : migrations) {
      try {
        m.run(con);
      } catch(SQLException sqle) {
        sqle.printStackTrace();
      }
    }
  }
}
