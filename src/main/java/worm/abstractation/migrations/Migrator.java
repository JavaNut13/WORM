package worm.abstractation.migrations;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import worm.abstractation.Connection;
import worm.abstractation.Query;
import worm.abstractation.SQLResult;
import worm.table.StoredTable;

public abstract class Migrator {
  private ArrayList<Migration> migrations;

  public Migrator() {
  }

  protected abstract void upgrade();

  protected abstract Class[] tables();

  protected void migrate(Migration m) {
    migrations.add(m);
  }

  public void perform(Connection con) throws SQLException {
    HashMap<String, StoredTable> tabs = con.loadTables(tables());
    con.loadTables(Migration.class);
    HashMap<String, String[]> existing = getExisting(con, null);
    boolean changed = false;
    for (String tableName : tabs.keySet()) {
      if (!existing.containsKey(tableName)) {
        con.sqlWithoutResult(tabs.get(tableName).createStatement());
        changed = true;
      }
    }
    if (changed) {
      existing = getExisting(con, null);
    }

    Object latest = new Query(con).from(Migration.class).where("migratorname=?", getName()).max("id");
    int latestMigration = -1;
    if (latest != null) {
      latestMigration = (int) latest;
    }
    migrations = new ArrayList<>();
    upgrade();

    Migration m;
    for (int index = latestMigration + 1; index < migrations.size(); index++) {
      m = migrations.get(index);
      try {
        changed = m.run(con, existing);
        m.setValues(index, getName());
        con.insert(m);
        if (changed) {
          String tableName = con.getTable(m.getTable()).name;
          HashMap<String, String[]> updated = getExisting(con, tableName);
          existing.put(tableName, updated.get(tableName));
        }
      } catch (SQLException sqle) {
        sqle.printStackTrace();
      }
    }
  }

  public static HashMap<String, String[]> getExisting(Connection con, String table) throws SQLException {
    SQLResult sqlr;
    if (table == null) {
      sqlr = new Query(con).from("sqlite_master").select("name, sql").rawAll();
    } else {
      sqlr = new Query(con).from("sqlite_master").select("name, sql").where("name=?", table).rawAll();
    }

    HashMap<String, String[]> tables = new HashMap<>();
    if (sqlr.moveToFirst()) {
      ArrayList<String> cols = new ArrayList<>();
      do {
        String statement = (String) sqlr.get("sql", null);
        String name = (String) sqlr.get("name", null);
        String columns = statement.substring(statement.indexOf('(') + 1, statement.length() - 1);
        cols.clear();
        for (String col : columns.split(",")) {
          String colName;
          if (col.charAt(0) == '`') {
            colName = col.substring(1, col.indexOf('`', 1));
          } else {
            col = col.replaceAll("\\s+", " ");
            colName = col.substring(1, col.indexOf(' ', 1));
          }
          cols.add(colName);
        }
        tables.put(name, cols.toArray(new String[cols.size()]));
      } while (sqlr.moveToNext());
    }
    return tables;
  }

  private String getName() {
    return getClass().getName().toLowerCase();
  }
}
