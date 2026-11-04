package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * The identity of a chemical species (CD-DM4). Opaque: equality is the only operation used.
 *
 * @param species a non-negative identifier of the species
 */
public record Chem(@RoboChartType("nat") int species) {
}
