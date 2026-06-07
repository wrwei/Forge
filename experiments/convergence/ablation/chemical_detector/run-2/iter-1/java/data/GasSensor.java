package chemdetector.data;

import chemdetector.annotation.RoboChartType;

/**
 * One gas-sensor sample: chemical identity {@code c} and measured
 * intensity {@code i} (CD-DM6).
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
