package abstractation;

import java.sql.SQLException;
import java.util.HashMap;

import abstractation.migrations.Migrator;
import annotations.Table;
import table.Column;
import table.StoredTable;

/**
 * Handles a connection to a database. Must be subclassed to provide implementation-specific querying methods
 */
public abstract class Connection {
  protected HashMap<String, StoredTable> tables;
  private static Connection globalDatabase = null;
  private Class<? extends Migrator> migrator;

  // MARK: SQL operations
  public abstract void sqlWithoutResult(String sql, Object[] args) throws SQLException;

  public void sqlWithoutResult(String sql) throws SQLException {
    sqlWithoutResult(sql, new Object[] {});
  }

  public abstract SQLResult sqlWithResult(String sql, Object[] args) throws SQLException;

  public SQLResult sqlWithResult(String sql) throws SQLException {
    return sqlWithResult(sql, new Object[] {});
  }

  public abstract int sqlReturningRowid(String sql, Object[] args) throws SQLException;

  public int sqlReturningRowid(String sql) throws SQLException {
    return sqlReturningRowid(sql, new Object[] {});
  }

  public abstract void connect() throws SQLException;

  // MARK: Accessing db operations
  public Connection open() throws SQLException {
    connect();
    // TODO delete this
    if (migrator == null) return this;
    try {
      Migrator m = migrator.newInstance();
      m.perform(this);
    } catch (IllegalAccessException | InstantiationException ise) {
      ise.printStackTrace();
    }
    return this;
  }

  public abstract boolean close();

  public abstract boolean isClosed();

  public Connection(Class<? extends Migrator> m) {
    migrator = m;
  }

  public Connection globalize() {
    globalDatabase = this;
    return this;
  }

  public static Connection getGlobal() {
    return globalDatabase;
  }


  public void save(Row obj) throws SQLException {
    StoredTable table = getTable(obj.getClass());
    if (table.usesRowid) {
      try {
        int rowid = (Integer) table.keys[0].field.get(obj);
        if (rowid == 0) {
          insert(table, obj);
        } else {
          update(table, obj);
        }
      } catch (IllegalAccessException iae) {
        iae.printStackTrace();
      }
    } else {
      throw new SQLException("Bitch I can't save this");
    }
  }

  public void insert(Object obj) throws SQLException {
    String name = StoredTable.getTableName(obj.getClass());
    StoredTable table = tables.get(name);
    if (table == null) {
      throw new SQLException("Table isn't a table");
    }
    insert(table, obj);
  }

  /**
   * Insert an obj into the table specified by table.
   * Override this to provide a more efficient version
   * Will either call sqlWithoutResult() or sqlReturningRowid()
   * depending on whether the table has a rowid column
   *
   * @param table Table that
   * @param obj   Object to insert
   * @throws SQLException If there are any SQL errors
   */
  protected void insert(StoredTable table, Object obj) throws SQLException {
    StringBuilder sb = new StringBuilder();
    StringBuilder params = new StringBuilder();
    sb.append("INSERT INTO ");
    sb.append(table.name);
    sb.append('(');
    int len = table.columns.length;
    if (table.usesRowid) {
      len -= 1;
    }
    Object[] args = new Object[len];
    int moveBack = 0;
    try {
      for (int i = 0; i < table.columns.length; i++) {
        Column col = table.columns[i];
        if (table.usesRowid && col.name.equals("rowid")) {
          moveBack += 1;
          continue;
        }
        if (i - moveBack != 0) {
          sb.append(',');
          params.append(',');
        }
        sb.append(col.name);
        args[i - moveBack] = col.field.get(obj);
        params.append('?');
      }
    } catch (IllegalAccessException iae) {
      iae.printStackTrace();
    }
    sb.append(") VALUES (");
    sb.append(params.toString());
    sb.append(");");

    if (table.usesRowid) {
      int id = sqlReturningRowid(sb.toString(), args);
      try {
        table.keys[0].field.set(obj, id);
      } catch (IllegalAccessException iae) {
        iae.printStackTrace();
      }
    } else {
      sqlWithoutResult(sb.toString(), args);
    }
  }

  public void update(Object obj) throws SQLException {
    String name = StoredTable.getTableName(obj.getClass());
    StoredTable table = tables.get(name);
    if (table == null) {
      throw new SQLException("Table isn't a table");
    }
    update(table, obj);
  }

  /**
   * Update a row in the database. Calls sqlWithoutResult() to run the statement
   * Override this to provide a more efficient implementation.
   *
   * @param table StoredTable that the data will be updated in
   * @param obj   Object that has the values, will have at least one @Stored annotated field
   * @throws SQLException If any SQL fails
   */
  protected void update(StoredTable table, Object obj) throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE ");
    sb.append(table.name);
    sb.append(" SET ");
    int len = table.columns.length;
    if (!table.usesRowid) {
      len += table.keys.length;
    }
    Object[] args = new Object[len];

    try {
      int moveBack = 0;
      for (int i = 0; i < table.columns.length; i++) {
        Column col = table.columns[i];
        if (col.name.equals("rowid")) {
          moveBack += 1;
          continue;
        }
        if (i - moveBack != 0) {
          sb.append(',');
        }
        args[i - moveBack] = col.field.get(obj);
        sb.append(col.name);
        sb.append("=?");
      }
      sb.append(" WHERE ");
      int startIndex = table.columns.length - moveBack;
      for (int i = 0; i < table.keys.length; i++) {
        Column key = table.keys[i];
        if (i != 0) {
          sb.append(" AND ");
        }
        sb.append(key.name);
        sb.append("=?");
        args[startIndex + i] = key.field.get(obj);
      }
    } catch (IllegalAccessException iae) {
      return;
    }
    sb.append(';');
    sqlWithoutResult(sb.toString(), args);
  }

  public void create(Class... cls) throws SQLException {
    for (Class cl : cls) {
      StoredTable tbl = getTable(cl);
      String cr = tbl.createStatement();
      sqlWithoutResult(cr, new Object[] {});
    }
  }

  public StoredTable getTable(Class cl) throws SQLException {
    String name = StoredTable.getTableName(cl);
    StoredTable table = tables.get(name);
    if (table == null) {
      throw new SQLException("Table isn't a table");
    }
    return table;
  }

  private static HashMap<String, StoredTable> load(Class... tables) {
    HashMap<String, StoredTable> hashedTables = new HashMap<>();

    for (Class table : tables) {
      if (table.getAnnotation(Table.class) != null) {
        StoredTable tab = new StoredTable(table);
        hashedTables.put(tab.name, tab);
      }
    }

    return hashedTables;
  }

  public HashMap<String, StoredTable> loadTables(Class... tables) {
    if (this.tables == null) {
      this.tables = new HashMap<>();
    }
    this.tables.putAll(load(tables));
    return this.tables;
  }
}
