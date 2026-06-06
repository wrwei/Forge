package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the RoboChart type a Java field, parameter, or return value
 * represents when the Java type alone is ambiguous (e.g. {@code int} as
 * a RoboChart {@code nat}, {@code double} as a RoboChart {@code real}).
 * Consumed by the Spoon → RoboChart model extraction.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
