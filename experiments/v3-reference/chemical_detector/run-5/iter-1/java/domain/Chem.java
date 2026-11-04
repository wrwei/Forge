package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species. Equality is the only operation used on it.
 *
 * @param id the identifier of the chemical species
 */
public record Chem(@RoboChartType("nat") int id) {
}
