package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (CD-DM6): chemical identity {@code c} and measured intensity {@code i}.
 * A multi-sensor reading (CD-DM7) is an ordered {@code List<GasSensor>} whose 1-based
 * position identifies the sensing direction.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
