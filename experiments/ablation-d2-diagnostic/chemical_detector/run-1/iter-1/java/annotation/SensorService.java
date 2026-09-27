package chemical_detector.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks the class that provides sensor readings and sensor-derived functions. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SensorService {
}
