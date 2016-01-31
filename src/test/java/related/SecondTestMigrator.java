package related;

import worm.abstractation.migrations.Migration;
import worm.abstractation.migrations.Migrator;
import worm.abstractation.migrations.annotations.Add;
import worm.abstractation.migrations.annotations.Adjust;
import worm.abstractation.migrations.annotations.Remove;

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
