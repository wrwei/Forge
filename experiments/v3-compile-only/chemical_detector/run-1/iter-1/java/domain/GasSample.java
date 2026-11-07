package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (CD-DM6): the chemical identity {@code c} (CD-DM4,
 * an opaque identifier compared only for equality) and the measured
 * intensity {@code i} (CD-DM5, totally ordered).
 */
public record GasSample(@RoboChartType("nat") int c, @RoboChartType("real") double i) {
}
