package chemdetector.domain;

import chemdetector.annotation.RoboChartType;

/**
 * One (chemical, intensity) reading from the gas-sensor array (CD-DM6).
 * The position of a GasSensor inside the gas event's sequence encodes
 * the sensing direction (mapped via the angle function).
 */
public record GasSensor(
        Chem c,
        @RoboChartType("real") double i
) {
}
