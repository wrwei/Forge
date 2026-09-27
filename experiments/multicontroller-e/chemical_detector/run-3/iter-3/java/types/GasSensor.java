package chemical_detector.types;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: the chemical {@code c} and its measured intensity {@code i}.
 * A multi-sensor reading is an ordered {@code List<GasSensor>}, where the position
 * in the list identifies the sensing direction.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
