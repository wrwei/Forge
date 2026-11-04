package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/** Opaque identity of a chemical species; equality is its only operation (CD-DM4). */
public record Chem(@RoboChartType("nat") int species) {
}
