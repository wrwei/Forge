package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (CD-DM6): the chemical identity and the intensity
 * measured for it. Intensity (CD-DM5) is a totally ordered real quantity.
 * A multi-sensor reading (CD-DM7) is an ordered list of these, the 1-based
 * position encoding the sensing direction.
 *
 * @param c the chemical identity
 * @param i the measured intensity
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
