package chemdetector.datamodel;

import chemdetector.annotation.RoboChartType;

/**
 * One gas-sensor reading: the chemical identity and the measured
 * intensity for that chemical.
 */
public record GasSensor(@RoboChartType("nat") int c, @RoboChartType("real") double i) {
}
