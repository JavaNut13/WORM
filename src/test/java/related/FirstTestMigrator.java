package related;

import worm.abstractation.migrations.Migrator;

/**
 * Created by will on 24/01/16.
 */
public class FirstTestMigrator extends Migrator {
  protected void upgrade() {

  }

  protected Class[] tables() {
    return new Class[] {
        SampleRow.class,
        TableWithKey.class
    };
  }
}
