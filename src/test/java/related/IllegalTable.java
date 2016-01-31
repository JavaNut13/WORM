package related;

import worm.annotations.Stored;
import worm.annotations.Table;

@Table
public class IllegalTable {
  @Stored private boolean aBoolean;
}
