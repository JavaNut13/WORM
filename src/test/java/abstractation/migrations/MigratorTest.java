package abstractation.migrations;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import abstractation.Connection;
import abstractation.Log;
import jdbcimpl.JDBCConnection;
import related.SampleRow;
import related.TableWithKey;
import related.TestMigrator;
import table.StoredTable;

import static org.junit.Assert.assertEquals;

public class MigratorTest {

  @Before
  public void setUp() throws Exception {
    Log.logAt(Log.Level.VERBOSE);

  }


  @Test
  public void testUpgrade() throws Exception {
    Log.e("Migrating");
    Connection con = new JDBCConnection(TestMigrator.class, "temp.db");
    con.open();
  }

  @Test
  @Ignore
  public void testGetExisting() throws Exception {
    // This will need adjusting when migrations are done
    Connection con = new JDBCConnection(null);
    con.open();
    con.loadTables(SampleRow.class, TableWithKey.class);
    StoredTable st = con.getTable(SampleRow.class);
    StoredTable tk = con.getTable(TableWithKey.class);
    con.sqlWithoutResult(st.createStatement(), new Object[]{});
    con.sqlWithoutResult(tk.createStatement(), new Object[]{});
    TestMigrator mg = new TestMigrator();
    HashMap<String, String[]> exi = mg.getExisting(con);
    String[] cols = exi.get("samplerow");
    assertEquals("thestring", cols[0]);
    assertEquals("thedouble", cols[1]);
    assertEquals("thefloat", cols[2]);
    cols = exi.get("tablewithkey");
    assertEquals("thekey", cols[0]);
    assertEquals("thenumber", cols[1]);
  }

  @Test
  public void testPerform() throws Exception {

  }
}