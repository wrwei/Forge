package sranger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for the RoboChart primitive type a Java declaration represents.
 *
 * <p>Consumed by the downstream Spoon AST -> RoboChart EMF transformation. Apply only to
 * field declarations, method parameters, and method return types. Never apply to local
 * variables or generic type arguments.</p>
 *
 * <p>Common values: {@code "nat"} (non-negative {@code int}); {@code "real"} ({@code double}).</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
