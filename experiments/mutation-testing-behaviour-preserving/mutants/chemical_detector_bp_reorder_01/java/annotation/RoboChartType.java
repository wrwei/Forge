package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for downstream Spoon/ETL extraction: declares the RoboChart-level type
 * of an annotated field, parameter, or method return type when the Java type
 * alone is ambiguous (e.g. {@code int} representing a natural number).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
