package abstractation.migrations;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import abstractation.Connection;
import abstractation.QueryGenerator;
import abstractation.migrations.annotations.Add;
import abstractation.migrations.annotations.Adjust;
import abstractation.migrations.annotations.Remove;
import annotations.Stored;
import annotations.Table;
import table.Column;
import table.StoredTable;

@Table(name = "schema_migration")
public class Migration {
  @Stored
  public int id;
  @Stored
  public long timestamp;
  @Stored
  public String migratorName;

  private Class table;
  private Type operation;
  private String oldName;

  public Migration(Class table, Type operation) {
    this(table);
    this.operation = operation;
  }

  public Migration(String oldName, Type operation, Class table) {
    this(table, operation);
    this.oldName = oldName;
  }

  public Migration(Class table) {
    this.table = table;
  }

  public boolean run(Connection con, HashMap<String, String[]> existing) throws SQLException {
    StoredTable tab = con.getTable(table);
    if (operation == null) {
      String select = getAlterSelect(tab, existing);
      if (select == null) {
        return false;
      }
      String tmp = tab.name + "_tmp";
      con.sqlWithoutResult(QueryGenerator.renameTable(tab.name, tmp));
      con.sqlWithoutResult(QueryGenerator.createTableAs(select, tab.name, tmp));
      con.sqlWithoutResult(QueryGenerator.dropTable(tmp));
    } else if (operation == Type.DROP) {
      if (existing.containsKey(tab.name)) {
        con.sqlWithoutResult(QueryGenerator.dropTable(tab.name));
      } else {
        throw new SQLException(tab.name + " doesn't exist so it can't be dropped");
      }
    } else if (operation == Type.RENAME) {
      if (existing.containsKey(tab.name)) {
        con.sqlWithoutResult(QueryGenerator.renameTable(oldName, tab.name));
      } else {
        throw new SQLException(tab.name + " doesn't exist so it can't be dropped");
      }
    }
    return true;
  }

  private String getAlterSelect(StoredTable table, HashMap<String, String[]> existing) throws SQLException {
    StringBuilder sb = new StringBuilder();
    HashSet<String> columns = new HashSet<>();
    columns.addAll(Arrays.asList(existing.get(table.name)));
    boolean first = true;
    boolean changed = false;
    for (Field field : getClass().getDeclaredFields()) {
      Add add = field.getAnnotation(Add.class);
      if (add != null) {
        // we're adding a column
        String name = field.getName().toLowerCase();
        if (existing.containsKey(name)) {
          continue;
        }
        changed = true;
        String type = Column.Type.fromClass(field.getType()).asSql();
        if (!first) sb.append(',');
        first = false;
        sb.append("cast(");
        sb.append(add.as().toLowerCase());
        sb.append(" AS ");
        sb.append(type);
        sb.append(") AS ");
        sb.append('`');
        sb.append(name);
        sb.append('`');
      } else if (field.getAnnotation(Remove.class) != null) {
        // We're removing the column
        changed |= columns.remove(field.getName().toLowerCase());
      } else {
        Adjust an = field.getAnnotation(Adjust.class);
        if (an != null) {
          String old = an.old().toLowerCase();
          if (columns.contains(old)) {
            String name = field.getName().toLowerCase();
            changed = true;
            // Renaming/ casting a column
            String type = Column.Type.fromClass(field.getType()).asSql();
            if (!first) sb.append(',');
            first = false;
            sb.append("cast(");
            sb.append(old);
            sb.append(" AS ");
            sb.append(type);
            sb.append(") AS ");
            sb.append('`');
            sb.append(name);
            sb.append('`');
            columns.remove(old);
          }
        }
      }
    }
    if (!changed) {
      return null;
    }
    for (String name : columns) {
      sb.append(',');
      sb.append('`');
      sb.append(name);
      sb.append('`');
    }
    return sb.toString();
  }

  public Class getTable() {
    return table;
  }

  public enum Type {
    RENAME, DROP
  }

  public void setValues(int id, String migrator) {
    this.id = id;
    this.migratorName = migrator;
    this.timestamp = System.currentTimeMillis();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" (");
    boolean first = true;
    for (Field f : getClass().getDeclaredFields()) {
      if (!first) sb.append(", ");
      first = false;
      if (f.getAnnotation(Add.class) != null) {
        sb.append("add ");
      } else if (f.getAnnotation(Remove.class) != null) {
        sb.append("remove ");
      } else {
        Adjust adj = f.getAnnotation(Adjust.class);
        if (adj != null) {
          sb.append("adjust ");
          sb.append(adj.old());
          sb.append(' ');
        } else {
          sb.append("no-op ");
        }
      }
      sb.append(f.getType().getSimpleName());
      sb.append(' ');
      sb.append(f.getName());
    }
    sb.append(')');
    return sb.toString();
  }
}
