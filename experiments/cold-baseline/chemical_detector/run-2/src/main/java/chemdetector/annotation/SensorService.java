package chemdetector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a sensor service exposing zero-arg sensor methods
 * to be lifted into the RoboChart {@code Sensors} interface. The ETL
 * recognises this annotation when the Java class name does not contain
 * the substring "sensor".
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SensorService {
}
