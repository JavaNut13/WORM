package related;

import abstractation.migrations.Migration;
import abstractation.migrations.Migrator;
import abstractation.migrations.annotations.Add;
import abstractation.migrations.annotations.Adjust;
import abstractation.migrations.annotations.Remove;

/**
 * Created by will on 24/01/16.
 */
public class TestMigrator extends Migrator {
  protected void upgrade() {
    migrate(new Migration(SampleRow.class) {
      @Add String newColumn;
      @Remove double theDouble;
      @Adjust(old="column") double newDouble;
    });
  }

  protected Class[] tables() {
    return new Class[] {
        SampleRow.class,
        TableWithKey.class
    };
  }
}
