package database.abstractation;

import com.sun.istack.internal.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import database.table.Column;

public final class Query {
  private String where = null;
  private ArrayList<Object> args = null;
  private String select = null;
  private String groupBy = null;
  private String orderBy = null;
  private String from = null;
  private final Connection database;
  private Class classType = null;
  private Integer limit = null;

  public Query(String table, Connection database, String select, String where, ArrayList<Object> args, String groupBy, String orderBy) {
    this.from = table;
    this.where = where;
    this.args = args;
    this.select = select;
    this.groupBy = groupBy;
    this.orderBy = orderBy;
    this.database = database;
  }

  public Query(Connection face) {
    this.database = face;
  }

  public Query(Connection database, String table) {
    this.database = database;
    this.from = table;
  }

  public Query() {
    database = Connection.getGlobal();
    Log.v("Query with global database:", database);
  }

  public Query where(String where, Object... args) {
    if(this.where != null) {
      return and(where, args);
    }
    this.where = where;

    this.args = new ArrayList<>(Arrays.asList(args));
    if(args.length == 0) {
      this.args = null;
    }
    return this;
  }

  public Query and(String where, Object... args) {
    this.where = "(" + this.where + ") AND (" + where + ")";
    if(args.length > 0) {
      if(this.args == null || this.args.size() == 0) {
        this.args = new ArrayList<>(Arrays.asList(args));
      } else {
        this.args.addAll(Arrays.asList(args));
      }
    }
    return this;
  }

  public Query or(String where, Object... args) {
    this.where = "(" + this.where + ") OR (" + where + ")";
    if(args.length > 0) {
      if(this.args == null || this.args.size() == 0) {
        this.args = new ArrayList<>(Arrays.asList(args));
      } else {
        this.args.addAll(Arrays.asList(args));
      }
    }
    return this;
  }


  private Query whereRowid(int id) {
    return where("rowid=?", id);
  }

  public Query group(String groupBy) {
    this.groupBy = groupBy;
    return this;
  }


  public Query order(String orderBy) {
    this.orderBy = orderBy;
    return this;
  }


  public Query select(String select) {
    this.select = select;
    return this;
  }


  public Query from(String table) {
    this.from = table;
    return this;
  }

  public Query from(Class... cl) {
    classType = cl[0];
    try {
      StringBuilder sb = new StringBuilder();
      boolean isFirst = true;
      for(Class c : cl) {
        if(!isFirst) {
          sb.append(',');
        }
        isFirst = false;
        sb.append(database.getTable(c).name);
      }
      return from(sb.toString());
    } catch (SQLException sqle) {
      return this;
    }
  }

  public Query in(Class... cl) {
    return from(cl);
  }

  public Query in(String cl) {
    return from(cl);
  }


  public Query limit(int limit) {
    this.limit = limit;
    return this;
  }


  public void update(String assignments, Object... args) throws SQLException {
    if(this.args == null) {
      this.args = new ArrayList<>(Arrays.asList(args));
    } else {
      this.args.addAll(Arrays.asList(args));
    }

    String statement = QueryGenerator.update(select, where, assignments);
    database.sqlWithoutResult(statement, this.args.toArray());
  }


  public void drop() throws SQLException {
    String statement = QueryGenerator.drop(select, where);
    database.sqlWithoutResult(statement, this.args.toArray());
  }

  public <T> ArrayList<T> all() throws SQLException {
    SQLResult res = rawAll();
    ArrayList<T> items = (ArrayList<T>) TableLoader.loadAll(database, classType, res);
    res.close();
    return items;
  }

  public SQLResult rawAll() throws SQLException {
    if(select == null) {
      setSelectFromClassType();
    }
    String statement = QueryGenerator.query(select, from, where, groupBy, orderBy, Integer.toString(limit));
    Log.v("Getting all results:", statement);
    return database.sqlWithResult(statement, args.toArray());
  }

  public SQLResult rawFirst() throws SQLException {
    if(select == null) {
      setSelectFromClassType();
    }
    String statement = QueryGenerator.query(select, from, where, groupBy, orderBy, "1");
    Log.v("Getting single result:", statement);
    return database.sqlWithResult(statement, args == null ? null : args.toArray());
  }

  @Nullable
  public <T> T first() throws SQLException {
    SQLResult res = rawFirst();
    if(res.moveToFirst()) {
      T item = (T) TableLoader.load(database, classType, res);
      res.close();
      return item;
    } else {
      res.close();
      return null;
    }
  }

  public int count() throws SQLException {
    return count("*");
  }

  public int count(String column) throws SQLException {
    return (Integer) scalar("count(" + column + ")");
  }

  public Object max(String column) throws SQLException {
    return scalar("max(" + column + ")");
  }

  public Object min(String column) throws SQLException {
    return scalar("min(" + column + ")");
  }

  public Object sum(String column) throws SQLException {
    return scalar("sum(" + column + ")");
  }

  public Object scalar(String function) throws SQLException {
    Log.v("Running scalar function:", function);
    select = function;
    SQLResult rs = rawFirst();
    Object res = rs.get(1, null);
    rs.close();
    return res;
  }

  private void setSelectFromClassType() {
    if(classType == null) {
      select = "*";
    } else {
      try {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for(Column col : database.getTable(classType).columns) {
          if(!isFirst) {
            sb.append(',');
          }
          isFirst = false;
          sb.append(col.escapedName());
        }
        select = sb.toString();
      } catch (SQLException sqle) {
        sqle.printStackTrace();
      }
    }
  }
}
