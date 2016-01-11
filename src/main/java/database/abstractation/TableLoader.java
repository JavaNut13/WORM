package database.abstractation;

import java.sql.SQLException;
import java.util.ArrayList;

import database.table.Column;
import database.table.StoredTable;

public final class TableLoader {

  public static <T> T load(final Connection con, final Class<T> c, SQLResult result) throws SQLException {
    StoredTable table = con.getTable(c);
    try {
      T obj = c.newInstance();
      for(Column column : table.columns) {
        column.field.set(obj, result.get(column.name, null));
      }
      return obj;
    } catch(IllegalAccessException iae) {
      iae.printStackTrace();
    } catch(InstantiationException ie) {
      ie.printStackTrace();
      return null;
    }

    return null;
  }

  public static <T> ArrayList<T> loadAll(final Connection con, final Class<T> c, SQLResult result) throws SQLException {
    if(!result.moveToFirst()) {
      return new ArrayList<>();
    }
    ArrayList<T> items = new ArrayList<>();
    do {
      items.add(load(con, c, result));
    } while(result.moveToNext());
    return items;
  }

}
