package database;

/**
 * Holds the name and type of a single column in a table
 */
public enum Column {
  TEXT, INTEGER, BOOLEAN, FLOAT, NONE, INFER;
  public String name;
  public boolean identifies = false;

  /**
   * Returns the SQL equivalent of this type
   * @return String that represents this type in SQL
   */
  public String asSql() {
    switch(this) {
      case TEXT: return "TEXT";
      case INTEGER: return "INTEGER";
      case BOOLEAN: return "INTEGER";
      case FLOAT: return "DECIMAL";
      case NONE:
      case INFER:
      default: return "";
    }
  }

  @Override
  public String toString() {
    if(name != null) {
      return name + " " + asSql();
    } else {
      return "NONE " + asSql();
    }
  }

  /**
   * Create a Column from a class type - used when inferring type from a field
   * @param cl Class to get the type from
   * @return Column with the specified type, or NONE if type is not recognised
   */
  public static Column fromClass(Class cl) {
    if(cl.equals(String.class)) {
      return TEXT;
    } else if(cl.equals(Integer.class) || cl.equals(int.class)) {
      return INTEGER;
    } else if(cl.equals(Float.class) || cl.equals(float.class)) {
      return FLOAT;
    } else if(cl.equals(Double.class) || cl.equals(double.class)) {
      return FLOAT;
    } else if(cl.equals(Boolean.class) || cl.equals(boolean.class)) {
      return BOOLEAN;
    } else {
      return NONE;
    }
  }
}