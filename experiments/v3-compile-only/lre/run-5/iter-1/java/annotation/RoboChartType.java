package lre.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Names the RoboChart type that a Java field, parameter or method return
 * value maps to in the extracted model ("real", "nat", ...).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    /**
     * The RoboChart type name.
     *
     * @return the type name
     */
    String value();
}
