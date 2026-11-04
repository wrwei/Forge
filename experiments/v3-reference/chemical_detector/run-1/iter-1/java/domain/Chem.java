package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species; only equality is used (CD-DM4).
 */
public record Chem(@RoboChartType("nat") int id) {
}
