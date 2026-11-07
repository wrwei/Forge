package chemical_detector.types;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species (CD-DM4). Only equality is used.
 *
 * @param id the species identifier
 */
public record Chem(@RoboChartType("nat") int id) {
}
