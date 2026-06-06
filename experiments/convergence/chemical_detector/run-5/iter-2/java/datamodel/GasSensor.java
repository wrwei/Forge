package chemdetector.datamodel;

import chemdetector.annotation.RoboChartType;

/**
 * One gas-sensor reading: the chemical identity and the measured
 * intensity for that chemical (CD-DM6). Intensity is represented as a
 * real number (CD-DM5).
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
