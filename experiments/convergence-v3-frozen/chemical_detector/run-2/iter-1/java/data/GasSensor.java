package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/** One directional gas reading: chemical identity plus measured intensity. */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
