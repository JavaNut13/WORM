package abstractation;

import org.junit.Test;

import java.util.HashMap;

import abstractation.migrations.Migrator;
import jdbcimpl.JDBCConnection;
import related.FirstTestMigrator;
import related.SampleRow;
import related.SecondTestMigrator;
import related.TableWithKey;
import related.ThirdTestMigrator;
import table.StoredTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MigratorTest {
  @Test
  public void testUpgrade() throws Exception {
    Connection con = new JDBCConnection(FirstTestMigrator.class);
    con.open();
    Migrator mg = new SecondTestMigrator();
    mg.perform(con);
    HashMap<String, String[]> exi = Migrator.getExisting(con, null);
    String[] cols = exi.get("samplerow");
    assertEquals("adjustedint", cols[0]);
    assertEquals("thestring", cols[1]);
    assertEquals("newstring", cols[2]);
    cols = exi.get("tablewithkey");
    assertEquals("thekey", cols[0]);
    assertEquals("thenumber", cols[1]);

    // This is kind of nasty
    mg = new ThirdTestMigrator();
    mg.perform(con);
    exi = Migrator.getExisting(con, null);
    assertTrue(exi.containsKey("tablewithkey"));
    assertFalse(exi.containsKey("samplerow"));
    cols = exi.get("tablewithkey");
    assertEquals("adjustedint", cols[0]);
    assertEquals("thestring", cols[1]);
    assertEquals("newstring", cols[2]);
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
    HashMap<String, String[]> exi = Migrator.getExisting(con, null);
    String[] cols = exi.get("samplerow");
    assertEquals("thestring", cols[0]);
    assertEquals("thedouble", cols[1]);
    assertEquals("thefloat", cols[2]);
    cols = exi.get("tablewithkey");
    assertEquals("thekey", cols[0]);
    assertEquals("thenumber", cols[1]);
  }

  @Test
  public void testMultipleMigrates() throws Exception {
    Connection con = new JDBCConnection(FirstTestMigrator.class, "/Users/will/Desktop/out.db");
    con.open();
    Migrator mg = new SecondTestMigrator();
    mg.perform(con);
    mg.perform(con);
    HashMap<String, String[]> exi = Migrator.getExisting(con, null);
    String[] cols = exi.get("samplerow");
    assertEquals("adjustedint", cols[0]);
    assertEquals("thestring", cols[1]);
    assertEquals("newstring", cols[2]);
    cols = exi.get("tablewithkey");
    assertEquals("thekey", cols[0]);
    assertEquals("thenumber", cols[1]);
  }
}