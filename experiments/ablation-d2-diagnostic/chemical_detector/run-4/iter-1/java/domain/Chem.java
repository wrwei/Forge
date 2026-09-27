package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species (CD-DM4). Equality is the only operation used.
 */
public record Chem(@RoboChartType("nat") int id) {
}
