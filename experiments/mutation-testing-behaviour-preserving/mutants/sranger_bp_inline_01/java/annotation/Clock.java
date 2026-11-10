package sranger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or type as the controller's clock dependency.
 *
 * <p>The ETL recognises this annotation (matched by simple name) to identify the clock
 * source used for timed transitions. The annotated field is expected to expose a
 * monotonic "now" method consumed by the controller's timing predicates.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Clock {
}
