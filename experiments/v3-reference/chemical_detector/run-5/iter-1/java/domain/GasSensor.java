package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: the chemical identity and the intensity measured for it.
 * Intensities are totally ordered and compared only through {@code goreq}.
 *
 * @param c the chemical identity
 * @param i the measured intensity for that chemical
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
