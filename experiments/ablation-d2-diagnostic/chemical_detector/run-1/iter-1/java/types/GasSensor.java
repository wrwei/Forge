package chemical_detector.types;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: chemical identity {@code c} and measured intensity {@code i}.
 * A gas reading is an ordered {@code List<GasSensor>}; list position encodes the sensing direction.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
