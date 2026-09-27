package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: the chemical identity {@code c} and the measured
 * intensity {@code i} for it. Intensities are totally ordered and compared
 * with {@link chemical_detector.sensor.GasAnalysisFunctions#goreq}.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
