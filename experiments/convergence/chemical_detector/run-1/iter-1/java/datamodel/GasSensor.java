package chemdetector.datamodel;

import chemdetector.annotation.RoboChartType;

/**
 * One gas-sensor reading (CD-DM6): the chemical identity c and the
 * measured intensity i for that chemical. Intensity (CD-DM5) is a
 * totally-ordered quantity represented as a real.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
