package chemical_detector.types;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: a chemical and the intensity measured for it
 * (CD-DM6; the requirements call this record GasSensor). Intensity
 * (CD-DM5) is a real number. A multi-sensor reading (CD-DM7) is an
 * ordered list of these, one per sensing direction.
 *
 * @param c the chemical identity
 * @param i the measured intensity
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
