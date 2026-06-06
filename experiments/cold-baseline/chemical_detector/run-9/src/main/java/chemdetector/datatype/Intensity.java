package chemdetector.datatype;

import chemdetector.annotation.RoboChartType;

/**
 * CD-DM5: totally-ordered intensity. Comparison via {@link #goreq}.
 */
public record Intensity(@RoboChartType("real") double value) {
}
