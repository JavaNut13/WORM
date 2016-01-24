package abstractation.migrations;

import java.sql.SQLException;

import abstractation.Connection;
import abstractation.Log;

public class Migration {
  public Migration(Class table, Type operation) {

  }

  public Migration() {

  }

  public void run(Connection con) throws SQLException {
    Log.e("Migrating", con);
  }

  public enum Type {
    CREATE, DROP
  }
}
