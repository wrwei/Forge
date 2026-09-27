package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/**
 * One element of a multi-sensor gas reading: the chemical {@code c} seen by one
 * directional gas sensor and its measured intensity {@code i}.
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
