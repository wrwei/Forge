package chemdetector.datatype;

import chemdetector.annotation.RoboChartType;

/**
 * Totally-ordered intensity value. Comparison is implemented by the goreq function
 * on the sensor layer (see CD-Fn4).
 */
public record Intensity(@RoboChartType("real") double value) {
}
