package chemdetector.datamodel;

import chemdetector.annotation.RoboChartType;

/**
 * One gas-sensor sample: the chemical identity and the measured intensity
 * for that chemical (CD-DM6).
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
