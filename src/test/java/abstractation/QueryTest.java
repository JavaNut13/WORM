package abstractation;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import jdbcimpl.JDBCConnection;
import related.SampleRow;
import related.TableWithKey;

import static org.junit.Assert.*;


public class QueryTest {
  Connection con;

  @Before
  public void setUp() throws Exception {
    con = new JDBCConnection().globalize().open();
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
    ArrayList<SampleRow> items = new Query().from(SampleRow.class).where("thestring=?", "String").all();
    for(SampleRow sr : items) {
      assertEquals("String", sr.theString);
    }
    assertEquals(8, items.size());
    ArrayList<TableWithKey> otherItems = new Query().from(TableWithKey.class).where("thenumber < ?", 5).all();
    int i = 0;

    for(TableWithKey twk : otherItems) {
      assertEquals(i, twk.theNumber);
      i++;
    }
    assertEquals(5, otherItems.size());
  }

  @Test
  public void testAnd() throws Exception {
    ArrayList<SampleRow> items = new Query()
        .from(SampleRow.class).where("thestring=?", "Thing").and("thedouble<?", 5).all();
    for(SampleRow item : items) {
      assertEquals("Thing", item.theString);
      assertTrue(item.theDouble < 5);
    }
    assertEquals(7, items.size());
  }

  @Test
  public void testOr() throws Exception {
    ArrayList<SampleRow> items = new Query()
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
    ArrayList<SampleRow> items = new Query()
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
    ArrayList<SampleRow> items = new Query().select("1 as rowid")
        .from(SampleRow.class).all();
    for(SampleRow item : items) {
      assertEquals(1, item.rowid);
    }
    assertEquals(15, items.size());
  }

  @Test
  public void testFrom() throws Exception {

  }

  @Test
  public void testFrom1() throws Exception {

  }

  @Test
  public void testIn() throws Exception {

  }

  @Test
  public void testIn1() throws Exception {

  }

  @Test
  public void testLimit() throws Exception {

  }

  @Test
  public void testUpdate() throws Exception {

  }

  @Test
  public void testDrop() throws Exception {

  }

  @Test
  public void testAll() throws Exception {

  }

  @Test
  public void testRawAll() throws Exception {

  }

  @Test
  public void testRawFirst() throws Exception {

  }

  @Test
  public void testFirst() throws Exception {

  }

  @Test
  public void testCount() throws Exception {

  }

  @Test
  public void testCount1() throws Exception {

  }

  @Test
  public void testMax() throws Exception {

  }

  @Test
  public void testMin() throws Exception {

  }

  @Test
  public void testSum() throws Exception {

  }

  @Test
  public void testScalar() throws Exception {

  }
}