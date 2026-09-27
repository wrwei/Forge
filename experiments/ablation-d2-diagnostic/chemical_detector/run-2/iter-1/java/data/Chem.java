package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species. Equality is the only operation the
 * system needs on it.
 */
public record Chem(@RoboChartType("nat") int id) {
}
