package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: chemical {@code c} measured at intensity {@code i}.
 * A multi-sensor reading is an ordered list of these, where the (1-based)
 * position in the list identifies the sensing direction.
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
