package database;

import java.lang.reflect.Field;
import java.util.ArrayList;

import database.annotations.Stored;
import database.annotations.Table;


/*
TODO
implement primary keys and rowid (in @Table annotation)
better storedtable
 */

/**
 * Holds schema information about a table and its columns
 */
public class StoredTable {
  public Column[] columns;
  public String name;

  /**
   * Create a table from a class that *must* have an @Table annotation.
   * Gets array of all stored properties and their types
   *
   * @param table Class to record details of
   */
  public StoredTable(Class table) {
    ArrayList<Column> columns = new ArrayList<>();
    Table tableAnnotation = (Table) table.getAnnotation(Table.class);

    name = tableAnnotation.name().toLowerCase();
    if(name.equals("")) {
      name = table.getSimpleName().toLowerCase();
    }
    for(Field field : table.getFields()) {
      Stored storedAnnotation = field.getAnnotation(Stored.class);
      if(storedAnnotation != null) {
        Column col = storedAnnotation.type();
        col.name = storedAnnotation.name();
        if(col == Column.INFER) {
          col = Column.fromClass(field.getType());
        }
        if(col.name == null || col.name.equals("")) {
          col.name = field.getName();
        }
        col.name = col.name.toLowerCase();
        columns.add(col);
      }
    }
    this.columns = columns.toArray(new Column[columns.size()]);
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
    return name + " (" + columnsCommaSeparated() + ")";
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
}