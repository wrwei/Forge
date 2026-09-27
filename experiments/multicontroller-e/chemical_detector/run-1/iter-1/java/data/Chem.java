package chemical_detector.data;

import chemical_detector.annotation.RoboChartType;

/**
 * Identity of a chemical species. Opaque: equality is the only operation the
 * system relies on.
 */
public record Chem(@RoboChartType("nat") int id) {
}
