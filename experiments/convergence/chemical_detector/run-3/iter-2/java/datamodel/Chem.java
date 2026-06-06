package chemdetector.datamodel;

import chemdetector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species. Equality is the only operation
 * needed on Chem.
 */
public record Chem(@RoboChartType("nat") int id) {
}
