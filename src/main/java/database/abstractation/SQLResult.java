package database.abstractation;

public interface SQLResult {
  /**
   * Get a value from a column with a given name or return a default value
   * @param column Column name to retrieve value from
   * @param defaultValue Value to return if no value is found
   * @return Retrieved value or default
   */
  Object get(String column, Object defaultValue);

  /**
   * Move to the start of the results
   * @return Whether there is an element at the start
   */
  boolean moveToFirst();

  /**
   * Move to the next row of the result
   * @return true if another row exists
   */
  boolean moveToNext();

  /**
   * Close the result
   */
  void close();
}
