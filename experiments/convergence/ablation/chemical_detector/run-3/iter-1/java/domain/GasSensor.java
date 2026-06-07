package chemdetector.domain;

import chemdetector.annotation.RoboChartType;

/** One gas-sensor sample: chemical identity and measured intensity (CD-DM6). */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
