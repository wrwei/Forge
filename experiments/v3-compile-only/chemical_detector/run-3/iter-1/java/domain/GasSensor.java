package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (CD-DM6): the chemical {@code c} and its measured
 * intensity {@code i}. A reading (CD-DM7) is a {@code List<GasSensor>} whose
 * 1-based position identifies the sensing direction.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
