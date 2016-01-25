package related;

import abstractation.migrations.Migration;
import abstractation.migrations.Migrator;
import abstractation.migrations.annotations.Add;
import abstractation.migrations.annotations.Adjust;
import abstractation.migrations.annotations.Remove;

public class SecondTestMigrator extends Migrator {
  public void upgrade() {
    migrate(new Migration(SampleRow.class) {
      @Add String newString;
      @Remove int theDouble;
    });

    migrate(new Migration(SampleRow.class) {
      @Adjust(old="theFloat") int adjustedInt;
    });
  }

  public Class[] tables() {
    return new Class[] {
        SampleRow.class,
        TableWithKey.class
    };
  }
}
