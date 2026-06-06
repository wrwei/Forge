package sranger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Java field, parameter, or method return type with the corresponding
 * RoboChart primitive type name (e.g., "nat", "real").
 *
 * Consumed by the Spoon -> ETL -> RoboChart EMF pipeline.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
