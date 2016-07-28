package datetime;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import worm.abstractation.Connection;
import worm.abstractation.Query;
import worm.abstractation.migrations.Migrator;
import worm.jdbcimpl.JDBCConnection;
import worm.table.Column;
import worm.table.StoredTable;

import static org.junit.Assert.assertEquals;

public class DateTest {

  public static class TestMigrator extends Migrator {
    public TestMigrator() {
    }

    public Class[] tables() {
      return new Class[] {
          TableWithDate.class
      };
    }

    public void upgrade() {

    }
  }

  @Before
  public void setUp() throws Exception {
    Connection con = new JDBCConnection(TestMigrator.class);
    con.open().globalize();
  }

  @Test
  public void testSaveDate() throws Exception {
    TableWithDate twd = new TableWithDate();
    twd.time = LocalDateTime.now();

    twd.save();

    TableWithDate twd1 = new Query().from(TableWithDate.class).first();
    assertEquals(twd.time, twd1.time);
  }

  @Test
  public void testDateType() throws Exception {
    StoredTable tb = Connection.getGlobal().getTable(TableWithDate.class);

    Optional<Column> col = Stream.of(tb.columns)
        .filter(column -> column.name.equals("time"))
        .findFirst();
    assertEquals(col.get().type, Column.Type.DATE);
  }
}
