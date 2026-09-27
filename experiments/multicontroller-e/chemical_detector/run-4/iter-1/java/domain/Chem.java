package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species; only equality is meaningful.
 */
public record Chem(@RoboChartType("nat") int id) {
}
