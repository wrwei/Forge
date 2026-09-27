package chemical_detector.types;

import chemical_detector.annotation.RoboChartType;

/**
 * Opaque identity of a chemical species; equality is the only operation used.
 */
public record Chem(@RoboChartType("nat") int species) {
}
