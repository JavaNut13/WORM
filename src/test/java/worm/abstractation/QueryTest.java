package worm.abstractation;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import related.SampleRow;
import related.TableWithKey;
import worm.jdbcimpl.JDBCConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class QueryTest {
  Connection con;

  @Before
  public void setUp() throws Exception {
    con = new JDBCConnection(null).globalize().open();
    con.loadTables(SampleRow.class, TableWithKey.class);
    con.create(SampleRow.class, TableWithKey.class);
    for(int i = 0; i < 15; i++) {
      SampleRow sr = new SampleRow();
      sr.theString = i % 2 == 0 ? "String" : "Thing";
      sr.theDouble = i / 3.0;
      sr.theFloat = (i * 5) - i;
      con.insert(sr);
      TableWithKey twk = new TableWithKey();
      twk.theNumber = i;
      twk.theKey = "Key" + Integer.toString(i);
      con.insert(twk);
    }
  }

  @Test
  public void testWhere() throws Exception {
    List<SampleRow> items = new Query().from(SampleRow.class).where("thestring=?", "String").all();
    for(SampleRow sr : items) {
      assertEquals("String", sr.theString);
    }
    assertEquals(8, items.size());
    List<TableWithKey> otherItems = new Query().from(TableWithKey.class).where("thenumber < ?", 5).all();
    int i = 0;

    for(TableWithKey twk : otherItems) {
      assertEquals(i, twk.theNumber);
      i++;
    }
    assertEquals(5, otherItems.size());
  }

  @Test
  public void testAnd() throws Exception {
    List<SampleRow> items = new Query()
        .from(SampleRow.class).where("thestring=?", "Thing").and("thedouble<?", 5).all();
    for(SampleRow item : items) {
      assertEquals("Thing", item.theString);
      assertTrue(item.theDouble < 5);
    }
    assertEquals(7, items.size());
  }

  @Test
  public void testOr() throws Exception {
    List<SampleRow> items = new Query()
        .from(SampleRow.class).where("thestring=?", "Thing").or("thestring=?", "String").all();
    assertEquals(15, items.size());
    items = new Query()
        .from(SampleRow.class).where("thestring=?", "Thing").or("thedouble<?", 1).all();
    assertEquals(9, items.size());
  }

  @Test
  public void testGroup() throws Exception {
    SQLResult sqlr = new Query()
        .from(SampleRow.class).select("sum(thedouble)").group("thestring").rawAll();
    sqlr.moveToFirst();
    assertEquals(18, (int) ((double) sqlr.get(1, null)));
    sqlr.moveToNext();
    assertEquals(16, (int) ((double) sqlr.get(1, null)));
  }

  @Test
  public void testOrder() throws Exception {
    List<SampleRow> items = new Query()
        .from(SampleRow.class).order("thedouble DESC").all();
    double prev = 15;
    for(SampleRow item : items) {
      assertTrue(item.theDouble <= prev);
      prev = item.theDouble;
    }
    assertEquals(15, items.size());
  }

  @Test
  public void testSelect() throws Exception {
    List<SampleRow> items = new Query().select("1 as rowid")
        .from(SampleRow.class).all();
    for(SampleRow item : items) {
      assertEquals(1, item.rowid);
    }
    assertEquals(15, items.size());
  }

  @Test
  public void testFrom() throws Exception {
    SQLResult sqlr = new Query(con).select("thenumber, samplerow.rowid").from("tablewithkey, samplerow")
        .where("tablewithkey.thenumber=samplerow.rowid").rawAll();
    sqlr.moveToFirst();
    do {
      assertEquals(sqlr.get("thenumber", -1), sqlr.get("rowid", -2));
    } while(sqlr.moveToNext());
    // Same with in()
    sqlr = new Query(con).select("thenumber, samplerow.rowid").in("tablewithkey, samplerow")
        .where("tablewithkey.thenumber=samplerow.rowid").rawAll();
    sqlr.moveToFirst();
    do {
      assertEquals(sqlr.get("thenumber", -1), sqlr.get("rowid", -2));
    } while(sqlr.moveToNext());
  }

  @Test
  public void testLimit() throws Exception {
    int count = new Query().in(SampleRow.class).limit(5).all().size();
    assertEquals(5, count);
  }

  @Test
  public void testUpdate() throws Exception {
    new Query().from(SampleRow.class).where("thestring=?", "String").update("thedouble=8");
    List<SampleRow> items = new Query().from(SampleRow.class).all();
    for(SampleRow item : items) {
      if(item.theString.equals("String")) {
        assertEquals(8, (int) ((double) item.theDouble));
      } else {
        assertTrue(8 != item.theDouble);
      }
    }
    new Query().from(SampleRow.class).update("thedouble=8");
    int count = new Query().from(SampleRow.class).where("thedouble=?", 8).count();
    assertEquals(15, count);
  }

  @Test
  public void testDrop() throws Exception {
    new Query().from(SampleRow.class).where("thestring=?", "String").drop();
    int count = new Query().from(SampleRow.class).count();
    assertEquals(7, count);
  }

  @Test
  public void testRawAll() throws Exception {
    SQLResult res = new Query().from(SampleRow.class).where("thestring=?", "String").rawAll();
    res.moveToFirst();
    do {
      assertEquals("String", res.get("thestring", null));
    } while(res.moveToNext());
  }

  @Test
  public void testRawFirst() throws Exception {
    SQLResult res = new Query().from(SampleRow.class).where("thestring=?", "String").rawFirst();
    res.moveToFirst();
    assertEquals("String", res.get("thestring", null));
    assertFalse(res.moveToNext());
  }

  @Test
  public void testFirst() throws Exception {
    SampleRow res = new Query().from(SampleRow.class).where("thestring=?", "String")
        .order("thedouble").first();
    assertEquals("String", res.theString);
    assertEquals(0, (int) ((double) res.theDouble));
  }

  @Test
  public void testCount() throws Exception {
    int count = new Query().from("samplerow").count();
    assertEquals(15, count);
  }

  @Test
  public void testMax() throws Exception {
    int max = (int) new Query().from("samplerow").max("rowid");
    assertEquals(15, max);
  }

  @Test
  public void testMin() throws Exception {
    int min = (int) new Query().from("samplerow").min("rowid");
    assertEquals(1, min);
  }

  @Test
  public void testSum() throws Exception {
    int sum = (int) new Query().from("samplerow").sum("rowid");
    assertEquals(120, sum);
  }

  @Test
  public void testScalar() throws Exception {
    String st = (String) new Query().from("samplerow").limit(1).scalar("thestring");
    assertEquals("String", st);
  }

  @Test
  public void testQueryToSql() throws Exception {
    String query = new Query().from("table").where("things=?").and("stuff=?").toSql();
    assertEquals("SELECT * FROM table WHERE (things=?) AND (stuff=?)", query);
    query = new Query().select("stuff").from("table, another").group("stuff").order("stuff ASC").toSql();
    assertEquals("SELECT stuff FROM table, another GROUP BY stuff ORDER BY stuff ASC", query);
    query = new Query().from("table").where("things=?").or("stuff=?").toSql();
    assertEquals("SELECT * FROM table WHERE (things=?) OR (stuff=?)", query);
  }
}