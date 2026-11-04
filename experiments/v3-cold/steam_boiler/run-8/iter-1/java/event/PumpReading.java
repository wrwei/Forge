package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Content of the PUMP_STATE(n, b) message (SB-DM3, SB-DM9): pump {@code pumpIndex}
 * (1 to 4) reports whether it is on.
 *
 * @param pumpIndex number of the reporting pump, 1 to 4
 * @param pumpOpen  {@code true} if the pump is on
 */
public record PumpReading(@RoboChartType("nat") int pumpIndex, boolean pumpOpen) {
}
