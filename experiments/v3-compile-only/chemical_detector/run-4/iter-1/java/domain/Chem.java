package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Identity of a chemical species (CD-DM4). Opaque: equality is the only
 * operation the system uses on it.
 *
 * @param chemId the species identifier
 */
public record Chem(@RoboChartType("nat") int chemId) {
}
