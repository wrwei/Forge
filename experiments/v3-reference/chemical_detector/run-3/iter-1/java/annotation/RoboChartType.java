package chemical_detector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Names the RoboChart type of a field, parameter or return value whose Java
 * type alone does not fix it (for example {@code real} for a {@code double}).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    /** The RoboChart type name, such as {@code "real"} or {@code "nat"}. */
    String value();
}
