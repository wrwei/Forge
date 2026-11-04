package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species (CD-DM4); equality is the only
 * operation used on it.
 */
public record Chem(@RoboChartType("nat") int species) {
}
