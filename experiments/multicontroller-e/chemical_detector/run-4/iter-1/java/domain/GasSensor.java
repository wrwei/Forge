package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: the chemical {@code c} and its measured intensity {@code i}.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
