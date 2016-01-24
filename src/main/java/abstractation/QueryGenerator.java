package abstractation;

/**
 * Created by will on 12/01/16.
 */
public class QueryGenerator {
  public static String update(String from, String where, String set) {
    if(set == null) {
      return null;
    }
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
}
