package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One sensor's value within a gas reading (CD-DM6): the chemical {@code c} and
 * its measured intensity {@code i} (CD-DM5, a natural number, ordered by
 * {@code goreq}). A gas reading (CD-DM7) is a {@code List<GasSensor>} whose
 * 1-based position identifies the sensing direction.
 */
public record GasSensor(Chem c, @RoboChartType("nat") int i) {
}
