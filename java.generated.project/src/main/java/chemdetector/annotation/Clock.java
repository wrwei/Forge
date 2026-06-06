package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a clock dependency (the class providing the time source, or the
 * controller field referencing it) so the M2M promotes the elapsed-time
 * field into a RoboChart {@code clock}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Clock {
}
