package worm.table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import worm.annotations.Stored;
import worm.annotations.Table;

/**
 * Holds schema information about a worm.table and its columns
 */
public class StoredTable {
  public Column[] columns;
  public String name;
  public Column[] keys;
  public final boolean usesRowid;


  /**
   * Create a worm.table from a class that *must* have an @Table annotation.
   * Gets array of all stored properties and their types
   *
   * @param table Class to record details of
   */
  public StoredTable(Class table) {
    ArrayList<Column> columns = new ArrayList<>();
    ArrayList<Column> keys = new ArrayList<>();
    Table tableAnnotation = (Table) table.getAnnotation(Table.class);

    HashSet<String> keyNames = new HashSet<>();
    if (tableAnnotation != null) {
      if (tableAnnotation.keys().length == 0) {
        keyNames.add(tableAnnotation.key());
      } else {
        keyNames.addAll(Arrays.asList(tableAnnotation.keys()));
      }
    }

    name = getTableName(table);

    HashSet<Field> fields = new HashSet<>();
    fields.addAll(Arrays.asList(table.getFields()));
    fields.addAll(Arrays.asList(table.getDeclaredFields()));


    for (Field field : fields) {
      Stored storedAnnotation = field.getAnnotation(Stored.class);
      if (storedAnnotation != null) {
        Column col = new Column(field);
        columns.add(col);
        if (keyNames.contains(col.name)) {
          keys.add(col);
        }
      }
    }
    this.columns = columns.toArray(new Column[columns.size()]);
    this.keys = keys.toArray(new Column[keys.size()]);
    this.usesRowid = this.keys.length == 1 && this.keys[0].name.equals("rowid");
  }

  /**
   * SQL Statement to create this worm.table
   *
   * @return SQL CREATE statement to make a worm.table with this name and columns. Does not drop if exists
   */
  public String createStatement() {
    return "CREATE TABLE " + name + " (" + columnsCommaSeparated() + ");";
  }

  /**
   * String representation of this worm.table and it's columns
   *
   * @return String representation of the worm.table
   */
  @Override
  public String toString() {
    return "Table(" + name + ")";
  }

  private String columnsCommaSeparated() {
    StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for (Column column : columns) {
      if (column.name.equals("rowid")) {
        continue;
      }
      if (!isFirst) {
        sb.append(',');
      }
      isFirst = false;
      sb.append(column.escapedName());
      sb.append(' ');
      sb.append(column.type.asSql());
    }
    return sb.toString();
  }

  public static String getTableName(Class cl) {
    String name = null;
    Annotation an = cl.getAnnotation(Table.class);
    if (an != null) {
      Table tab = (Table) an;
      name = tab.name().toLowerCase();
      if (name.equals("")) {
        name = cl.getSimpleName().toLowerCase();
      }
    }
    return name;
  }
}