package steam_boiler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The RoboChart type a field, parameter or return value takes in the extracted model. */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    /** The RoboChart type name, for example {@code "real"} or {@code "nat"}. */
    String value();
}
