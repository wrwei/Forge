package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: the chemical {@code c} and its measured intensity {@code i}
 * (CD-DM6). Intensity is a totally ordered real quantity (CD-DM5). A reading
 * (CD-DM7) is an ordered list of these, position k (1-based) being sensor k.
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
