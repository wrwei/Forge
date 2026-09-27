package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/**
 * One directional gas reading (CD-DM6): the chemical identity {@code c} and
 * the intensity {@code i} measured for it.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
