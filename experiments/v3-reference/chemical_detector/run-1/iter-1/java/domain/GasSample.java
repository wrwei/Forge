package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: chemical identity c and measured intensity i (CD-DM6).
 * A gas reading (CD-DM7) is an ordered list of these, one per sensing direction.
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
