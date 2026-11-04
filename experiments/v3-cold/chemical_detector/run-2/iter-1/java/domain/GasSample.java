package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: a chemical and its measured intensity (CD-DM6).
 * Intensity (CD-DM5) is a real number, ordered by
 * {@code GasSensorArray.goreq}.
 *
 * @param c the chemical identity
 * @param i the measured intensity for that chemical
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {}
