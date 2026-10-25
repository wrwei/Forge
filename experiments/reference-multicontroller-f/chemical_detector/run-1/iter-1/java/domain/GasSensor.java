package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: the chemical identity {@code c} and its measured intensity {@code i}.
 * A reading is an ordered list of these, one per sensing direction.
 */
public record GasSensor(@RoboChartType("nat") int c, @RoboChartType("real") double i) {
}
