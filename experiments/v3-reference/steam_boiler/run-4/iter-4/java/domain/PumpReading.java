package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of a PUMP_STATE(n, b) message (SB-DM3, SB-DM9): pump number n
 * (1 to 4) and whether that pump is on.
 *
 * @param pumpNumber  the pump the reading is about, 1 to 4
 * @param pumpRunning true if the pump reports itself on
 */
public record PumpReading(@RoboChartType("nat") int pumpNumber, boolean pumpRunning) {
}
