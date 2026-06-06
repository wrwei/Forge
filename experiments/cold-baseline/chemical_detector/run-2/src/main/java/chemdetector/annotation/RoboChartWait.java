package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a RoboChart {@code wait} primitive (a timed
 * delay). The ETL maps any call to a {@code @RoboChartWait}-annotated
 * method to a RoboChart {@code wait(N)} action regardless of the Java
 * method name.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface RoboChartWait {
}
