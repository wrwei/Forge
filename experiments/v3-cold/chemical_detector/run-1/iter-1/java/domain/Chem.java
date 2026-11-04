package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Identity of a chemical species. Only equality is used.
 */
public record Chem(@RoboChartType("nat") int id) {
}
