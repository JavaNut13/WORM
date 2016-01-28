package abstractation;

public class QueryGenerator {
  public static String update(String from, String where, String set) {
    return "UPDATE " + from
        + " SET " + set
        + (where == null ? "" : " WHERE " + where) + ";";
  }

  public static String drop(String from, String where) {
    return "DELETE FROM " + from + (where == null ? "" : " WHERE " + where) + ";";
  }

  public static String query(String select, String from, String where, String group, String order, Integer limit) {
    return "SELECT " + (select == null ? "*" : select)
        + (from == null ? "" : " FROM " + from)
        + (where == null ? "" : " WHERE " + where)
        + (group == null ? "" : " GROUP BY " + group)
        + (order == null ? "" : " ORDER BY " + order)
        + (limit == null ? "" : " LIMIT " + Integer.toString(limit));
  }

  public static String dropTable(String tableName) {
    return "DROP TABLE IF EXISTS " + tableName + ";";
  }

  public static String renameTable(String old, String newName) {
    return "ALTER TABLE " + old + " RENAME TO " + newName + ";";
  }

  public static String createTableAs(String select, String table, String old) {
    return "CREATE TABLE " + table + " AS SELECT " + select + " FROM " + old + ";";
  }
}
