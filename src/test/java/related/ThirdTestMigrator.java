package related;

import worm.abstractation.migrations.Migration;
import worm.abstractation.migrations.Migrator;

/**
 * Created by will on 28/01/16.
 */
public class ThirdTestMigrator extends Migrator {
  @Override
  public void upgrade() {
    migrate(new Migration(TableWithKey.class, Migration.Type.DROP));

    migrate(new Migration("samplerow", Migration.Type.RENAME, TableWithKey.class));
  }

  @Override
  public Class[] tables() {
    return new Class[] {
        SampleRow.class,
        TableWithKey.class
    };
  }
}
