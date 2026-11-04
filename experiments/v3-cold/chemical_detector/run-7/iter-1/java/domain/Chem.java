package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/** Opaque identity of a chemical species; only equality of identities is used. */
public record Chem(@RoboChartType("nat") int id) {
}
