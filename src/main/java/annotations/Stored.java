package annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import table.Column;

/**
 * Annotates a field of a class that will be stored,
 */
@Documented
@Target({ElementType.FIELD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
/**
 * Marks a field on an object to be saved in the database
 * Field must be public
 */
public @interface Stored {
  Column.Type type() default Column.Type.INFER;
  String name() default "";
}
