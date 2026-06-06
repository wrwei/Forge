package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags a Java field, parameter or method return type with the RoboChart
 * type name it represents (e.g. "nat", "real"). Consumed by the
 * Spoon-based discoverer and the ETL transformation when building the
 * RoboChart EMF model.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
