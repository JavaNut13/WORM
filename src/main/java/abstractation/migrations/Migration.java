package abstractation.migrations;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import abstractation.Connection;
import abstractation.Log;
import abstractation.QueryGenerator;
import abstractation.migrations.annotations.Add;
import abstractation.migrations.annotations.Adjust;
import abstractation.migrations.annotations.Remove;
import table.Column;
import table.StoredTable;

public class Migration {
  private Class table;
  private Type operation;

  public Migration(Class table, Type operation) {
    this(table);
    this.operation = operation;
  }

  public Migration(Class table) {
    this.table = table;
  }

  public void run(Connection con, HashMap<String, String[]> existing) throws SQLException {
    StoredTable tab = con.getTable(table);
    Log.d("Migrating", tab);
    if(operation == null) {
      String select = getAlterSelect(tab, existing);
      if(select == null) {
        return;
      }
      String tmp = tab.name + "_tmp";
      con.sqlWithoutResult(QueryGenerator.renameTable(tab.name, tmp));
      con.sqlWithoutResult(QueryGenerator.createTableAs(select, tab.name, tmp));
      con.sqlWithoutResult(QueryGenerator.dropTable(tmp));
    } else if(operation == Type.DROP) {
      if(existing.containsKey(tab.name)) {
        con.sqlWithoutResult(QueryGenerator.dropTable(tab.name), new Object[]{});
      } else {
        throw new SQLException(tab.name + " doesn't exist so it can't be dropped");
      }
    } else if(operation == Type.CREATE) {
      Log.d("Creating table", tab.name);
      con.sqlWithoutResult(tab.createStatement());
    }
  }

  private String getAlterSelect(StoredTable table, HashMap<String, String[]> existing) throws SQLException {
    StringBuilder sb = new StringBuilder();
    HashSet<String> columns = new HashSet<>();
    columns.addAll(Arrays.asList(existing.get(table.name)));
    Log.d("Columns:", columns);
    boolean first = true;
    boolean changed = false;
    for(Field field : getClass().getDeclaredFields()) {
      Add add = field.getAnnotation(Add.class);
      if(add != null) {
        // we're adding a column
        Log.d("Adding column", field);
        String name = field.getName().toLowerCase();
        if(existing.containsKey(name)) {
          Log.w("Column already exists:", name);
          continue;
        }
        changed = true;
        String type = Column.Type.fromClass(field.getType()).asSql();
        if(!first) sb.append(',');
        first = false;
        sb.append("cast(");
        sb.append(add.as());
        sb.append(" AS ");
        sb.append(type);
        sb.append(") AS ");
        sb.append('`');
        sb.append(name);
        sb.append('`');
      } else if(field.getAnnotation(Remove.class) != null) {
        // We're removing the column
        Log.d("Removing column", field.getName().toLowerCase());
        changed |= columns.remove(field.getName().toLowerCase());
      } else {
        Adjust an = field.getAnnotation(Adjust.class);
        String name = field.getName().toLowerCase();
        if(an != null && existing.containsKey(name)) {
          changed = true;
          // Renaming/ casting a column
          Log.d("Altering column", name);
          String old = an.old();
          String type = Column.Type.fromClass(field.getType()).asSql();
          if(!first) sb.append(',');
          first = false;
          sb.append("cast(");
          sb.append(old);
          sb.append(" AS ");
          sb.append(type);
          sb.append(") AS ");
          sb.append('`');
          sb.append(name);
          sb.append('`');
          existing.remove(name);
        }
      }
    }
    if(!changed) {
      return null;
    }
    for(String name : columns) {
      sb.append(',');
      sb.append('`');
      sb.append(name);
      sb.append('`');
    }
    return sb.toString();
  }

  public enum Type {
    CREATE, DROP
  }
}
