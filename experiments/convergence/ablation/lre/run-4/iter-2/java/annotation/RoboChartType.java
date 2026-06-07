package lre.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the RoboChart type a Java field, parameter, or method return value
 * represents (e.g. {@code "nat"}, {@code "real"}). Consumed by the
 * Spoon/Epsilon model-extraction pipeline.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
