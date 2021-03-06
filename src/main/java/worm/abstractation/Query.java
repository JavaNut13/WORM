package worm.abstractation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import worm.table.Column;

/**
 * Represents a query to a given database or the global database. Generates SQL that is passed to
 * the database and can either return a list of instantiated objects, or an SQLResult
 */
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

  public Query(Connection face) {
    this.database = face;
  }

  public Query() {
    database = Connection.getGlobal();
  }

  public Query where(String where, Object... args) {
    if (this.where != null) {
      return and(where, args);
    }
    this.where = where;

    this.args = new ArrayList<>(Arrays.asList(args));
    if (args.length == 0) {
      this.args = null;
    }
    return this;
  }

  public Query and(String where, Object... args) {
    this.where = "(" + this.where + ") AND (" + where + ")";
    if (args.length > 0) {
      if (this.args == null || this.args.size() == 0) {
        this.args = new ArrayList<>(Arrays.asList(args));
      } else {
        this.args.addAll(Arrays.asList(args));
      }
    }
    return this;
  }

  public Query or(String where, Object... args) {
    this.where = "(" + this.where + ") OR (" + where + ")";
    if (args.length > 0) {
      if (this.args == null || this.args.size() == 0) {
        this.args = new ArrayList<>(Arrays.asList(args));
      } else {
        this.args.addAll(Arrays.asList(args));
      }
    }
    return this;
  }

  private Query isInHelper(String attribute, Object[] args) {
    String qmarks = new String(new char[args.length]).replace("\0", ",?").substring(1);
    if (where != null) {
      this.where = "(" + where + ") AND (" + attribute + " IN (" + qmarks + "))";
    } else {
      this.where = attribute + " IN (" + qmarks + ")";
    }
    if (this.args == null) {
      this.args = new ArrayList<>(Arrays.asList(args));
    } else {
      this.args.addAll(Arrays.asList(args));
    }
    return this;
  }

  public Query isIn(String attribute, List<?> args) {
    return isInHelper(attribute, args.toArray());
  }

  public Query valuesIn(String attribute, Object... args) {
    return isInHelper(attribute, args);
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
      for (Class c : cl) {
        if (!isFirst) {
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
    if (this.args == null) {
      this.args = new ArrayList<>(Arrays.asList(args));
    } else {
      List<Object> oldArgs = this.args;
      this.args = new ArrayList<>(Arrays.asList(args));
      this.args.addAll(oldArgs);
    }

    String statement = QueryGenerator.update(from, where, assignments);
    database.sqlWithoutResult(statement, this.args.toArray());
  }


  public void drop() throws SQLException {
    String statement = QueryGenerator.drop(from, where);
    database.sqlWithoutResult(statement, args == null ? new Object[0] : args.toArray());
  }

  public <T> List<T> all() throws SQLException {
    SQLResult res = rawAll();
    List<T> items = (List<T>) TableLoader.loadAll(database, classType, res);
    res.close();
    return items;
  }

  public SQLResult rawAll() throws SQLException {
    if (select == null) {
      setSelectFromClassType();
    }
    String statement = QueryGenerator.query(select, from, where, groupBy, orderBy, limit);
    if (args == null) {
      return database.sqlWithResult(statement);
    } else {
      return database.sqlWithResult(statement, args.toArray());
    }
  }

  public <T> List<T> listOf(String columnName) throws SQLException {
    select(columnName);
    SQLResult result = rawAll();
    List<T> list = new ArrayList<>();
    if (result.moveToFirst()) {
      while (result.moveToNext()) {
        list.add((T) result.get(1, null));
      }
    }
    return list;
  }

  public SQLResult rawFirst() throws SQLException {
    if (select == null) {
      setSelectFromClassType();
    }
    String statement = QueryGenerator.query(select, from, where, groupBy, orderBy, 1);
    return database.sqlWithResult(statement, args == null ? null : args.toArray());
  }

  public <T> T first() throws SQLException {
    SQLResult res = rawFirst();
    if (res.moveToFirst()) {
      T item = (T) TableLoader.load(database, classType, res);
      res.close();
      return item;
    } else {
      res.close();
      return null;
    }
  }

  public String toSql() {
    if (select == null) {
      try {
        setSelectFromClassType();
      } catch (SQLException sqle) {
        select = "*";
      }
    }
    return QueryGenerator.query(select, from, where, groupBy, orderBy, limit);
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
    select = function;
    SQLResult rs = rawFirst();
    Object res = rs.get(1, null);
    rs.close();
    return res;
  }

  private void setSelectFromClassType() throws SQLException {
    if (classType == null) {
      select = "*";
    } else {
      StringBuilder sb = new StringBuilder();
      boolean isFirst = true;
      for (Column col : database.getTable(classType).columns) {
        if (!isFirst) {
          sb.append(',');
        }
        isFirst = false;
        sb.append(col.escapedName());
      }
      select = sb.toString();
    }
  }
}
