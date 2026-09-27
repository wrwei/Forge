package chemical_detector.types;

import chemical_detector.annotation.RoboChartType;

/** Opaque identity of a chemical species; only equality is used. */
public record Chem(@RoboChartType("nat") int id) {
}
