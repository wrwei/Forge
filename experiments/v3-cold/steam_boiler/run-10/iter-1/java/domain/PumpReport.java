package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of a PUMP_STATE(n, b) message: pump number {@code pumpIndex} is on or off
 * (SB-DM3, SB-DM9). Pumps are numbered 0 to 3.
 *
 * @param pumpIndex the pump number
 * @param pumpOn whether the pump reports itself switched on
 */
public record PumpReport(@RoboChartType("nat") int pumpIndex, boolean pumpOn) {
}
