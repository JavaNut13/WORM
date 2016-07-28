package datetime;

import java.time.LocalDateTime;

import worm.abstractation.Row;
import worm.annotations.Stored;
import worm.annotations.Table;

@Table
public class TableWithDate extends Row {
  @Stored
  public LocalDateTime time;
}
