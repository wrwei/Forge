package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (CD-DM6): chemical identity {@code c} and measured
 * intensity {@code i} (CD-DM5, a totally ordered real).
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
