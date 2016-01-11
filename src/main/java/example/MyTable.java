package example;

import database.abstractation.Row;
import database.annotations.Stored;
import database.annotations.Table;

/**
 * Created by will on 9/01/16.
 */
@Table
public class MyTable extends Row {
  @Stored public String name;
  @Stored public int age;

  public MyTable(String name) {
    this.name = name;
  }
}
