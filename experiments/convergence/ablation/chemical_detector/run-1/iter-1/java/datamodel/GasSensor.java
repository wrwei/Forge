package chemdetector.datamodel;

import chemdetector.annotation.RoboChartType;

/**
 * One gas-sensor sample: the chemical identity c and the measured
 * intensity i for that chemical.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
