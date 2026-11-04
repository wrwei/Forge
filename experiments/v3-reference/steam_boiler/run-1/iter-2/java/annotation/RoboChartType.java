package steam_boiler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The RoboChart type of a field, parameter or method return value
 * ({@code "real"} for quantities in litres or litres per second,
 * {@code "nat"} for counts and pump numbers).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
