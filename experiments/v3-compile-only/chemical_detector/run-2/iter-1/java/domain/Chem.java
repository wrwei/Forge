package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species (CD-DM4). Only equality is used; the
 * species number has no meaning beyond distinguishing species.
 */
public record Chem(@RoboChartType("nat") int species) {
}
