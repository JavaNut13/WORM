package example;

import database.abstractation.Row;
import database.annotations.Stored;
import database.annotations.Table;

@Table
public class OtherTable extends Row {
  @Stored public int thing;
  @Stored public String values;
}
