package related;

import abstractation.migrations.Migration;
import abstractation.migrations.Migrator;

/**
 * Created by will on 24/01/16.
 */
public class TestMigrator extends Migrator {
  public void upgrade() {
    migrate(new Migration(SampleRow.class, Migration.Type.CREATE));

  }
}
