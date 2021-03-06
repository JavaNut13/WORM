package worm.table;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import worm.annotations.Stored;

/**
 * Holds the name and type of a single column in a worm.table
 */
public class Column {
  public Type type;
  public String name;
  public Field field;

  public Column(Field field) {
    Stored storedAnnotation = field.getAnnotation(Stored.class);
    type = storedAnnotation.type();
    if (type == Type.INFER) {
      type = Type.fromClass(field.getType());
    }
    name = storedAnnotation.name();
    if (name.equals("")) {
      name = field.getName().toLowerCase();
    } else {
      name = name.toLowerCase();
    }
    this.field = field;
    // Allow setting private fields
    this.field.setAccessible(true);
  }

  @Override
  public String toString() {
    if (name != null) {
      return name + ("(" + field.getName() + ")") + " " + type.asSql();
    } else {
      return "NONE " + type.asSql();
    }
  }

  public String escapedName() {
    return "`" + name + "`";
  }

  public enum Type {
    TEXT, INTEGER, BOOLEAN, FLOAT, DATE, NONE, INFER;

    /**
     * Create a Column from a class type - used when inferring type from a field
     *
     * @param cl Class to get the type from
     * @return Column with the specified type, or NONE if type is not recognised
     */
    public static Type fromClass(Class cl) {
      if (cl.equals(String.class)) {
        return TEXT;
      } else if (cl.equals(Integer.class) || cl.equals(int.class)) {
        return INTEGER;
      } else if (cl.equals(Float.class) || cl.equals(float.class)) {
        return FLOAT;
      } else if (cl.equals(Double.class) || cl.equals(double.class)) {
        return FLOAT;
      } else if (cl.equals(Boolean.class) || cl.equals(boolean.class)) {
        return BOOLEAN;
      } else if (cl.equals(LocalDateTime.class)) {
        return DATE;
      } else {
        return NONE;
      }
    }

    /**
     * Returns the SQL equivalent of this type
     *
     * @return String that represents this type in SQL
     */
    public String asSql() {
      switch (this) {
        case TEXT:
          return "TEXT";
        case INTEGER:
          return "INTEGER";
        case BOOLEAN:
          return "BOOLEAN";
        case FLOAT:
          return "DECIMAL";
        case DATE:
          return "INTEGER";
        case NONE:
        case INFER:
        default:
          return "";
      }
    }
  }
}