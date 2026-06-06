package chemdetector.datamodel;

import chemdetector.annotation.RoboChartType;

/**
 * One directional gas reading: the chemical identity and the measured
 * intensity for that chemical.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
