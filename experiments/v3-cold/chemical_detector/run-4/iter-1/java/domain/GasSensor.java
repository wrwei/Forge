package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One value of a multi-sensor gas reading: a chemical and its measured intensity.
 *
 * @param c the chemical identity
 * @param i the measured intensity for that chemical
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
