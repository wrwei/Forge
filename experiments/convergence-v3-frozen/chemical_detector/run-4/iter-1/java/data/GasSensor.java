package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: the chemical identity {@code c} (an opaque species id,
 * compared by equality only) and the measured intensity {@code i} (totally ordered).
 */
public record GasSensor(@RoboChartType("nat") int c, @RoboChartType("real") double i) {
}
