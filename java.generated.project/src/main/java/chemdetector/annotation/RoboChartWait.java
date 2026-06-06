package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a RoboChart timing primitive ({@code wait}) so the
 * M2M emits a {@code wait(duration)} action regardless of the Java
 * method name.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface RoboChartWait {
}
