package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (the specification's GasSensor record, CD-DM6): the chemical
 * identity {@code c} and its measured intensity {@code i}. Intensity (CD-DM5) is a
 * totally ordered real quantity.
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
