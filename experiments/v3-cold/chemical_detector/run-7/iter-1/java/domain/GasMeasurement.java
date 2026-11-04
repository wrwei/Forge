package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One (chemical, intensity) pair of a gas-sensor reading (the requirements' GasSensor record):
 * {@code c} is the chemical identity, {@code i} the measured intensity.
 */
public record GasMeasurement(Chem c, @RoboChartType("real") double i) {
}
