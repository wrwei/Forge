package chemdetector.datatype;

import chemdetector.annotation.RoboChartType;

/**
 * CD-DM5: totally-ordered sensor-reading strength. Ordering is via goreq (CD-Fn4).
 */
public record Intensity(@RoboChartType("real") double value) {
}
