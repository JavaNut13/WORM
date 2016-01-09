package database;

import java.util.ArrayList;

import database.annotations.Table;
import example.MyTable;
import example.OtherTable;


public class Connection {
  public static void main(String[] args) {
    initialise(MyTable.class, OtherTable.class);
  }

  private static Object[] tables;

  public static void initialise(Class ...tables) {
    ArrayList<StoredTable> storedTables = new ArrayList<>();
    for(Class table : tables) {
      if(table.getAnnotation(Table.class) != null) {
        storedTables.add(new StoredTable(table));
      }
    }
    for(StoredTable table : storedTables) {
      System.out.println(table);
    }

  }
}
