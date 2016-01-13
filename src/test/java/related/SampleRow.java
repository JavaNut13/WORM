package related;

import abstractation.Row;
import annotations.Stored;
import annotations.Table;

@Table
public class SampleRow extends Row {
  @Stored public String theString;
  @Stored public double theDouble;
  @Stored public float theFloat;
}
