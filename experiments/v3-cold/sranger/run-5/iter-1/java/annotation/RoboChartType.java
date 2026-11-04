package sranger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the RoboChart type of a field, parameter or method return value
 * (for example {@code "real"} for a Java {@code double}).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    /** The RoboChart type name, such as {@code "real"} or {@code "nat"}. */
    String value();
}
