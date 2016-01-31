package related;

import worm.abstractation.Row;
import worm.annotations.Stored;
import worm.annotations.Table;

@Table
public class SampleRow extends Row {
  @Stored public String theString;
  @Stored public double theDouble;
  @Stored public float theFloat;
}
