package abstractation.migrations;

import org.junit.Test;

import java.util.HashMap;

import abstractation.Connection;
import abstractation.Log;
import jdbcimpl.JDBCConnection;
import related.FirstTestMigrator;
import related.SampleRow;
import related.SecondTestMigrator;
import related.TableWithKey;
import table.StoredTable;

import static org.junit.Assert.assertEquals;

public class MigratorTest {
  @Test
  public void testUpgrade() throws Exception {
    Log.logAt(Log.Level.VERBOSE);
    Connection con = new JDBCConnection(FirstTestMigrator.class);
    con.open();
    Migrator mg = new SecondTestMigrator();
    mg.perform(con);
    HashMap<String, String[]> exi = Migrator.getExisting(con, null);
    String[] cols = exi.get("samplerow");
    assertEquals("adjustedint", cols[0]);
    assertEquals("newstring", cols[1]);
    assertEquals("thestring", cols[2]);
    cols = exi.get("tablewithkey");
    assertEquals("thekey", cols[0]);
    assertEquals("thenumber", cols[1]);
    Log.logAt(Log.Level.NONE);
  }

  @Test
  public void testGetExisting() throws Exception {
    // This will need adjusting when migrations are done
    Connection con = new JDBCConnection(null);
    con.open();
    con.loadTables(SampleRow.class, TableWithKey.class);
    StoredTable st = con.getTable(SampleRow.class);
    StoredTable tk = con.getTable(TableWithKey.class);
    con.sqlWithoutResult(st.createStatement(), new Object[]{});
    con.sqlWithoutResult(tk.createStatement(), new Object[]{});
    FirstTestMigrator mg = new FirstTestMigrator();
    HashMap<String, String[]> exi = Migrator.getExisting(con, null);
    String[] cols = exi.get("samplerow");
    assertEquals("thestring", cols[0]);
    assertEquals("thedouble", cols[1]);
    assertEquals("thefloat", cols[2]);
    cols = exi.get("tablewithkey");
    assertEquals("thekey", cols[0]);
    assertEquals("thenumber", cols[1]);
  }
}