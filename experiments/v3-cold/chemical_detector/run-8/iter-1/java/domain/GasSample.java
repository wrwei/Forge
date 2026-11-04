package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (CD-DM6): the chemical {@code c} and its measured
 * intensity {@code i} (CD-DM5, an ordered real). A gas reading (CD-DM7) is a
 * {@code List<GasSample>} whose position encodes the sensing direction.
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
