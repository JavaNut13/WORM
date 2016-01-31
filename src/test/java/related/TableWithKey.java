package related;

import worm.annotations.Stored;
import worm.annotations.Table;

@Table(key="thekey")
public class TableWithKey {
  @Stored public String theKey;
  @Stored public int theNumber;
  @Stored public long theLong;
  @Stored public boolean aBoolean;
}
