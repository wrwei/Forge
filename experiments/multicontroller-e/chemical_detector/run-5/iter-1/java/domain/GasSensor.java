package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (CD-DM6): the chemical identity {@code c} (CD-DM4, compared by
 * equality only) and the measured intensity {@code i} (CD-DM5, totally ordered).
 */
public record GasSensor(@RoboChartType("nat") int c, @RoboChartType("real") double i) {
}
