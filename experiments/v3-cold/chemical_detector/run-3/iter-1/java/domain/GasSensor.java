package chemical_detector.domain;

import chemical_detector.annotation.RoboChartType;

/**
 * One gas-sensor value: the chemical {@code c} and its measured intensity
 * {@code i} (CD-DM6). Intensity (CD-DM5) is represented as a real number.
 * A gas reading (CD-DM7) is an ordered {@code List<GasSensor>}.
 */
public record GasSensor(Chem c, @RoboChartType("real") double i) {
}
