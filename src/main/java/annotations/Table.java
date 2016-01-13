package annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class that will be stored as a table
 */
@Documented
@Target({ElementType.TYPE})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
  /**
   * Name of the table to use. Defaults to empty string, and the simple class name will be used
   * @return Table name
   */
  String name() default "";

  /**
   * Column names that are used to identify this row.
   * If this is not empty then the single key is ignored.
   * The names should be the name of the field in the class, not the column name if these are different
   * @return Keys to identify this row, empty if single key is being used
   */
  String[] keys() default {};

  /**
   * Specify a single key, defaults to `rowid`.
   * This must be the name of the field, not column.
   * There must be some field to store rowid in, ie:
   * `@Stored public int rowid;`
   * @return the single key to identify this row
   */
  String key() default "rowid";
}
