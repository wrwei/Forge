package lre.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Java field, parameter, or method return type with the RoboChart
 * type it represents. Consumed by the Spoon/Epsilon transformation pipeline
 * to disambiguate Java primitive types (e.g., {@code int} as {@code nat}
 * vs. {@code int}, {@code double} as {@code real}).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
