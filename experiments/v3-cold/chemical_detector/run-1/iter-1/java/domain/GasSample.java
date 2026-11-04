package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One (chemical, intensity) pair of a gas reading: the value produced by one
 * sensor of the gas-sensor array. A whole reading is a {@code List<GasSample>}
 * whose 1-based position encodes the sensing direction.
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
