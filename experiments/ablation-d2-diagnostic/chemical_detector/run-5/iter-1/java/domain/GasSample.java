package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One (chemical, intensity) pair produced by one directional sensor of the gas-sensor
 * array: the GasSensor record of the specification.
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
