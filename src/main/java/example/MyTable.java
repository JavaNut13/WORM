package example;

import database.Column;
import database.annotations.Stored;
import database.annotations.Table;

/**
 * Created by will on 9/01/16.
 */
@Table(key = "id")
public class MyTable {
  @Stored public int id;
  @Stored public String name;
  @Stored public int age;
  @Stored(type=Column.Type.TEXT, name="geoff") public boolean isTrue;
  public float aFloat;

  public MyTable(String name) {
    this.name = name;
  }
}
