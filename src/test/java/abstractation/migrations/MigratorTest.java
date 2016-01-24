package abstractation.migrations;

import org.junit.Before;
import org.junit.Test;

import abstractation.Connection;
import abstractation.Log;
import jdbcimpl.JDBCConnection;
import related.TestMigrator;

public class MigratorTest {

  @Before
  public void setUp() throws Exception {
    Log.logAt(Log.Level.VERBOSE);

  }

  @Test
  public void testUpgrade() throws Exception {
    Log.e("Migrating");
    Connection con = new JDBCConnection(TestMigrator.class);
    con.open();
  }

  @Test
  public void testPerform() throws Exception {

  }
}