package database;

import java.util.ArrayList;

import database.annotations.Table;
import example.MyTable;
import example.OtherTable;

/**
 * Created by will on 9/01/16.
 */
public class Connection {
  public static void main(String[] args) {
//    System.out.println("Hello world");
//    Class cl = MyTable.class;
//    Field[] fields = cl.getDeclaredFields();
//    MyTable ex = new MyTable("Geoff");
//
//    for(Field field : fields) {
//      System.out.println(field);
//      Stored st = field.getAnnotation(Stored.class);
//      System.out.println(st);
//      try {
//        System.out.println(field.get(ex));
//        field.set(ex, 100);
//      } catch(IllegalAccessException iae) {
//        iae.printStackTrace();
//      }
//    }
//    System.out.println(ex);
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
      System.out.println(table.createStatement());
    }

  }
}
