package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value (the specification's {@code GasSensor} record): the
 * chemical identity {@code c} and the measured intensity {@code i} for it.
 * A multi-sensor reading is an ordered list of these, the position in the list
 * encoding the sensing direction.
 */
public record GasSample(Chem c, @RoboChartType("real") double i) {
}
