package sranger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RoboChart type hint consumed by the model-extraction pipeline
 * (Spoon AST to RoboChart EMF). Names the RoboChart type a Java
 * field, parameter, or method return value represents (e.g. "nat",
 * "real").
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
