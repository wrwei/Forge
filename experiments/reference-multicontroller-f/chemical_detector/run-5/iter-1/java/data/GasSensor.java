package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/** One sensor's contribution to a gas reading: chemical identity c and measured intensity i. */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
