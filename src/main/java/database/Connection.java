package database;

import java.io.File;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import database.annotations.Table;
import example.MyTable;
import example.OtherTable;


public class Connection {
  private java.sql.Connection connection;
  private Statement runningStatement;
  private static HashMap<String, StoredTable> tables;
  private static Connection globalDatabase = null;
  private String path;

  public static void main(String[] args) {
    load(MyTable.class, OtherTable.class);

    try {
      Connection cn = new Connection("/Users/will/Desktop/out.db").open().globalize();
      cn.create(MyTable.class);
      MyTable mt = new MyTable("Geoff");
      mt.age = 40;
      mt.save();
      System.out.println(mt);
      System.out.println(mt.rowid);
      mt.age = 60;
      mt.save();
    } catch (SQLException sqle) {
      sqle.printStackTrace();
    }


  }

  public static void load(Class... tables) {
    if(Connection.tables == null) {
      Connection.tables = new HashMap<>();
    }
    for(Class table : tables) {
      if(table.getAnnotation(Table.class) != null) {
        StoredTable tab = new StoredTable(table);
        Connection.tables.put(tab.name, tab);
      }
    }
    System.out.println(Connection.tables);
  }

  public void create(Class cl) throws SQLException {
    StoredTable tbl = getTable(cl);
    Statement stmt = connection.createStatement();
    System.out.println(tbl.createStatement());
    stmt.execute(tbl.createStatement());
    stmt.close();
  }

  public Connection() {
    path = ":memory:";
  }


  public Connection(File location) {
    path = location.getAbsolutePath();
  }

  public Connection(String location) {
    path = location;
  }

  public Connection globalize() {
    globalDatabase = this;
    return this;
  }

  public static Connection getGlobal() {
    return globalDatabase;
  }

  public Connection open() throws SQLException {
    close();
    connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    return this;
  }

  public boolean close() {
    if(connection != null) {
      try {
        connection.close();
        connection = null;
        return true;
      } catch (SQLException sqe) {
        // Meh..
      }

    }
    return false;
  }


  public boolean isClosed() {
    return connection == null;
  }

  public ResultSet sqlWithResult(String sql, Object[] args) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(sql);
    addArgsToStatement(stmt, args);
    ResultSet rs = stmt.executeQuery();
    return rs;
  }

  public void save(Row obj) throws SQLException {
    String name = StoredTable.getTableName(obj.getClass());
    StoredTable table = Connection.tables.get(name);
    if(table == null) {
      throw new SQLException("Table isn't a table");
    }
    if(table.usesRowid) {
      try {
        int rowid = (Integer) table.keys[0].field.get(obj);
        if(rowid == 0) {
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
    StoredTable table = Connection.tables.get(name);
    if(table == null) {
      throw new SQLException("Table isn't a table");
    }
    insert(table, obj);
  }

  private void insert(StoredTable table, Object obj) throws SQLException {
    StringBuilder sb = new StringBuilder();
    StringBuilder params = new StringBuilder();
    sb.append("INSERT INTO ");
    sb.append(table.name);
    sb.append('(');
    int len = table.columns.length;
    if(table.usesRowid) {
      len -= 1;
    }
    Object[] args = new Object[len];
    int moveBack = 0;
    try {
      for(int i = 0; i < table.columns.length; i++) {
        Column col = table.columns[i];
        if(table.usesRowid && col.name.equals("rowid")) {
          moveBack += 1;
          continue;
        }
        if(i - moveBack != 0) {
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

    System.out.println(sb.toString());

    PreparedStatement stmt = connection.prepareStatement(sb.toString());
    addArgsToStatement(stmt, args);
    stmt.executeUpdate();
    if(table.usesRowid) {
      ResultSet rs = stmt.getGeneratedKeys();
      int id = rs.getInt("last_insert_rowid()");
      rs.close();
      try {
        table.keys[0].field.set(obj, id);
      } catch (IllegalAccessException iae) {
        iae.printStackTrace();
      }
    }
    stmt.close();
  }

  public void update(Object obj) throws SQLException {
    String name = StoredTable.getTableName(obj.getClass());
    StoredTable table = Connection.tables.get(name);
    if(table == null) {
      throw new SQLException("Table isn't a table");
    }
    update(table, obj);
  }

  private void update(StoredTable table, Object obj) throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE ");
    sb.append(table.name);
    sb.append(" SET ");
    int len = table.columns.length;
    if(!table.usesRowid) {
      len += table.keys.length;
    }
    Object[] args = new Object[len];

    try {
      int moveBack = 0;
      for(int i = 0; i < table.columns.length; i++) {
        Column col = table.columns[i];
        if(col.name.equals("rowid")) {
          moveBack += 1;
          continue;
        }
        if(i - moveBack != 0) {
          sb.append(',');
        }
        args[i - moveBack] = col.field.get(obj);
        sb.append(col.name);
        sb.append("=?");
      }
      sb.append(" WHERE ");
      int startIndex = table.columns.length - moveBack;
      for(int i = 0; i < table.keys.length; i++) {
        Column key = table.keys[i];
        if(i != 0) {
          sb.append(" AND ");
        }
        sb.append(key.name);
        sb.append("=?");
        args[startIndex + i] = key.field.get(obj);
      }
    } catch (IllegalAccessException iae) {
      iae.printStackTrace();
      return;
    }
    sb.append(';');
    System.out.println(sb.toString());
    PreparedStatement stmt = connection.prepareStatement(sb.toString());
    addArgsToStatement(stmt, args);
    for(Object arg : args) {
      System.out.println(arg);
    }
    stmt.executeUpdate();
    stmt.close();
  }

  public void sqlWithoutResult(String sql, Object[] args) throws SQLException {
    PreparedStatement stmt = connection.prepareStatement(sql);
    addArgsToStatement(stmt, args);
    stmt.execute();
    stmt.close();
  }

  /**
   * Cancel the currently running statement.
   *
   * @return If the statement was cancelled.
   */
  public boolean cancel() {
    if(runningStatement != null) {
      try {
        runningStatement.cancel();
        runningStatement = null;
        return true;
      } catch (SQLException sven) {
        sven.printStackTrace();
        return false;
      }
    } else {
      return false;
    }
  }

  private static void addArgsToStatement(PreparedStatement stmt, Object[] args) throws SQLException {
    int start = 1;
    for(Object arg : args) {
      if(arg instanceof Integer) {
        stmt.setInt(start, (Integer) arg);
      } else if(arg instanceof String) {
        stmt.setString(start, (String) arg);
      } else if(arg instanceof Float) {
        stmt.setFloat(start, (Float) arg);
      } else if(arg instanceof Long) {
        stmt.setLong(start, (Long) arg);
      } else if(arg instanceof Boolean) {
        stmt.setBoolean(start, (Boolean) arg);
      } else if(arg instanceof Double) {
        stmt.setDouble(start, (Double) arg);
      }
      start += 1;
    }
  }

  private static StoredTable getTable(Class cl) throws SQLException {
    String name = StoredTable.getTableName(cl);
    StoredTable table = Connection.tables.get(name);
    if(table == null) {
      throw new SQLException("Table isn't a table");
    }
    return table;
  }
}
