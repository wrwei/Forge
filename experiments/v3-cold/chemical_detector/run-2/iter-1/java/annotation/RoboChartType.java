package chemical_detector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Names the RoboChart type of a field, parameter or method return value
 * ({@code "real"} for doubles, {@code "nat"} for non-negative integers).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    /** The RoboChart type name. */
    String value();
}
