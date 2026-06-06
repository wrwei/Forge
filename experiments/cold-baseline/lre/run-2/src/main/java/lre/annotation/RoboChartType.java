package lre.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Java field, parameter, or method return type with its corresponding
 * RoboChart type name so the downstream model extractor (Spoon AST -&gt; RoboChart
 * EMF) can map Java primitives onto the RoboChart type system without guessing.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface RoboChartType {
    String value();
}
