package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species (CD-DM4). Record equality is the only
 * operation the system uses on it.
 *
 * @param species the identifier of the chemical species
 */
public record Chem(@RoboChartType("nat") int species) {
}
