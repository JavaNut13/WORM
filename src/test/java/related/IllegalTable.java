package related;

import annotations.Stored;
import annotations.Table;

@Table
public class IllegalTable {
  @Stored private boolean aBoolean;
}
