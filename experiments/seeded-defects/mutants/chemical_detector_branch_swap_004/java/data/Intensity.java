package chemdetector.data;

import chemdetector.annotation.RoboChartType;

/**
 * Totally-ordered strength of a sensor reading (CD-DM5). Only comparison is
 * needed; see {@link chemdetector.function.IntensityFunctions#goreq}.
 */
public record Intensity(@RoboChartType("real") double value) {
}
