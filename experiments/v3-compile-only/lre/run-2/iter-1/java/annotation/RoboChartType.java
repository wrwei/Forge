package lre.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Records the RoboChart type of a field, a parameter or a method's return value.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {

    /**
     * The RoboChart type name, for example {@code "real"} or {@code "nat"}.
     *
     * @return the type name
     */
    String value();
}
