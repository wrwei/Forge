package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (the GasSensor record of CD-DM6): a chemical and the intensity
 * measured for it. A reading (CD-DM7) is an ordered list of these, one per sensing
 * direction. Intensity (CD-DM5) is a real number, compared only through goreq.
 *
 * @param c the chemical identity
 * @param i the measured intensity
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
