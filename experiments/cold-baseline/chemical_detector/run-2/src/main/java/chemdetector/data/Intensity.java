package chemdetector.data;

import chemdetector.annotation.RoboChartType;

/**
 * CD-DM5: Totally-ordered strength of a sensor reading. Comparison
 * happens via the goreq function on {@link chemdetector.operation.GasFunctions}.
 */
public record Intensity(@RoboChartType("real") double value) {
}
