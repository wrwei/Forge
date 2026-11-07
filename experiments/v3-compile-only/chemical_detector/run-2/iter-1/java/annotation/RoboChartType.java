package chemical_detector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the RoboChart type of a field, parameter or method return value
 * (for example {@code "real"} for intensities, velocities and distances, and
 * {@code "nat"} for durations).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    /** The RoboChart type name. */
    String value();
}
