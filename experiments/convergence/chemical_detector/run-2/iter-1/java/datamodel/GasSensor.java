package chemdetector.datamodel;

import chemdetector.annotation.RoboChartType;

/**
 * One gas-sensor reading (CD-DM6): chemical identity c (CD-DM4, opaque,
 * equality only) and measured intensity i (CD-DM5, totally ordered).
 */
public record GasSensor(@RoboChartType("nat") int c, @RoboChartType("real") double i) {
}
