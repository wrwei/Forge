package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a timing primitive equivalent to the canonical
 * {@code wait(n)} / {@code pause(n)} / {@code delay(n)} construct.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface RoboChartWait {
}
