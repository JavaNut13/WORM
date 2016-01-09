package example;

import database.annotations.Stored;
import database.annotations.Table;

/**
 * Created by will on 9/01/16.
 */
@Table
public class OtherTable {
  @Stored public int rowid;
  @Stored public int thing;
  @Stored public String values;
}
