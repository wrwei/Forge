package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the RoboChart type a Java field, parameter, or method return
 * value represents (e.g. {@code "nat"}, {@code "real"}). Consumed by the
 * downstream model extraction (Spoon AST &rarr; RoboChart EMF).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
