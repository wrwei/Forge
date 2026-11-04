package steam_boiler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the RoboChart type that a Java field, parameter or return value is
 * modelled as ({@code "real"} for doubles, {@code "nat"} for non-negative
 * integers).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {

    /**
     * The RoboChart type name.
     *
     * @return the type name, for example {@code "real"} or {@code "nat"}
     */
    String value();
}
