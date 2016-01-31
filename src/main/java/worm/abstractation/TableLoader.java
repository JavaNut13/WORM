package worm.abstractation;

import java.sql.SQLException;
import java.util.ArrayList;

import worm.table.Column;
import worm.table.StoredTable;

/**
 * Helper methods to instantiate @Table objects
 */
public class TableLoader {

  public static <T> T load(final Connection con, final Class<T> c, SQLResult result) throws SQLException {
    StoredTable table = con.getTable(c);
    try {
      T obj = c.newInstance();
      for (Column column : table.columns) {
        final Object defaultVal = instantiate(column.field.getType());
        final Object val = result.get(column.name, defaultVal);
        column.field.set(obj, convertValue(val, column.field.getType()));
      }
      return obj;
    } catch (IllegalAccessException | InstantiationException ie) {
      ie.printStackTrace();
      return null;
    }
  }

  public static <T> ArrayList<T> loadAll(final Connection con, final Class<T> c, SQLResult result) throws SQLException {
    result.moveToFirst();
    ArrayList<T> items = new ArrayList<>();
    do {
      items.add(load(con, c, result));
    } while (result.moveToNext());
    return items;
  }

  private static Object convertValue(Object value, Class expected) {
    if ((expected == boolean.class || expected == Boolean.class) && value instanceof Integer) {
      // Booleans are actually stored as integers. This is awkward :(
      return (Integer) value != 0;
    } else {
      return value;
    }
  }

  private static Object instantiate(Class cl) {
    switch (cl.getSimpleName()) {
      case "int":
        return 0;
      case "float":
        return 0.0f;
      case "double":
        return 0.0;
      case "boolean":
        return false;
      case "long":
        return 0L;
      default:
        return null;
    }
  }
}
