package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class (or a field reference to it) as the system clock used
 * by the movement subsystem for stuck detection. The ETL recognises
 * this annotation as a RoboChart clock dependency irrespective of the
 * Java class name.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Clock {
}
