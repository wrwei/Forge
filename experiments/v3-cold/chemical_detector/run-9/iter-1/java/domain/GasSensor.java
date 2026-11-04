package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: chemical identity {@code c} and measured intensity {@code i} (CD-DM6).
 * Intensity is represented as a real number, ordered by {@code goreq} (CD-DM5).
 * A multi-sensor reading is an ordered {@code List<GasSensor>} (CD-DM7).
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
