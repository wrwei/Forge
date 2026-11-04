package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (CD-DM6): a chemical identity and the intensity measured for it.
 * Intensity (CD-DM5) is a natural number of sensor units; it is ordered only through
 * the goreq function.
 *
 * @param c the chemical identity
 * @param i the measured intensity for that chemical
 */
public record GasSensor(Chem c, @RoboChartType("nat") int i) {
}
