package chemdetector.data;

import chemdetector.annotation.RoboChartType;

/**
 * CD-DM6: GasSensor is a record with two fields: c (the chemical
 * identity) and i (the measured intensity for that chemical).
 * The Vehicle's gas event carries a sequence of GasSensor values.
 *
 * Intensity is modelled as a real for compatibility with the
 * goreq comparison; see CD-DM5 / CD-Fn4 notes in GasFunctions.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
