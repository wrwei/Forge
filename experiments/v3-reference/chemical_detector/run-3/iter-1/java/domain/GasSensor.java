package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One value of a multi-sensor gas reading (CD-DM6): the chemical identity and
 * the intensity measured for it. Intensity (CD-DM5) is a real number, ordered
 * by {@code goreq}.
 *
 * @param c the chemical identity
 * @param i the measured intensity for that chemical
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
