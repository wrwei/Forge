package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * The identity of a chemical species (CD-DM4). Opaque: only equality is used,
 * so the identity is carried as a plain identifier.
 */
public record Chem(@RoboChartType("nat") int id) {
}
