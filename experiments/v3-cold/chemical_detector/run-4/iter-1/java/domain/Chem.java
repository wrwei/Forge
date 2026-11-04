package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species; equality is its only operation.
 *
 * @param id the identifier of the species
 */
public record Chem(@RoboChartType("nat") int id) {
}
