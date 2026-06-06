package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a RoboChart timing primitive: calls to the annotated
 * method are extracted as {@code wait(<duration>)} actions by the M2M
 * transformation, regardless of the Java method name.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface RoboChartWait {
}
