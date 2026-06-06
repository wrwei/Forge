package chemdetector.datamodel;

import chemdetector.annotation.RoboChartType;

/**
 * A single sensor reading (CD-DM6): the chemical identity {@code c} and
 * its measured intensity {@code i}. Intensity (CD-DM5) is a
 * totally-ordered quantity, modelled here as a RoboChart {@code real}.
 * The Vehicle's gas event carries a sequence (CD-DM7) of these.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
