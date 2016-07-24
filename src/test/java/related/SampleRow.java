package related;

import worm.abstractation.Row;
import worm.annotations.Stored;
import worm.annotations.Table;

@Table
public class SampleRow extends Row {
  @Stored
  public String theString;
  @Stored
  public double theDouble;
  @Stored
  private float theFloat;

  public float getTheFloat() {
    return theFloat;
  }

  public void setTheFloat(float fl) {
    theFloat = fl;
  }
}
