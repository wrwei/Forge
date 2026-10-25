package chemical_detector.types;

import chemical_detector.annotation.RoboChartType;

/**
 * One sensor's contribution to a gas reading: chemical identity {@code c} and intensity {@code i}.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
