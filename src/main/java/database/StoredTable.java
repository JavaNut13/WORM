package database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import database.annotations.Stored;
import database.annotations.Table;

/**
 * Holds schema information about a table and its columns
 */
public class StoredTable {
  public Column[] columns;
  public String name;
  public Column[] keys;
  Class stored;

  /**
   * Create a table from a class that *must* have an @Table annotation.
   * Gets array of all stored properties and their types
   *
   * @param table Class to record details of
   */
  public StoredTable(Class table) {
    stored = table;
    ArrayList<Column> columns = new ArrayList<>();
    ArrayList<Column> keys = new ArrayList<>();
    Table tableAnnotation = (Table) table.getAnnotation(Table.class);

    HashSet<String> keyNames = new HashSet<>();
    if(tableAnnotation.keys().length == 0) {
      keyNames.add(tableAnnotation.key());
    } else {
      keyNames.addAll(Arrays.asList(tableAnnotation.keys()));
    }

    name = tableAnnotation.name().toLowerCase();
    if(name.equals("")) {
      name = table.getSimpleName().toLowerCase();
    }
    for(Field field : table.getFields()) {
      Stored storedAnnotation = field.getAnnotation(Stored.class);
      if(storedAnnotation != null) {
        Column col = new Column(field);
        columns.add(col);
        if(keyNames.contains(col.name)) {
          keys.add(col);
        }
      }
    }
    this.columns = columns.toArray(new Column[columns.size()]);
    this.keys = keys.toArray(new Column[keys.size()]);
  }

  /**
   * SQL Statement to create this table
   * @return SQL CREATE statement to make a table with this name and columns. Does not drop if exists
   */
  public String createStatement() {
    return "CREATE TABLE " + name + " (" + columnsCommaSeparated() + ");";
  }

  /**
   * String representation of this table and it's columns
   * @return String representation of the table
   */
  @Override
  public String toString() {
    return name + " (" + columnsCommaSeparated() + ") keys: " + keysCommaSeparated();
  }

  private String columnsCommaSeparated() {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for(Column column : columns) {
      sb.append(column);
      if(i++ < columns.length - 1) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }

  private String keysCommaSeparated() {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for(Column column : keys) {
      sb.append(column);
      if(i++ < keys.length - 1) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }
}