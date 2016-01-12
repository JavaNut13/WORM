package database.abstractation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by will on 11/01/16.
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
  private Integer offset = null;

  /**
   * Create a query. null args are ignored when executed.
   * @param table Table to query from
   * @param database DBInterface to use
   * @param select Content for 'SELECT ...'
   * @param where Content for 'WHERE ...'
   * @param args Values for ?s in WHERE
   * @param groupBy Content for 'GROUP BY ...'
   * @param orderBy Content for 'ORDER BY ...'
   */
  public Query(String table, Connection database, String select, String where, ArrayList<Object> args, String groupBy, String orderBy) {
    this.from = table;
    this.where = where;
    this.args = args;
    this.select = select;
    this.groupBy = groupBy;
    this.orderBy = orderBy;
    this.database = database;
  }

  /**
   * Create a query on given DBInterface
   *
   * @param face DBInterface to use
   */
  public Query(Connection face) {
    this.database = face;
  }

  /**
   * Create a query on a dbinterface and table
   *
   * @param database DB to use
   * @param table Table to query on
   */
  public Query(Connection database, String table) {
    this.database = database;
    this.from = table;
  }

  /**
   * Create an empty query
   */
  public Query() {
    database = Connection.getGlobal();
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
    this.where = "(" + this.where +  ") AND (" + where + ")";
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
    this.where = "(" + this.where +  ") OR (" + where + ")";
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

  public Query from(Class cl) {
    classType = cl;
    try {
      return from(database.getTable(cl).name);
    } catch(SQLException sqle) {
      return this;
    }
  }

  public Query in(Class cl) {
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
    return (ArrayList<T>) TableLoader.loadAll(database, classType, rawAll());
  }

  public SQLResult rawAll() throws SQLException {
    String statement = QueryGenerator.query(select, from, where, groupBy, orderBy, Integer.toString(limit));
    return database.sqlWithResult(statement, args.toArray());
  }

  public SQLResult rawFirst() throws SQLException {
    String statement = QueryGenerator.query(select, from, where, groupBy, orderBy, "1");
    System.out.println(statement);
    return database.sqlWithResult(statement, args == null ? null : args.toArray());
  }

  public <T> T first() throws SQLException {
    return (T) TableLoader.load(database, classType, rawFirst());
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
}
