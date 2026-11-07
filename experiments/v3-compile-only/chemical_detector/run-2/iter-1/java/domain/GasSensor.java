package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One value of a multi-sensor gas reading (CD-DM6): the chemical {@code c} and
 * its measured intensity {@code i} (CD-DM5: intensities are totally ordered
 * reals).
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
